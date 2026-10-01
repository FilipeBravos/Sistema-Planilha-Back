package com.filipebravos.planilha;

import com.filipebravos.planilha.despesa.DespesaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DespesaApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    DespesaRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private static String json(String categoria, String nome, String data, String valor, String forma, String parcelas) {
        return """
                {"categoria":"%s","nome":"%s","data":"%s","valor":%s,"formaPagamento":"%s","parcelas":%s}
                """.formatted(categoria, nome, data, valor, forma, parcelas);
    }

    private String criar(String corpo) throws Exception {
        MvcResult r = mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn();
        String resposta = r.getResponse().getContentAsString();
        return resposta.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    @Test
    void cartaoParceladoCalculaValorDaParcela() throws Exception {
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("CARRO", "PNEUS/2025", "2026-09-10", "1000.00", "CARTAO", "3")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parcelas").value(3))
                .andExpect(jsonPath("$.valorParcela").value(333.33));
    }

    @Test
    void parcelasSaoIgnoradasForaDoCartao() throws Exception {
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("MOTO", "SEGURO", "2026-09-10", "200", "BOLETO", "5")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parcelas").doesNotExist())
                .andExpect(jsonPath("$.valorParcela").doesNotExist());
    }

    @Test
    void cartaoExigeParcelas() throws Exception {
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("CARRO", "SEGURO", "2026-09-10", "200", "CARTAO", "null")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaCategoriaEFormaInvalidasEValorZero() throws Exception {
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("OUTRA", "X", "2026-09-10", "10", "DINHEIRO", "null")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("CARRO", "X", "2026-09-10", "10", "PIX", "null")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("CARRO", "X", "2026-09-10", "0", "DINHEIRO", "null")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("CARRO", " ", "2026-09-10", "10", "DINHEIRO", "null")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtraPorCategoriaEPeriodoEResume() throws Exception {
        criar(json("CARRO", "GASOLINA", "2026-09-05", "100", "DINHEIRO", "null"));
        criar(json("CARRO", "SEGURO", "2026-10-05", "300", "CHEQUE", "null"));
        criar(json("FARMACIA", "VAGNER", "2026-09-06", "50.50", "DINHEIRO", "null"));

        mvc.perform(get("/api/despesas").param("categoria", "CARRO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/despesas").param("inicio", "2026-09-01").param("fim", "2026-09-30"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/despesas").param("inicio", "2026-10-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/despesas/resumo").param("inicio", "2026-09-01").param("fim", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150.5))
                .andExpect(jsonPath("$.porCategoria.length()").value(10))
                .andExpect(jsonPath("$.porCategoria[?(@.categoria == 'CARRO')].total").value(100.0))
                .andExpect(jsonPath("$.porCategoria[?(@.categoria == 'FARMACIA')].total").value(50.5))
                .andExpect(jsonPath("$.porCategoria[?(@.categoria == 'MOTO')].total").value(0));
    }

    @Test
    void atualizaEExclui() throws Exception {
        String id = criar(json("CARRO", "GASOLINA", "2026-09-05", "100", "DINHEIRO", "null"));

        mvc.perform(put("/api/despesas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("MOTO", "IPVA", "2026-09-07", "400", "CARTAO", "4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("MOTO"))
                .andExpect(jsonPath("$.valorParcela").value(100.0));

        mvc.perform(delete("/api/despesas/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/despesas/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/despesas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("MOTO", "IPVA", "2026-09-07", "400", "DINHEIRO", "null")))
                .andExpect(status().isNotFound());
    }
}

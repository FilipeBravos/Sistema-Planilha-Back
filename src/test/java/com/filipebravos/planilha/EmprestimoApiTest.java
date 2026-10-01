package com.filipebravos.planilha;

import com.filipebravos.planilha.emprestimo.EmprestimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(AutenticadoTestConfig.class)
@AutoConfigureMockMvc
class EmprestimoApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EmprestimoRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private static String json(String credor, String terceiro, String valor, int parcelas, String pago) {
        return """
                {"credor":"%s","nomeTerceiro":%s,"data":"2026-09-10","valor":%s,"parcelas":%d,"valorPago":%s}
                """.formatted(credor, terceiro == null ? "null" : "\"" + terceiro + "\"", valor, parcelas, pago);
    }

    private String criar(String corpo) throws Exception {
        String resposta = mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return resposta.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    @Test
    void criaComSaldoEValorDaParcela() throws Exception {
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("BANCO_ITAU", null, "1000", 3, "400")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorParcela").value(333.33))
                .andExpect(jsonPath("$.saldo").value(600.0))
                .andExpect(jsonPath("$.quitado").value(false));
    }

    @Test
    void terceirosExigeNomeEOutrosIgnoram() throws Exception {
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("TERCEIROS", null, "500", 2, "0")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("TERCEIROS", "  ", "500", 2, "0")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("TERCEIROS", " João ", "500", 2, "0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeTerceiro").value("João"));
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ROMILDA", "Fulano", "500", 2, "0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeTerceiro").doesNotExist());
    }

    @Test
    void validaCamposNumericosECredor() throws Exception {
        String[] invalidos = {
                json("ROMILDA", null, "0", 2, "0"),
                json("ROMILDA", null, "100", 0, "0"),
                json("ROMILDA", null, "100", 2, "-1"),
                json("NUBANK", null, "100", 2, "0"),
        };
        for (String corpo : invalidos) {
            mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void pagarAlemDoValorQuitaSemSaldoNegativo() throws Exception {
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("BANCO_DO_BRASIL", null, "1000", 10, "1200")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.quitado").value(true));
    }

    @Test
    void resumoPorCredor() throws Exception {
        criar(json("ROMILDA", null, "1000", 5, "250"));
        criar(json("ROMILDA", null, "500", 2, "500"));
        criar(json("TERCEIROS", "João", "300", 3, "400"));

        mvc.perform(get("/api/emprestimos/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmprestado").value(1800.0))
                .andExpect(jsonPath("$.totalPago").value(1150.0))
                // 750 (Romilda #1) + 0 + 0: pagamento a mais num empréstimo não abate o de outro
                .andExpect(jsonPath("$.saldo").value(750.0))
                .andExpect(jsonPath("$.porCredor.length()").value(5))
                .andExpect(jsonPath("$.porCredor[?(@.credor == 'ROMILDA')].saldo").value(750.0))
                .andExpect(jsonPath("$.porCredor[?(@.credor == 'VERONICA')].emprestado").value(0));

        mvc.perform(get("/api/emprestimos").param("credor", "ROMILDA"))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/emprestimos"))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void atualizaEExclui() throws Exception {
        String id = criar(json("ROMILDA", null, "1000", 5, "0"));

        mvc.perform(put("/api/emprestimos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("BANCO_ITAU", null, "1000", 5, "200")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credor").value("BANCO_ITAU"))
                .andExpect(jsonPath("$.saldo").value(800.0));

        mvc.perform(delete("/api/emprestimos/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/emprestimos/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/emprestimos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("ROMILDA", null, "1000", 5, "0")))
                .andExpect(status().isNotFound());
    }
}

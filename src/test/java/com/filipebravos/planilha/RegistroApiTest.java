package com.filipebravos.planilha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroApiTest {

    @Autowired
    MockMvc mvc;

    private static String json(int kmInicial, int kmFinal) {
        return """
                {"data":"2026-09-28","horaInicialVagner":"08:00","horaFinalVagner":"12:30",
                 "horaInicialFilipe":"13:00","horaFinalFilipe":"14:00",
                 "kmInicial":%d,"kmFinal":%d,"cargaPostoVagner":50,"cargaPostoFilipe":20,"valorVagner":200,"valorFilipe":120}
                """.formatted(kmInicial, kmFinal);
    }

    @Test
    void criaEListaComCamposCalculados() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(json(1000, 1100)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaSemana").value("Segunda-feira"))
                .andExpect(jsonPath("$.totalMinutosVagner").value(270))
                .andExpect(jsonPath("$.totalMinutosFilipe").value(60))
                .andExpect(jsonPath("$.totalMinutos").value(330))
                .andExpect(jsonPath("$.totalKm").value(100))
                .andExpect(jsonPath("$.liquidoVagner").value(150.0))
                .andExpect(jsonPath("$.liquidoFilipe").value(100.0));

        mvc.perform(get("/api/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brutoTotal").value(320.0));
    }

    @Test
    void filtraPorPeriodoComLimitesAbertos() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(json(1000, 1100)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/registros").param("inicio", "2026-09-28"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].data").value("2026-09-28"));
        mvc.perform(get("/api/registros").param("fim", "2026-09-28"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].data").value("2026-09-28"));
        mvc.perform(get("/api/registros").param("inicio", "2026-09-29").param("fim", "2026-10-05"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.data == '2026-09-28')]").isEmpty());
    }

    @Test
    void rejeitaHorarioIncompleto() throws Exception {
        String corpo = """
                {"data":"2026-09-28","horaInicialVagner":"08:00","kmInicial":0,"kmFinal":1,
                 "cargaPostoVagner":0,"cargaPostoFilipe":0,"valorVagner":0,"valorFilipe":0}
                """;
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaKmFinalMenorQueInicial() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(json(1100, 1000)))
                .andExpect(status().isBadRequest());
    }
}

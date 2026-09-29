package com.filipebravos.planilha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1")
class RegistroApiTest {

    @Autowired
    MockMvc mvc;

    private static String json(int kmInicial, int kmFinal) {
        return """
                {"data":"2026-09-28","horaInicial":"08:00","horaFinal":"12:30",
                 "kmInicial":%d,"kmFinal":%d,"cargaPosto":50,"valorVagner":200,"valorFilipe":120}
                """.formatted(kmInicial, kmFinal);
    }

    @Test
    void criaEListaComCamposCalculados() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(json(1000, 1100)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaSemana").value("Segunda-feira"))
                .andExpect(jsonPath("$.totalMinutos").value(270))
                .andExpect(jsonPath("$.totalKm").value(100))
                .andExpect(jsonPath("$.liquidoVagner").value(150.0))
                .andExpect(jsonPath("$.liquidoFilipe").value(70.0));

        mvc.perform(get("/api/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brutoTotal").value(320.0));
    }

    @Test
    void rejeitaKmFinalMenorQueInicial() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(json(1100, 1000)))
                .andExpect(status().isBadRequest());
    }
}

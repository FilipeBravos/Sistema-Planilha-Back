package com.filipebravos.planilha;

import com.filipebravos.planilha.config.AjusteDeEsquema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(AutenticadoTestConfig.class)
@AutoConfigureMockMvc
class RegistroApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    AjusteDeEsquema ajusteDeEsquema;

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

    private static String semKm(String km) {
        return """
                {"data":"2026-09-28","horaInicialVagner":"08:00","horaFinalVagner":"12:30",
                 %s "cargaPostoVagner":50,"cargaPostoFilipe":0,"valorVagner":200,"valorFilipe":0}
                """.formatted(km);
    }

    @Test
    void kmPodeFicarEmBranco() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(semKm("")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kmInicial").value(nullValue()))
                .andExpect(jsonPath("$.kmFinal").value(nullValue()))
                .andExpect(jsonPath("$.totalKm").value(nullValue()))
                .andExpect(jsonPath("$.liquidoVagner").value(150.0));

        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON)
                        .content(semKm("\"kmInicial\":null,\"kmFinal\":null,")))
                .andExpect(status().isCreated());
    }

    @Test
    void kmComSoUmDosDoisNaoTemTotal() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(semKm("\"kmFinal\":1500,")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kmFinal").value(1500))
                .andExpect(jsonPath("$.totalKm").value(nullValue()));
    }

    @Test
    void kmNegativoContinuaRecusado() throws Exception {
        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(semKm("\"kmInicial\":-1,")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bancoAntigoComKmObrigatorioEAjustadoNaInicializacao() throws Exception {
        // simula um banco criado antes de o Km ser opcional
        jdbc.execute("ALTER TABLE registro_diario ALTER COLUMN km_inicial SET NOT NULL");
        jdbc.execute("ALTER TABLE registro_diario ALTER COLUMN km_final SET NOT NULL");

        ajusteDeEsquema.run(null);
        ajusteDeEsquema.run(null); // repetir é seguro

        mvc.perform(post("/api/registros").contentType(MediaType.APPLICATION_JSON).content(semKm("")))
                .andExpect(status().isCreated());
    }
}

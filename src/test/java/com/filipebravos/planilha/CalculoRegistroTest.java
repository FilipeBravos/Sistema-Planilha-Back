package com.filipebravos.planilha;

import com.filipebravos.planilha.dto.ResumoResponse;
import com.filipebravos.planilha.model.RegistroDiario;
import com.filipebravos.planilha.service.CalculoRegistro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CalculoRegistroTest {

    private static LocalTime t(String hora) {
        return hora == null ? null : LocalTime.parse(hora);
    }

    private RegistroDiario registro(String data, String iniV, String fimV, String iniF, String fimF,
                                    Integer kmIniV, Integer kmFimV, Integer kmIniF, Integer kmFimF,
                                    String postoV, String postoF, String vagner, String filipe) {
        RegistroDiario r = new RegistroDiario();
        r.setData(LocalDate.parse(data));
        r.setHoraInicialVagner(t(iniV));
        r.setHoraFinalVagner(t(fimV));
        r.setHoraInicialFilipe(t(iniF));
        r.setHoraFinalFilipe(t(fimF));
        r.setKmInicialVagner(kmIniV);
        r.setKmFinalVagner(kmFimV);
        r.setKmInicialFilipe(kmIniF);
        r.setKmFinalFilipe(kmFimF);
        r.setCargaPostoVagner(new BigDecimal(postoV));
        r.setCargaPostoFilipe(new BigDecimal(postoF));
        r.setValorVagner(new BigDecimal(vagner));
        r.setValorFilipe(new BigDecimal(filipe));
        return r;
    }

    @Test
    void calculaTotaisDoDia() {
        RegistroDiario r = registro("2026-09-28", "08:00", "12:30", "13:00", "17:00",
                1000, 1180, 2000, 2050, "50.00", "20.00", "200.00", "100.00");
        assertEquals(270, CalculoRegistro.totalMinutosVagner(r));
        assertEquals(240, CalculoRegistro.totalMinutosFilipe(r));
        assertEquals(510, CalculoRegistro.totalMinutos(r));
        assertEquals(180, CalculoRegistro.totalKmVagner(r));
        assertEquals(50, CalculoRegistro.totalKmFilipe(r));
        assertEquals(230, CalculoRegistro.totalKm(r));
        assertEquals(new BigDecimal("150.00"), CalculoRegistro.liquidoVagner(r));
        assertEquals(new BigDecimal("80.00"), CalculoRegistro.liquidoFilipe(r));
        assertEquals("Segunda-feira", CalculoRegistro.diaSemana(r));
    }

    @Test
    void kmEmBrancoNaoQuebraOsCalculosENaoEntraNaSoma() {
        RegistroDiario semKm = registro("2026-09-28", "08:00", "12:00", null, null, null, null, null, null, "0", "0", "100", "0");
        RegistroDiario soFinal = registro("2026-09-29", "08:00", "12:00", null, null, null, 500, null, null, "0", "0", "100", "0");
        RegistroDiario comKm = registro("2026-09-30", "08:00", "12:00", null, null, 100, 160, null, null, "0", "0", "100", "0");

        assertNull(CalculoRegistro.totalKmVagner(semKm));
        assertNull(CalculoRegistro.totalKmVagner(soFinal));
        assertEquals(60, CalculoRegistro.totalKmVagner(comKm));
        assertEquals(0, CalculoRegistro.totalKm(semKm));
        assertEquals(60, CalculoRegistro.resumir(List.of(semKm, soFinal, comKm)).kmVagner());
        assertEquals(0, CalculoRegistro.resumir(List.of(semKm)).kmTotal());
    }

    @Test
    void kmDeCadaUmESomaDoDia() {
        RegistroDiario soFilipe = registro("2026-09-28", null, null, "08:00", "12:00", null, null, 500, 540, "0", "0", "0", "100");
        assertNull(CalculoRegistro.totalKmVagner(soFilipe));
        assertEquals(40, CalculoRegistro.totalKmFilipe(soFilipe));
        assertEquals(40, CalculoRegistro.totalKm(soFilipe));
    }

    @Test
    void turnoQueViraMeiaNoiteEPessoaSemHorario() {
        RegistroDiario r = registro("2026-09-28", "22:00", "02:30", null, null, 0, 10, null, null, "0", "0", "0", "0");
        assertEquals(270, CalculoRegistro.totalMinutosVagner(r));
        assertEquals(0, CalculoRegistro.totalMinutosFilipe(r));
    }

    @Test
    void resumoSomaPorPessoaEConjunto() {
        RegistroDiario a = registro("2026-09-28", "08:00", "12:00", null, null, 0, 100, null, null, "40", "0", "300", "0");
        RegistroDiario b = registro("2026-09-29", null, null, "08:00", "10:00", null, null, 100, 150, "0", "20", "0", "150");
        RegistroDiario c = registro("2026-09-30", "10:00", "11:00", "10:00", "12:00", 150, 170, 200, 230, "10", "15", "80", "60");

        ResumoResponse s = CalculoRegistro.resumir(List.of(a, b, c));

        assertEquals(300, s.minutosVagner());
        assertEquals(240, s.minutosFilipe());
        assertEquals(540, s.minutosTotal());
        assertEquals(new BigDecimal("380"), s.brutoVagner());
        assertEquals(new BigDecimal("210"), s.brutoFilipe());
        assertEquals(new BigDecimal("590"), s.brutoTotal());
        assertEquals(new BigDecimal("330"), s.liquidoVagner());
        assertEquals(new BigDecimal("175"), s.liquidoFilipe());
        assertEquals(new BigDecimal("50"), s.cargaPostoVagner());
        assertEquals(new BigDecimal("35"), s.cargaPostoFilipe());
        assertEquals(120, s.kmVagner());
        assertEquals(80, s.kmFilipe());
        assertEquals(200, s.kmTotal());
    }
}

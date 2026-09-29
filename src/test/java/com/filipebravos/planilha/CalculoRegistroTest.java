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

class CalculoRegistroTest {

    private RegistroDiario registro(String data, String ini, String fim, int kmIni, int kmFim,
                                    String posto, String vagner, String filipe) {
        RegistroDiario r = new RegistroDiario();
        r.setData(LocalDate.parse(data));
        r.setHoraInicial(LocalTime.parse(ini));
        r.setHoraFinal(LocalTime.parse(fim));
        r.setKmInicial(kmIni);
        r.setKmFinal(kmFim);
        r.setCargaPosto(new BigDecimal(posto));
        r.setValorVagner(new BigDecimal(vagner));
        r.setValorFilipe(new BigDecimal(filipe));
        return r;
    }

    @Test
    void calculaTotaisDoDia() {
        RegistroDiario r = registro("2026-09-28", "08:00", "17:30", 1000, 1180, "50.00", "200.00", "0.00");
        assertEquals(570, CalculoRegistro.totalMinutos(r));
        assertEquals(180, CalculoRegistro.totalKm(r));
        assertEquals(new BigDecimal("150.00"), CalculoRegistro.liquidoVagner(r));
        assertEquals(new BigDecimal("-50.00"), CalculoRegistro.liquidoFilipe(r));
        assertEquals("Segunda-feira", CalculoRegistro.diaSemana(r));
    }

    @Test
    void turnoQueViraMeiaNoite() {
        RegistroDiario r = registro("2026-09-28", "22:00", "02:30", 0, 10, "0", "0", "0");
        assertEquals(270, CalculoRegistro.totalMinutos(r));
    }

    @Test
    void resumoSomaPorPessoaEConjunto() {
        RegistroDiario a = registro("2026-09-28", "08:00", "12:00", 0, 100, "40", "300", "0");
        RegistroDiario b = registro("2026-09-29", "08:00", "10:00", 100, 150, "20", "0", "150");
        RegistroDiario c = registro("2026-09-30", "10:00", "11:00", 150, 170, "10", "80", "60");

        ResumoResponse s = CalculoRegistro.resumir(List.of(a, b, c));

        assertEquals(420, s.minutosTotal());
        assertEquals(300, s.minutosVagner());
        assertEquals(180, s.minutosFilipe());
        assertEquals(new BigDecimal("380"), s.brutoVagner());
        assertEquals(new BigDecimal("210"), s.brutoFilipe());
        assertEquals(new BigDecimal("590"), s.brutoTotal());
        assertEquals(new BigDecimal("310"), s.liquidoVagner());
        assertEquals(new BigDecimal("140"), s.liquidoFilipe());
        assertEquals(170, s.kmTotal());
    }
}

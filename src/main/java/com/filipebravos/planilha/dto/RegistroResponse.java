package com.filipebravos.planilha.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroResponse(
        Long id,
        LocalDate data,
        String diaSemana,
        LocalTime horaInicial,
        LocalTime horaFinal,
        long totalMinutos,
        int kmInicial,
        int kmFinal,
        int totalKm,
        BigDecimal cargaPosto,
        BigDecimal valorVagner,
        BigDecimal valorFilipe,
        BigDecimal liquidoVagner,
        BigDecimal liquidoFilipe) {
}

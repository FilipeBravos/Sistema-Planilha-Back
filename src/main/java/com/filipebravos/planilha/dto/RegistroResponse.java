package com.filipebravos.planilha.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroResponse(
        Long id,
        LocalDate data,
        String diaSemana,
        LocalTime horaInicialVagner,
        LocalTime horaFinalVagner,
        long totalMinutosVagner,
        LocalTime horaInicialFilipe,
        LocalTime horaFinalFilipe,
        long totalMinutosFilipe,
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

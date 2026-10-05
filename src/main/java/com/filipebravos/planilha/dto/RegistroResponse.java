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
        Integer kmInicial,
        Integer kmFinal,
        Integer totalKm,
        BigDecimal cargaPostoVagner,
        BigDecimal cargaPostoFilipe,
        BigDecimal valorVagner,
        BigDecimal valorFilipe,
        BigDecimal liquidoVagner,
        BigDecimal liquidoFilipe) {
}

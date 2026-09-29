package com.filipebravos.planilha.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroRequest(
        @NotNull LocalDate data,
        @NotNull LocalTime horaInicial,
        @NotNull LocalTime horaFinal,
        @NotNull @Min(0) Integer kmInicial,
        @NotNull @Min(0) Integer kmFinal,
        @NotNull @PositiveOrZero BigDecimal cargaPosto,
        @NotNull @PositiveOrZero BigDecimal valorVagner,
        @NotNull @PositiveOrZero BigDecimal valorFilipe) {

    @AssertTrue(message = "Km final não pode ser menor que o Km inicial")
    public boolean isKmValido() {
        return kmInicial == null || kmFinal == null || kmFinal >= kmInicial;
    }
}

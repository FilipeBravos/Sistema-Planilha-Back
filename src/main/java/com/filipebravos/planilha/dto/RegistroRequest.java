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
        LocalTime horaInicialVagner,
        LocalTime horaFinalVagner,
        LocalTime horaInicialFilipe,
        LocalTime horaFinalFilipe,
        @NotNull @Min(0) Integer kmInicial,
        @NotNull @Min(0) Integer kmFinal,
        @NotNull @PositiveOrZero BigDecimal cargaPostoVagner,
        @NotNull @PositiveOrZero BigDecimal cargaPostoFilipe,
        @NotNull @PositiveOrZero BigDecimal valorVagner,
        @NotNull @PositiveOrZero BigDecimal valorFilipe) {

    @AssertTrue(message = "Informe hora inicial e final do Vagner (ou deixe as duas em branco)")
    public boolean isHorarioVagnerValido() {
        return (horaInicialVagner == null) == (horaFinalVagner == null);
    }

    @AssertTrue(message = "Informe hora inicial e final do Filipe (ou deixe as duas em branco)")
    public boolean isHorarioFilipeValido() {
        return (horaInicialFilipe == null) == (horaFinalFilipe == null);
    }

    @AssertTrue(message = "Km final não pode ser menor que o Km inicial")
    public boolean isKmValido() {
        return kmInicial == null || kmFinal == null || kmFinal >= kmInicial;
    }
}

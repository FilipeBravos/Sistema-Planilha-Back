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
        @Min(0) Integer kmInicialVagner,
        @Min(0) Integer kmFinalVagner,
        @Min(0) Integer kmInicialFilipe,
        @Min(0) Integer kmFinalFilipe,
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

    @AssertTrue(message = "Km final do Vagner não pode ser menor que o Km inicial")
    public boolean isKmVagnerValido() {
        return kmInicialVagner == null || kmFinalVagner == null || kmFinalVagner >= kmInicialVagner;
    }

    @AssertTrue(message = "Km final do Filipe não pode ser menor que o Km inicial")
    public boolean isKmFilipeValido() {
        return kmInicialFilipe == null || kmFinalFilipe == null || kmFinalFilipe >= kmInicialFilipe;
    }
}

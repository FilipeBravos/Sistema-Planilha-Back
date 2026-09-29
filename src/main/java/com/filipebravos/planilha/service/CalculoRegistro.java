package com.filipebravos.planilha.service;

import com.filipebravos.planilha.dto.RegistroResponse;
import com.filipebravos.planilha.dto.ResumoResponse;
import com.filipebravos.planilha.model.RegistroDiario;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Regras de cálculo da planilha, sem dependência de Spring.
 */
public final class CalculoRegistro {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private CalculoRegistro() {
    }

    /** Hora final - hora inicial; se a final for menor, o turno virou a meia-noite. */
    public static long totalMinutos(RegistroDiario r) {
        long minutos = Duration.between(r.getHoraInicial(), r.getHoraFinal()).toMinutes();
        return minutos < 0 ? minutos + 24 * 60 : minutos;
    }

    public static int totalKm(RegistroDiario r) {
        return r.getKmFinal() - r.getKmInicial();
    }

    public static BigDecimal liquidoVagner(RegistroDiario r) {
        return r.getValorVagner().subtract(r.getCargaPosto());
    }

    public static BigDecimal liquidoFilipe(RegistroDiario r) {
        return r.getValorFilipe().subtract(r.getCargaPosto());
    }

    public static String diaSemana(RegistroDiario r) {
        String nome = r.getData().getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }

    public static RegistroResponse toResponse(RegistroDiario r) {
        return new RegistroResponse(
                r.getId(), r.getData(), diaSemana(r),
                r.getHoraInicial(), r.getHoraFinal(), totalMinutos(r),
                r.getKmInicial(), r.getKmFinal(), totalKm(r),
                r.getCargaPosto(), r.getValorVagner(), r.getValorFilipe(),
                liquidoVagner(r), liquidoFilipe(r));
    }

    /**
     * Horas individuais: a planilha tem uma única faixa de horário por dia, então as
     * horas do dia são atribuídas a quem teve valor lançado (> 0) naquele dia. O total
     * conjunto conta cada dia uma só vez.
     */
    public static ResumoResponse resumir(List<RegistroDiario> registros) {
        long minTotal = 0;
        long minVagner = 0;
        long minFilipe = 0;
        int km = 0;
        BigDecimal brutoVagner = BigDecimal.ZERO;
        BigDecimal brutoFilipe = BigDecimal.ZERO;
        BigDecimal liqVagner = BigDecimal.ZERO;
        BigDecimal liqFilipe = BigDecimal.ZERO;

        for (RegistroDiario r : registros) {
            long minutos = totalMinutos(r);
            minTotal += minutos;
            if (r.getValorVagner().signum() > 0) {
                minVagner += minutos;
            }
            if (r.getValorFilipe().signum() > 0) {
                minFilipe += minutos;
            }
            km += totalKm(r);
            brutoVagner = brutoVagner.add(r.getValorVagner());
            brutoFilipe = brutoFilipe.add(r.getValorFilipe());
            liqVagner = liqVagner.add(liquidoVagner(r));
            liqFilipe = liqFilipe.add(liquidoFilipe(r));
        }

        return new ResumoResponse(
                minTotal, minVagner, minFilipe,
                brutoVagner, brutoFilipe, brutoVagner.add(brutoFilipe),
                liqVagner, liqFilipe, liqVagner.add(liqFilipe),
                km);
    }
}

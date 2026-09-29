package com.filipebravos.planilha.service;

import com.filipebravos.planilha.dto.RegistroResponse;
import com.filipebravos.planilha.dto.ResumoResponse;
import com.filipebravos.planilha.model.RegistroDiario;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
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
    public static long minutos(LocalTime inicial, LocalTime fim) {
        if (inicial == null || fim == null) {
            return 0;
        }
        long minutos = Duration.between(inicial, fim).toMinutes();
        return minutos < 0 ? minutos + 24 * 60 : minutos;
    }

    public static long totalMinutosVagner(RegistroDiario r) {
        return minutos(r.getHoraInicialVagner(), r.getHoraFinalVagner());
    }

    public static long totalMinutosFilipe(RegistroDiario r) {
        return minutos(r.getHoraInicialFilipe(), r.getHoraFinalFilipe());
    }

    /** Horas dos dois somadas. */
    public static long totalMinutos(RegistroDiario r) {
        return totalMinutosVagner(r) + totalMinutosFilipe(r);
    }

    public static int totalKm(RegistroDiario r) {
        return r.getKmFinal() - r.getKmInicial();
    }

    /** Valor Vagner - Carga Posto do Vagner. */
    public static BigDecimal liquidoVagner(RegistroDiario r) {
        return r.getValorVagner().subtract(r.getCargaPostoVagner());
    }

    /** Valor Filipe - Carga Posto do Filipe. */
    public static BigDecimal liquidoFilipe(RegistroDiario r) {
        return r.getValorFilipe().subtract(r.getCargaPostoFilipe());
    }

    public static String diaSemana(RegistroDiario r) {
        String nome = r.getData().getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }

    public static RegistroResponse toResponse(RegistroDiario r) {
        return new RegistroResponse(
                r.getId(), r.getData(), diaSemana(r),
                r.getHoraInicialVagner(), r.getHoraFinalVagner(), totalMinutosVagner(r),
                r.getHoraInicialFilipe(), r.getHoraFinalFilipe(), totalMinutosFilipe(r),
                totalMinutos(r),
                r.getKmInicial(), r.getKmFinal(), totalKm(r),
                r.getCargaPostoVagner(), r.getCargaPostoFilipe(), r.getValorVagner(), r.getValorFilipe(),
                liquidoVagner(r), liquidoFilipe(r));
    }

    /** Totais do topo da planilha. */
    public static ResumoResponse resumir(List<RegistroDiario> registros) {
        long minVagner = 0;
        long minFilipe = 0;
        int km = 0;
        BigDecimal brutoVagner = BigDecimal.ZERO;
        BigDecimal brutoFilipe = BigDecimal.ZERO;
        BigDecimal postoVagner = BigDecimal.ZERO;
        BigDecimal postoFilipe = BigDecimal.ZERO;
        BigDecimal liqVagner = BigDecimal.ZERO;
        BigDecimal liqFilipe = BigDecimal.ZERO;

        for (RegistroDiario r : registros) {
            minVagner += totalMinutosVagner(r);
            minFilipe += totalMinutosFilipe(r);
            km += totalKm(r);
            brutoVagner = brutoVagner.add(r.getValorVagner());
            brutoFilipe = brutoFilipe.add(r.getValorFilipe());
            postoVagner = postoVagner.add(r.getCargaPostoVagner());
            postoFilipe = postoFilipe.add(r.getCargaPostoFilipe());
            liqVagner = liqVagner.add(liquidoVagner(r));
            liqFilipe = liqFilipe.add(liquidoFilipe(r));
        }

        return new ResumoResponse(
                minVagner + minFilipe, minVagner, minFilipe,
                brutoVagner, brutoFilipe, brutoVagner.add(brutoFilipe),
                postoVagner, postoFilipe,
                liqVagner, liqFilipe, liqVagner.add(liqFilipe),
                km);
    }
}

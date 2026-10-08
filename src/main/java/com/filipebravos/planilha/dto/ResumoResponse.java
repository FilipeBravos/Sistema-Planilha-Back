package com.filipebravos.planilha.dto;

import java.math.BigDecimal;

/**
 * Totais exibidos acima da planilha. Horas em minutos (o front formata).
 */
public record ResumoResponse(
        long minutosTotal,
        long minutosVagner,
        long minutosFilipe,
        BigDecimal brutoVagner,
        BigDecimal brutoFilipe,
        BigDecimal brutoTotal,
        BigDecimal cargaPostoVagner,
        BigDecimal cargaPostoFilipe,
        BigDecimal liquidoVagner,
        BigDecimal liquidoFilipe,
        BigDecimal liquidoTotal,
        int kmVagner,
        int kmFilipe,
        int kmTotal) {
}

package com.filipebravos.planilha.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaResponse(
        Long id,
        CategoriaDespesa categoria,
        String nome,
        LocalDate data,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        Integer parcelas,
        BigDecimal valorParcela) {
}

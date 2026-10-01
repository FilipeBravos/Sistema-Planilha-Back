package com.filipebravos.planilha.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Um pagamento a vencer de uma despesa. Despesas fora do cartão têm um único vencimento (na data da
 * compra, sem número de parcela); no cartão há um vencimento por parcela, uma por mês.
 */
public record VencimentoResponse(
        Long despesaId,
        CategoriaDespesa categoria,
        String nome,
        FormaPagamento formaPagamento,
        LocalDate dataCompra,
        BigDecimal valorTotal,
        Integer parcelas,
        Integer numeroParcela,
        LocalDate vencimento,
        BigDecimal valor) {
}

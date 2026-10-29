package com.filipebravos.planilha.despesa;

import java.math.BigDecimal;
import java.util.List;

/**
 * Total do período, total por categoria (todas as categorias aparecem, mesmo com zero) e total por forma de
 * pagamento (com a quantidade de vencimentos, para a tela decidir se mostra o cartão da forma).
 */
public record DespesaResumoResponse(
        BigDecimal total,
        List<TotalCategoria> porCategoria,
        List<TotalForma> porFormaPagamento) {

    public record TotalCategoria(CategoriaDespesa categoria, BigDecimal total) {
    }

    public record TotalForma(FormaPagamento formaPagamento, BigDecimal total, int quantidade) {
    }
}

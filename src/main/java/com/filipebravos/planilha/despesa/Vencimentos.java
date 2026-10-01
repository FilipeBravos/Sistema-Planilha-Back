package com.filipebravos.planilha.despesa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera os vencimentos de uma despesa. No cartão, a 1ª parcela vence na data da compra e as demais
 * nos meses seguintes, no mesmo dia (ou no último dia do mês, quando ele não existe). A última parcela
 * absorve a diferença de arredondamento, então a soma das parcelas é sempre igual ao valor total.
 */
public final class Vencimentos {

    private Vencimentos() {
    }

    public static List<VencimentoResponse> de(Despesa d) {
        List<VencimentoResponse> lista = new ArrayList<>();
        if (d.getFormaPagamento() != FormaPagamento.CARTAO || d.getParcelas() == null) {
            lista.add(new VencimentoResponse(d.getId(), d.getCategoria(), d.getNome(), d.getFormaPagamento(),
                    d.getData(), d.getValor(), null, null, d.getData(), d.getValor()));
            return lista;
        }
        int n = d.getParcelas();
        BigDecimal parcela = d.getValor().divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 1; i <= n; i++) {
            BigDecimal valor = i < n ? parcela : d.getValor().subtract(acumulado);
            acumulado = acumulado.add(valor);
            lista.add(new VencimentoResponse(d.getId(), d.getCategoria(), d.getNome(), d.getFormaPagamento(),
                    d.getData(), d.getValor(), n, i, d.getData().plusMonths(i - 1L), valor));
        }
        return lista;
    }
}

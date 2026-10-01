package com.filipebravos.planilha.emprestimo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cronograma previsto de um empréstimo: uma parcela por mês, a 1ª um mês depois da data do empréstimo.
 * A última parcela absorve o arredondamento, então a soma é sempre o valor do empréstimo.
 */
public final class ParcelasEmprestimo {

    public record Parcela(int numero, LocalDate vencimento, BigDecimal valor) {
    }

    private ParcelasEmprestimo() {
    }

    public static List<Parcela> de(Emprestimo e) {
        int n = e.getParcelas();
        BigDecimal base = e.getValor().divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acumulado = BigDecimal.ZERO;
        List<Parcela> lista = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            BigDecimal valor = i < n ? base : e.getValor().subtract(acumulado);
            acumulado = acumulado.add(valor);
            lista.add(new Parcela(i, e.getData().plusMonths(i), valor));
        }
        return lista;
    }
}

package com.filipebravos.planilha.relatorio;

import com.filipebravos.planilha.despesa.CategoriaDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Dados do painel de relatórios para um intervalo de meses. */
public record RelatorioResponse(
        LocalDate inicio,
        LocalDate fim,
        List<Mes> meses,
        Mes totais,
        List<TotalCategoria> despesasPorCategoria,
        List<TotalItem> maioresDespesas,
        SituacaoEmprestimos emprestimos) {

    /**
     * Um mês (ou o total do intervalo, com {@code mes = "total"}).
     * Faturamento líquido = bruto − carga do posto; resultado = líquido − despesas − parcelas de empréstimos.
     */
    public record Mes(
            String mes,
            BigDecimal faturamentoBruto,
            BigDecimal cargaPosto,
            BigDecimal faturamentoLiquido,
            BigDecimal despesas,
            BigDecimal parcelasEmprestimos,
            BigDecimal resultado) {
    }

    public record TotalCategoria(CategoriaDespesa categoria, BigDecimal total, BigDecimal percentual) {
    }

    public record TotalItem(CategoriaDespesa categoria, String nome, BigDecimal total, BigDecimal percentual) {
    }

    /** Situação atual dos empréstimos (não depende do intervalo). */
    public record SituacaoEmprestimos(BigDecimal totalEmprestado, BigDecimal totalPago, BigDecimal saldoDevedor) {
    }
}

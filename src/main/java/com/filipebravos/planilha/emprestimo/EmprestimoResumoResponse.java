package com.filipebravos.planilha.emprestimo;

import java.math.BigDecimal;
import java.util.List;

/** Totais gerais e por credor (todos os credores aparecem, mesmo sem empréstimos). */
public record EmprestimoResumoResponse(
        BigDecimal totalEmprestado,
        BigDecimal totalPago,
        BigDecimal saldo,
        List<TotalCredor> porCredor) {

    public record TotalCredor(Credor credor, BigDecimal emprestado, BigDecimal pago, BigDecimal saldo) {
    }
}

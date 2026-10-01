package com.filipebravos.planilha.emprestimo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoResponse(
        Long id,
        Credor credor,
        String nomeTerceiro,
        LocalDate data,
        BigDecimal valor,
        int parcelas,
        BigDecimal valorParcela,
        BigDecimal valorPago,
        BigDecimal saldo,
        boolean quitado) {
}

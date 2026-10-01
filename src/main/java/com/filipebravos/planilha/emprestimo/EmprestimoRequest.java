package com.filipebravos.planilha.emprestimo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmprestimoRequest(
        @NotNull Credor credor,
        @Size(max = 120) String nomeTerceiro,
        @NotNull LocalDate data,
        @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero") BigDecimal valor,
        @NotNull @Min(1) @Max(600) Integer parcelas,
        @NotNull @PositiveOrZero BigDecimal valorPago) {

    @AssertTrue(message = "Informe o nome de quem emprestou")
    public boolean isNomeTerceiroValido() {
        return credor != Credor.TERCEIROS || (nomeTerceiro != null && !nomeTerceiro.isBlank());
    }
}

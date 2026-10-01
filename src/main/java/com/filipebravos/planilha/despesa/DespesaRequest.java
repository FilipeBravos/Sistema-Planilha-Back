package com.filipebravos.planilha.despesa;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaRequest(
        @NotNull CategoriaDespesa categoria,
        @NotBlank @Size(max = 120) String nome,
        @NotNull LocalDate data,
        @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero") BigDecimal valor,
        @NotNull FormaPagamento formaPagamento,
        @Min(1) @Max(60) Integer parcelas) {

    @AssertTrue(message = "Informe em quantas parcelas o cartão foi dividido")
    public boolean isParcelasValidas() {
        return formaPagamento != FormaPagamento.CARTAO || parcelas != null;
    }
}

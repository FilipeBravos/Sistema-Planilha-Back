package com.filipebravos.planilha.despesa;

public enum FormaPagamento {
    DINHEIRO,
    CARTAO,
    CHEQUE,
    BOLETO,
    CARTAO_VERONICA;

    /** Cartões e boleto podem ser divididos em parcelas, uma por mês. */
    public boolean parcelavel() {
        return this == CARTAO || this == BOLETO || this == CARTAO_VERONICA;
    }
}

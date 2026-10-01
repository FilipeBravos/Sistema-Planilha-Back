package com.filipebravos.planilha.despesa;

import java.math.BigDecimal;
import java.util.List;

/** Total do período e total por categoria (todas as categorias aparecem, mesmo com zero). */
public record DespesaResumoResponse(BigDecimal total, List<TotalCategoria> porCategoria) {

    public record TotalCategoria(CategoriaDespesa categoria, BigDecimal total) {
    }
}

package com.filipebravos.planilha;

import com.filipebravos.planilha.despesa.CategoriaDespesa;
import com.filipebravos.planilha.despesa.Despesa;
import com.filipebravos.planilha.despesa.FormaPagamento;
import com.filipebravos.planilha.despesa.VencimentoResponse;
import com.filipebravos.planilha.despesa.Vencimentos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VencimentosTest {

    private Despesa despesa(String data, String valor, FormaPagamento forma, Integer parcelas) {
        Despesa d = new Despesa();
        d.setCategoria(CategoriaDespesa.CARRO);
        d.setNome("PNEUS");
        d.setData(LocalDate.parse(data));
        d.setValor(new BigDecimal(valor));
        d.setFormaPagamento(forma);
        d.setParcelas(parcelas);
        return d;
    }

    @Test
    void foraDoCartaoTemUmUnicoVencimentoNaDataDaCompra() {
        List<VencimentoResponse> v = Vencimentos.de(despesa("2026-10-10", "250.00", FormaPagamento.BOLETO, null));
        assertEquals(1, v.size());
        assertEquals(LocalDate.parse("2026-10-10"), v.get(0).vencimento());
        assertEquals(new BigDecimal("250.00"), v.get(0).valor());
        assertNull(v.get(0).numeroParcela());
    }

    @Test
    void cartaoGeraUmaParcelaPorMesComecandoNaCompra() {
        List<VencimentoResponse> v = Vencimentos.de(despesa("2026-10-02", "900.00", FormaPagamento.CARTAO, 3));
        assertEquals(3, v.size());
        assertEquals(LocalDate.parse("2026-10-02"), v.get(0).vencimento());
        assertEquals(LocalDate.parse("2026-11-02"), v.get(1).vencimento());
        assertEquals(LocalDate.parse("2026-12-02"), v.get(2).vencimento());
        assertEquals(3, v.get(2).numeroParcela());
        assertEquals(new BigDecimal("300.00"), v.get(1).valor());
    }

    @Test
    void ultimaParcelaAbsorveOArredondamento() {
        List<VencimentoResponse> v = Vencimentos.de(despesa("2026-10-02", "200.00", FormaPagamento.CARTAO, 3));
        assertEquals(new BigDecimal("66.67"), v.get(0).valor());
        assertEquals(new BigDecimal("66.67"), v.get(1).valor());
        assertEquals(new BigDecimal("66.66"), v.get(2).valor());
        BigDecimal soma = v.stream().map(VencimentoResponse::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("200.00"), soma);
    }

    @Test
    void diaInexistenteNoMesVaiParaOUltimoDia() {
        List<VencimentoResponse> v = Vencimentos.de(despesa("2026-01-31", "300.00", FormaPagamento.CARTAO, 3));
        assertEquals(LocalDate.parse("2026-02-28"), v.get(1).vencimento());
        assertEquals(LocalDate.parse("2026-03-31"), v.get(2).vencimento());
    }
}

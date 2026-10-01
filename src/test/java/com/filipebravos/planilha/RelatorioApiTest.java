package com.filipebravos.planilha;

import com.filipebravos.planilha.despesa.CategoriaDespesa;
import com.filipebravos.planilha.despesa.Despesa;
import com.filipebravos.planilha.despesa.DespesaRepository;
import com.filipebravos.planilha.despesa.FormaPagamento;
import com.filipebravos.planilha.emprestimo.Credor;
import com.filipebravos.planilha.emprestimo.Emprestimo;
import com.filipebravos.planilha.emprestimo.EmprestimoRepository;
import com.filipebravos.planilha.emprestimo.ParcelasEmprestimo;
import com.filipebravos.planilha.model.RegistroDiario;
import com.filipebravos.planilha.repository.RegistroDiarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(AutenticadoTestConfig.class)
@AutoConfigureMockMvc
class RelatorioApiTest {

    @Autowired MockMvc mvc;
    @Autowired RegistroDiarioRepository registros;
    @Autowired DespesaRepository despesas;
    @Autowired EmprestimoRepository emprestimos;

    @BeforeEach
    void preparar() {
        limpar();

        RegistroDiario r = new RegistroDiario();
        r.setData(LocalDate.parse("2026-09-10"));
        r.setHoraInicialVagner(LocalTime.of(8, 0));
        r.setHoraFinalVagner(LocalTime.of(12, 0));
        r.setKmInicial(0);
        r.setKmFinal(10);
        r.setValorVagner(new BigDecimal("300"));
        r.setCargaPostoVagner(new BigDecimal("50"));
        r.setValorFilipe(new BigDecimal("200"));
        r.setCargaPostoFilipe(new BigDecimal("20"));
        registros.save(r);

        despesas.save(despesa("GASOLINA", "2026-09-05", "100", FormaPagamento.DINHEIRO, null));
        despesas.save(despesa("pneus ", "2026-09-15", "900", FormaPagamento.CARTAO, 3));

        Emprestimo e = new Emprestimo();
        e.setCredor(Credor.BANCO_ITAU);
        e.setData(LocalDate.parse("2026-08-20"));
        e.setValor(new BigDecimal("1200"));
        e.setParcelas(4);
        e.setValorPago(new BigDecimal("400"));
        emprestimos.save(e);
    }

    @AfterEach
    void limpar() {
        registros.deleteAll();
        despesas.deleteAll();
        emprestimos.deleteAll();
    }

    private Despesa despesa(String nome, String data, String valor, FormaPagamento forma, Integer parcelas) {
        Despesa d = new Despesa();
        d.setCategoria(CategoriaDespesa.CARRO);
        d.setNome(nome);
        d.setData(LocalDate.parse(data));
        d.setValor(new BigDecimal(valor));
        d.setFormaPagamento(forma);
        d.setParcelas(parcelas);
        return d;
    }

    @Test
    void consolidaReceitaDespesasEEmprestimosPorMes() throws Exception {
        mvc.perform(get("/api/relatorios/dashboard").param("inicio", "2026-09-01").param("fim", "2026-11-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value("2026-09-01"))
                .andExpect(jsonPath("$.fim").value("2026-11-30"))
                .andExpect(jsonPath("$.meses.length()").value(3))
                .andExpect(jsonPath("$.meses[0].mes").value("2026-09"))
                .andExpect(jsonPath("$.meses[0].faturamentoBruto").value(500.0))
                .andExpect(jsonPath("$.meses[0].cargaPosto").value(70.0))
                .andExpect(jsonPath("$.meses[0].faturamentoLiquido").value(430.0))
                .andExpect(jsonPath("$.meses[0].despesas").value(400.0))
                .andExpect(jsonPath("$.meses[0].parcelasEmprestimos").value(300.0))
                .andExpect(jsonPath("$.meses[0].resultado").value(-270.0))
                .andExpect(jsonPath("$.meses[1].faturamentoLiquido").value(0))
                .andExpect(jsonPath("$.meses[1].despesas").value(300.0))
                .andExpect(jsonPath("$.meses[2].resultado").value(-600.0))
                .andExpect(jsonPath("$.totais.mes").value("total"))
                .andExpect(jsonPath("$.totais.despesas").value(1000.0))
                .andExpect(jsonPath("$.totais.parcelasEmprestimos").value(900.0))
                .andExpect(jsonPath("$.totais.resultado").value(-1470.0));
    }

    @Test
    void rankingDeDespesasEmprestimosAtuais() throws Exception {
        mvc.perform(get("/api/relatorios/dashboard").param("inicio", "2026-09-01").param("fim", "2026-11-30"))
                .andExpect(jsonPath("$.despesasPorCategoria.length()").value(1))
                .andExpect(jsonPath("$.despesasPorCategoria[0].categoria").value("CARRO"))
                .andExpect(jsonPath("$.despesasPorCategoria[0].percentual").value(100.0))
                .andExpect(jsonPath("$.maioresDespesas[0].nome").value("PNEUS"))
                .andExpect(jsonPath("$.maioresDespesas[0].total").value(900.0))
                .andExpect(jsonPath("$.maioresDespesas[0].percentual").value(90.0))
                .andExpect(jsonPath("$.maioresDespesas[1].nome").value("GASOLINA"))
                .andExpect(jsonPath("$.emprestimos.totalEmprestado").value(1200.0))
                .andExpect(jsonPath("$.emprestimos.totalPago").value(400.0))
                .andExpect(jsonPath("$.emprestimos.saldoDevedor").value(800.0));
    }

    @Test
    void rejeitaIntervaloInvalido() throws Exception {
        mvc.perform(get("/api/relatorios/dashboard").param("inicio", "2026-10-01").param("fim", "2026-09-30"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/dashboard").param("inicio", "2000-01-01").param("fim", "2026-09-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semParametrosUsaOsUltimos12Meses() throws Exception {
        mvc.perform(get("/api/relatorios/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses.length()").value(12));
    }

    @Test
    void parcelasDoEmprestimoSomamOValorETemUmMesDeCarencia() {
        Emprestimo e = emprestimos.findAll().get(0);
        e.setValor(new BigDecimal("1000"));
        e.setParcelas(3);
        List<ParcelasEmprestimo.Parcela> p = ParcelasEmprestimo.de(e);
        assertEquals(LocalDate.parse("2026-09-20"), p.get(0).vencimento());
        assertEquals(LocalDate.parse("2026-11-20"), p.get(2).vencimento());
        assertEquals(new BigDecimal("333.33"), p.get(0).valor());
        assertEquals(new BigDecimal("333.34"), p.get(2).valor());
        assertEquals(new BigDecimal("1000.00"), p.stream().map(ParcelasEmprestimo.Parcela::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}

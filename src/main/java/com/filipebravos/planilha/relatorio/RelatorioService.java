package com.filipebravos.planilha.relatorio;

import com.filipebravos.planilha.despesa.CategoriaDespesa;
import com.filipebravos.planilha.despesa.DespesaService;
import com.filipebravos.planilha.despesa.VencimentoResponse;
import com.filipebravos.planilha.emprestimo.Emprestimo;
import com.filipebravos.planilha.emprestimo.EmprestimoRepository;
import com.filipebravos.planilha.emprestimo.ParcelasEmprestimo;
import com.filipebravos.planilha.model.RegistroDiario;
import com.filipebravos.planilha.relatorio.RelatorioResponse.Mes;
import com.filipebravos.planilha.repository.RegistroDiarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RelatorioService {

    static final int MAX_MESES = 120;
    static final int TOP_ITENS = 10;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final RegistroDiarioRepository registros;
    private final DespesaService despesas;
    private final EmprestimoRepository emprestimos;

    public RelatorioService(RegistroDiarioRepository registros, DespesaService despesas,
                            EmprestimoRepository emprestimos) {
        this.registros = registros;
        this.despesas = despesas;
        this.emprestimos = emprestimos;
    }

    /** Sem datas informadas, usa os últimos 12 meses terminando no mês atual. */
    @Transactional(readOnly = true)
    public RelatorioResponse gerar(LocalDate inicioInformado, LocalDate fimInformado) {
        YearMonth fimMes = fimInformado != null ? YearMonth.from(fimInformado) : YearMonth.now();
        YearMonth inicioMes = inicioInformado != null ? YearMonth.from(inicioInformado) : fimMes.minusMonths(11);
        if (inicioMes.isAfter(fimMes)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial é depois da final");
        }
        if (inicioMes.until(fimMes, java.time.temporal.ChronoUnit.MONTHS) + 1 > MAX_MESES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período máximo de " + MAX_MESES + " meses");
        }
        LocalDate inicio = inicioMes.atDay(1);
        LocalDate fim = fimMes.atEndOfMonth();

        Map<YearMonth, Acumulador> porMes = new LinkedHashMap<>();
        for (YearMonth m = inicioMes; !m.isAfter(fimMes); m = m.plusMonths(1)) {
            porMes.put(m, new Acumulador());
        }

        for (RegistroDiario r : registros.findByDataBetweenOrderByDataAscIdAsc(inicio, fim)) {
            Acumulador a = porMes.get(YearMonth.from(r.getData()));
            a.bruto = a.bruto.add(r.getValorVagner()).add(r.getValorFilipe());
            a.carga = a.carga.add(r.getCargaPostoVagner()).add(r.getCargaPostoFilipe());
        }

        List<VencimentoResponse> vencimentos = despesas.listar(null, inicio, fim);
        for (VencimentoResponse v : vencimentos) {
            Acumulador a = porMes.get(YearMonth.from(v.vencimento()));
            a.despesas = a.despesas.add(v.valor());
        }

        List<Emprestimo> todosEmprestimos = emprestimos.findAllByOrderByDataAscIdAsc();
        for (Emprestimo e : todosEmprestimos) {
            for (ParcelasEmprestimo.Parcela p : ParcelasEmprestimo.de(e)) {
                Acumulador a = porMes.get(YearMonth.from(p.vencimento()));
                if (a != null) {
                    a.emprestimos = a.emprestimos.add(p.valor());
                }
            }
        }

        List<Mes> meses = new ArrayList<>();
        Acumulador total = new Acumulador();
        porMes.forEach((m, a) -> {
            meses.add(a.paraMes(m.toString()));
            total.somar(a);
        });

        return new RelatorioResponse(inicio, fim, meses, total.paraMes("total"),
                porCategoria(vencimentos, total.despesas), maioresItens(vencimentos, total.despesas),
                situacaoEmprestimos(todosEmprestimos));
    }

    private static List<RelatorioResponse.TotalCategoria> porCategoria(List<VencimentoResponse> lista, BigDecimal total) {
        Map<CategoriaDespesa, BigDecimal> somas = new LinkedHashMap<>();
        lista.forEach(v -> somas.merge(v.categoria(), v.valor(), BigDecimal::add));
        return somas.entrySet().stream()
                .sorted(Map.Entry.<CategoriaDespesa, BigDecimal>comparingByValue().reversed())
                .map(e -> new RelatorioResponse.TotalCategoria(e.getKey(), e.getValue(), percentual(e.getValue(), total)))
                .toList();
    }

    /** Agrupa pelo nome (sem diferenciar maiúsculas) dentro de cada categoria. */
    private static List<RelatorioResponse.TotalItem> maioresItens(List<VencimentoResponse> lista, BigDecimal total) {
        record Chave(CategoriaDespesa categoria, String nome) {
        }
        Map<Chave, BigDecimal> somas = new LinkedHashMap<>();
        lista.forEach(v -> somas.merge(new Chave(v.categoria(), v.nome().trim().toUpperCase(Locale.ROOT)),
                v.valor(), BigDecimal::add));
        return somas.entrySet().stream()
                .sorted(Map.Entry.<Chave, BigDecimal>comparingByValue().reversed()
                        .thenComparing(e -> e.getKey().nome(), Comparator.naturalOrder()))
                .limit(TOP_ITENS)
                .map(e -> new RelatorioResponse.TotalItem(e.getKey().categoria(), e.getKey().nome(), e.getValue(),
                        percentual(e.getValue(), total)))
                .toList();
    }

    private static RelatorioResponse.SituacaoEmprestimos situacaoEmprestimos(List<Emprestimo> lista) {
        BigDecimal emprestado = BigDecimal.ZERO;
        BigDecimal pago = BigDecimal.ZERO;
        BigDecimal saldo = BigDecimal.ZERO;
        for (Emprestimo e : lista) {
            emprestado = emprestado.add(e.getValor());
            pago = pago.add(e.getValorPago());
            saldo = saldo.add(e.getValor().subtract(e.getValorPago()).max(BigDecimal.ZERO));
        }
        return new RelatorioResponse.SituacaoEmprestimos(emprestado, pago, saldo);
    }

    static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }

    private static final class Acumulador {
        BigDecimal bruto = BigDecimal.ZERO;
        BigDecimal carga = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        BigDecimal emprestimos = BigDecimal.ZERO;

        void somar(Acumulador o) {
            bruto = bruto.add(o.bruto);
            carga = carga.add(o.carga);
            despesas = despesas.add(o.despesas);
            emprestimos = emprestimos.add(o.emprestimos);
        }

        Mes paraMes(String rotulo) {
            BigDecimal liquido = bruto.subtract(carga);
            return new Mes(rotulo, bruto, carga, liquido, despesas, emprestimos,
                    liquido.subtract(despesas).subtract(emprestimos));
        }
    }
}

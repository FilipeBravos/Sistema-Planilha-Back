package com.filipebravos.planilha.despesa;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DespesaService {

    static final int MAX_PARCELAS = 60;

    private final DespesaRepository repository;

    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    /** Vencimentos que caem no período (compras parceladas podem aparecer em vários meses). */
    @Transactional(readOnly = true)
    public List<VencimentoResponse> listar(CategoriaDespesa categoria, LocalDate inicio, LocalDate fim) {
        return buscar(categoria, inicio, fim);
    }

    /** Total e total por categoria dos vencimentos do período; ignora filtro de categoria. */
    @Transactional(readOnly = true)
    public DespesaResumoResponse resumo(LocalDate inicio, LocalDate fim) {
        Map<CategoriaDespesa, BigDecimal> totais = new EnumMap<>(CategoriaDespesa.class);
        Arrays.stream(CategoriaDespesa.values()).forEach(c -> totais.put(c, BigDecimal.ZERO));
        BigDecimal total = BigDecimal.ZERO;
        for (VencimentoResponse v : buscar(null, inicio, fim)) {
            totais.merge(v.categoria(), v.valor(), BigDecimal::add);
            total = total.add(v.valor());
        }
        List<DespesaResumoResponse.TotalCategoria> porCategoria = totais.entrySet().stream()
                .map(e -> new DespesaResumoResponse.TotalCategoria(e.getKey(), e.getValue()))
                .toList();
        return new DespesaResumoResponse(total, porCategoria);
    }

    @Transactional
    public DespesaResponse criar(DespesaRequest request) {
        return toResponse(repository.save(aplicar(new Despesa(), request)));
    }

    @Transactional
    public DespesaResponse atualizar(Long id, DespesaRequest request) {
        Despesa despesa = repository.findById(id).orElseThrow(() -> naoEncontrada(id));
        return toResponse(repository.save(aplicar(despesa, request)));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw naoEncontrada(id);
        }
        repository.deleteById(id);
    }

    /** Valor de cada parcela; só existe para cartão. */
    static BigDecimal valorParcela(Despesa d) {
        if (d.getFormaPagamento() != FormaPagamento.CARTAO || d.getParcelas() == null) {
            return null;
        }
        return d.getValor().divide(BigDecimal.valueOf(d.getParcelas()), 2, RoundingMode.HALF_UP);
    }

    static DespesaResponse toResponse(Despesa d) {
        return new DespesaResponse(d.getId(), d.getCategoria(), d.getNome(), d.getData(), d.getValor(),
                d.getFormaPagamento(), d.getParcelas(), valorParcela(d));
    }

    private List<VencimentoResponse> buscar(CategoriaDespesa categoria, LocalDate inicio, LocalDate fim) {
        // Uma compra com a 1ª parcela até MAX_PARCELAS - 1 meses antes do início ainda pode vencer dentro do período.
        LocalDate compraMinima = inicio == null ? null : inicio.minusMonths(MAX_PARCELAS - 1L);
        Specification<Despesa> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (categoria != null) {
                condicoes.add(cb.equal(root.get("categoria"), categoria));
            }
            if (compraMinima != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("data"), compraMinima));
            }
            if (fim != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.get("data"), fim));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
        return repository.findAll(filtro).stream()
                .flatMap(d -> Vencimentos.de(d).stream())
                .filter(v -> (inicio == null || !v.vencimento().isBefore(inicio))
                        && (fim == null || !v.vencimento().isAfter(fim)))
                .sorted(Comparator.comparing(VencimentoResponse::vencimento)
                        .thenComparing(VencimentoResponse::despesaId)
                        .thenComparing(v -> v.numeroParcela() == null ? 0 : v.numeroParcela()))
                .toList();
    }

    private Despesa aplicar(Despesa d, DespesaRequest req) {
        d.setCategoria(req.categoria());
        d.setNome(req.nome().trim());
        d.setData(req.data());
        d.setValor(req.valor());
        d.setFormaPagamento(req.formaPagamento());
        d.setParcelas(req.formaPagamento() == FormaPagamento.CARTAO ? req.parcelas() : null);
        return d;
    }

    private ResponseStatusException naoEncontrada(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa " + id + " não encontrada");
    }
}

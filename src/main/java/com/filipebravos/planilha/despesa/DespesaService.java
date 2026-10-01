package com.filipebravos.planilha.despesa;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DespesaService {

    private final DespesaRepository repository;

    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listar(CategoriaDespesa categoria, LocalDate inicio, LocalDate fim) {
        return buscar(categoria, inicio, fim).stream().map(DespesaService::toResponse).toList();
    }

    /** O resumo ignora o filtro de categoria: mostra todas, para comparação. */
    @Transactional(readOnly = true)
    public DespesaResumoResponse resumo(LocalDate inicio, LocalDate fim) {
        Map<CategoriaDespesa, BigDecimal> totais = new EnumMap<>(CategoriaDespesa.class);
        Arrays.stream(CategoriaDespesa.values()).forEach(c -> totais.put(c, BigDecimal.ZERO));
        BigDecimal total = BigDecimal.ZERO;
        for (Despesa d : buscar(null, inicio, fim)) {
            totais.merge(d.getCategoria(), d.getValor(), BigDecimal::add);
            total = total.add(d.getValor());
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

    private List<Despesa> buscar(CategoriaDespesa categoria, LocalDate inicio, LocalDate fim) {
        Specification<Despesa> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (categoria != null) {
                condicoes.add(cb.equal(root.get("categoria"), categoria));
            }
            if (inicio != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("data"), inicio));
            }
            if (fim != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.get("data"), fim));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
        return repository.findAll(filtro, Sort.by("data", "id"));
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

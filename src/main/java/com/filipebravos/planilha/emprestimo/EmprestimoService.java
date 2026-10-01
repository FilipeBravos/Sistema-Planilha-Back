package com.filipebravos.planilha.emprestimo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;

    public EmprestimoService(EmprestimoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listar(Credor credor) {
        List<Emprestimo> lista = credor == null
                ? repository.findAllByOrderByDataAscIdAsc()
                : repository.findByCredorOrderByDataAscIdAsc(credor);
        return lista.stream().map(EmprestimoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmprestimoResumoResponse resumo() {
        List<Emprestimo> todos = repository.findAllByOrderByDataAscIdAsc();
        List<EmprestimoResumoResponse.TotalCredor> porCredor = new ArrayList<>();
        for (Credor c : Credor.values()) {
            List<Emprestimo> doCredor = todos.stream().filter(e -> e.getCredor() == c).toList();
            porCredor.add(new EmprestimoResumoResponse.TotalCredor(c, somar(doCredor, Campo.VALOR),
                    somar(doCredor, Campo.PAGO), somar(doCredor, Campo.SALDO)));
        }
        return new EmprestimoResumoResponse(somar(todos, Campo.VALOR), somar(todos, Campo.PAGO),
                somar(todos, Campo.SALDO), porCredor);
    }

    @Transactional
    public EmprestimoResponse criar(EmprestimoRequest request) {
        return toResponse(repository.save(aplicar(new Emprestimo(), request)));
    }

    @Transactional
    public EmprestimoResponse atualizar(Long id, EmprestimoRequest request) {
        Emprestimo e = repository.findById(id).orElseThrow(() -> naoEncontrado(id));
        return toResponse(repository.save(aplicar(e, request)));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw naoEncontrado(id);
        }
        repository.deleteById(id);
    }

    /** Quanto falta pagar; nunca negativo (pagar além do valor, por juros, não gera crédito). */
    static BigDecimal saldo(Emprestimo e) {
        return e.getValor().subtract(e.getValorPago()).max(BigDecimal.ZERO);
    }

    static EmprestimoResponse toResponse(Emprestimo e) {
        BigDecimal saldo = saldo(e);
        BigDecimal valorParcela = e.getValor().divide(BigDecimal.valueOf(e.getParcelas()), 2, RoundingMode.HALF_UP);
        return new EmprestimoResponse(e.getId(), e.getCredor(), e.getNomeTerceiro(), e.getData(), e.getValor(),
                e.getParcelas(), valorParcela, e.getValorPago(), saldo, saldo.signum() == 0);
    }

    private enum Campo { VALOR, PAGO, SALDO }

    private static BigDecimal somar(List<Emprestimo> lista, Campo campo) {
        return lista.stream()
                .map(e -> switch (campo) {
                    case VALOR -> e.getValor();
                    case PAGO -> e.getValorPago();
                    case SALDO -> saldo(e);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Emprestimo aplicar(Emprestimo e, EmprestimoRequest req) {
        e.setCredor(req.credor());
        e.setNomeTerceiro(req.credor() == Credor.TERCEIROS ? req.nomeTerceiro().trim() : null);
        e.setData(req.data());
        e.setValor(req.valor());
        e.setParcelas(req.parcelas());
        e.setValorPago(req.valorPago());
        return e;
    }

    private ResponseStatusException naoEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Empréstimo " + id + " não encontrado");
    }
}

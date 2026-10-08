package com.filipebravos.planilha.service;

import com.filipebravos.planilha.dto.RegistroRequest;
import com.filipebravos.planilha.dto.RegistroResponse;
import com.filipebravos.planilha.dto.ResumoResponse;
import com.filipebravos.planilha.model.RegistroDiario;
import com.filipebravos.planilha.repository.RegistroDiarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistroService {

    private final RegistroDiarioRepository repository;

    public RegistroService(RegistroDiarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegistroResponse> listar(LocalDate inicio, LocalDate fim) {
        return buscar(inicio, fim).stream().map(CalculoRegistro::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumo(LocalDate inicio, LocalDate fim) {
        return CalculoRegistro.resumir(buscar(inicio, fim));
    }

    @Transactional
    public RegistroResponse criar(RegistroRequest request) {
        return CalculoRegistro.toResponse(repository.save(aplicar(new RegistroDiario(), request)));
    }

    @Transactional
    public RegistroResponse atualizar(Long id, RegistroRequest request) {
        RegistroDiario registro = repository.findById(id).orElseThrow(() -> naoEncontrado(id));
        return CalculoRegistro.toResponse(repository.save(aplicar(registro, request)));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw naoEncontrado(id);
        }
        repository.deleteById(id);
    }

    private List<RegistroDiario> buscar(LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return repository.findAllByOrderByDataAscIdAsc();
        }
        if (fim == null) {
            return repository.findByDataGreaterThanEqualOrderByDataAscIdAsc(inicio);
        }
        if (inicio == null) {
            return repository.findByDataLessThanEqualOrderByDataAscIdAsc(fim);
        }
        return repository.findByDataBetweenOrderByDataAscIdAsc(inicio, fim);
    }

    private RegistroDiario aplicar(RegistroDiario r, RegistroRequest req) {
        r.setData(req.data());
        r.setHoraInicialVagner(req.horaInicialVagner());
        r.setHoraFinalVagner(req.horaFinalVagner());
        r.setHoraInicialFilipe(req.horaInicialFilipe());
        r.setHoraFinalFilipe(req.horaFinalFilipe());
        r.setKmInicialVagner(req.kmInicialVagner());
        r.setKmFinalVagner(req.kmFinalVagner());
        r.setKmInicialFilipe(req.kmInicialFilipe());
        r.setKmFinalFilipe(req.kmFinalFilipe());
        r.setCargaPostoVagner(req.cargaPostoVagner());
        r.setCargaPostoFilipe(req.cargaPostoFilipe());
        r.setValorVagner(req.valorVagner());
        r.setValorFilipe(req.valorFilipe());
        return r;
    }

    private ResponseStatusException naoEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro " + id + " não encontrado");
    }
}

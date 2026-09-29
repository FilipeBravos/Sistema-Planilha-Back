package com.filipebravos.planilha.controller;

import com.filipebravos.planilha.dto.RegistroRequest;
import com.filipebravos.planilha.dto.RegistroResponse;
import com.filipebravos.planilha.dto.ResumoResponse;
import com.filipebravos.planilha.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistroController {

    private final RegistroService service;

    public RegistroController(RegistroService service) {
        this.service = service;
    }

    @GetMapping("/registros")
    public List<RegistroResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.listar(inicio, fim);
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.resumo(inicio, fim);
    }

    @PostMapping("/registros")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse criar(@Valid @RequestBody RegistroRequest request) {
        return service.criar(request);
    }

    @PutMapping("/registros/{id}")
    public RegistroResponse atualizar(@PathVariable Long id, @Valid @RequestBody RegistroRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/registros/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

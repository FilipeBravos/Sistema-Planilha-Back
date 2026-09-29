package com.filipebravos.planilha.repository;

import com.filipebravos.planilha.model.RegistroDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroDiarioRepository extends JpaRepository<RegistroDiario, Long> {

    List<RegistroDiario> findAllByOrderByDataAscHoraInicialAsc();

    List<RegistroDiario> findByDataBetweenOrderByDataAscHoraInicialAsc(LocalDate inicio, LocalDate fim);
}

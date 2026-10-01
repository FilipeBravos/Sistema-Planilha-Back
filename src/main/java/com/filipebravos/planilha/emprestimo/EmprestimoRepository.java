package com.filipebravos.planilha.emprestimo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findAllByOrderByDataAscIdAsc();

    List<Emprestimo> findByCredorOrderByDataAscIdAsc(Credor credor);
}

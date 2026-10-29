package com.filipebravos.planilha.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ajustes que o {@code ddl-auto=update} do Hibernate não faz em bancos criados por versões antigas:
 * <ul>
 *   <li>colunas km_inicial/km_final (Km único, antes de ser separado por pessoa) eram NOT NULL; sem relaxar,
 *       os novos registros (que não preenchem mais essas colunas) seriam recusados;</li>
 *   <li>a coluna forma_pagamento tem uma restrição (CHECK) com a lista de formas da época de sua criação;
 *       sem removê-la, formas novas (como Cartão Verônica) seriam recusadas.</li>
 * </ul>
 * É seguro de repetir: se a coluna ou a restrição não existe, ou já foi ajustada, nada muda.
 */
@Component
public class AjusteDeEsquema implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AjusteDeEsquema.class);
    private static final List<String> AJUSTES = List.of(
            "ALTER TABLE registro_diario ALTER COLUMN km_inicial DROP NOT NULL",
            "ALTER TABLE registro_diario ALTER COLUMN km_final DROP NOT NULL",
            "ALTER TABLE despesa DROP CONSTRAINT IF EXISTS despesa_forma_pagamento_check");

    private final JdbcTemplate jdbc;

    public AjusteDeEsquema(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String sql : AJUSTES) {
            try {
                jdbc.execute(sql);
            } catch (DataAccessException e) {
                log.warn("Ajuste de esquema não aplicado ({}): {}", sql, e.getMostSpecificCause().getMessage());
            }
        }
    }
}

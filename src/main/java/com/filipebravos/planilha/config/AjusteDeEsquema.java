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
 * Bancos antigos têm as colunas km_inicial/km_final (Km único, antes de ser separado por pessoa) como
 * NOT NULL, e o {@code ddl-auto=update} do Hibernate nem remove nem relaxa essas colunas. Sem este ajuste,
 * os novos registros (que não preenchem mais essas colunas) seriam recusados. É seguro de repetir: se a
 * coluna não existe ou já aceita vazio, nada muda.
 */
@Component
public class AjusteDeEsquema implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AjusteDeEsquema.class);
    private static final List<String> COLUNAS_OPCIONAIS = List.of("km_inicial", "km_final");

    private final JdbcTemplate jdbc;

    public AjusteDeEsquema(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String coluna : COLUNAS_OPCIONAIS) {
            try {
                jdbc.execute("ALTER TABLE registro_diario ALTER COLUMN " + coluna + " DROP NOT NULL");
            } catch (DataAccessException e) {
                log.warn("Não consegui permitir valor vazio em registro_diario.{}: {}", coluna, e.getMostSpecificCause().getMessage());
            }
        }
    }
}

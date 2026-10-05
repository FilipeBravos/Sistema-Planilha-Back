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
 * Bancos criados antes de o Km virar opcional têm as colunas como NOT NULL, e o {@code ddl-auto=update}
 * do Hibernate não relaxa essa restrição. Este ajuste é seguro de repetir: se a coluna já aceita vazio,
 * nada muda.
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

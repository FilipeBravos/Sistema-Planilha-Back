package com.filipebravos.planilha;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** Faz toda requisição dos testes de negócio chegar já logada e com token CSRF. */
@TestConfiguration
public class AutenticadoTestConfig {

    @Bean
    MockMvcBuilderCustomizer autenticado() {
        return builder -> builder.defaultRequest(get("/").with(user("teste")).with(csrf()));
    }
}

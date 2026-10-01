package com.filipebravos.planilha.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Cria os usuários informados em {@code app.usuarios-iniciais} ({@code login:senha,login2:senha2}) que ainda
 * não existem. Nunca altera a senha de quem já existe: depois do primeiro acesso, cada um troca a sua pela tela.
 */
@Component
public class UsuariosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciais.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final String configurados;

    public UsuariosIniciais(UsuarioRepository repository, PasswordEncoder encoder,
                            @Value("${app.usuarios-iniciais:}") String configurados) {
        this.repository = repository;
        this.encoder = encoder;
        this.configurados = configurados;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String item : configurados.split(",")) {
            if (item.isBlank()) {
                continue;
            }
            int separador = item.indexOf(':');
            String login = separador < 0 ? "" : item.substring(0, separador).trim().toLowerCase(Locale.ROOT);
            String senha = separador < 0 ? "" : item.substring(separador + 1);
            if (login.isEmpty() || senha.length() < AuthController.TAMANHO_MINIMO_SENHA) {
                log.error("APP_USUARIOS_INICIAIS: entrada inválida (use login:senha, senha com 8+ caracteres); ignorada.");
                continue;
            }
            if (repository.findByLogin(login).isEmpty()) {
                repository.save(new Usuario(login, encoder.encode(senha)));
                log.info("Usuário '{}' criado.", login);
            }
        }
        if (repository.count() == 0) {
            log.warn("Nenhum usuário cadastrado: ninguém consegue entrar. Defina APP_USUARIOS_INICIAIS (login:senha,...).");
        }
    }
}

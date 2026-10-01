package com.filipebravos.planilha.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    static final int TAMANHO_MINIMO_SENHA = 8;

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final LimiteTentativas limite;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
                          PasswordEncoder encoder, LimiteTentativas limite) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.limite = limite;
    }

    public record LoginRequest(@NotBlank @Size(max = 60) String usuario, @NotBlank @Size(max = 200) String senha) {
    }

    public record TrocaSenhaRequest(@NotBlank @Size(max = 200) String senhaAtual,
                                    @NotBlank @Size(min = TAMANHO_MINIMO_SENHA, max = 200,
                                            message = "A nova senha precisa ter pelo menos 8 caracteres") String novaSenha) {
    }

    /** Endpoint público de verificação (usado pelo healthcheck do servidor). */
    @GetMapping("/saude")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> entrar(@Valid @RequestBody LoginRequest req,
                                                      HttpServletRequest request, HttpServletResponse response) {
        String login = req.usuario().trim().toLowerCase(Locale.ROOT);
        String chave = request.getRemoteAddr() + "|" + login;
        if (limite.bloqueado(chave)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("mensagem", "Muitas tentativas. Aguarde alguns minutos e tente de novo."));
        }
        Authentication autenticado;
        try {
            autenticado = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login, req.senha()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            limite.registrarFalha(chave);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Usuário ou senha inválidos."));
        }
        limite.limpar(chave);
        if (request.getSession(false) != null) {
            request.changeSessionId(); // evita fixação de sessão
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        contextRepository.saveContext(contexto, request, response);
        return ResponseEntity.ok(Map.of("usuario", autenticado.getName()));
    }

    /** Quem está logado (401 se ninguém). */
    @GetMapping("/auth/eu")
    public Map<String, String> eu(Authentication autenticado) {
        return Map.of("usuario", autenticado.getName());
    }

    @PostMapping("/auth/senha")
    @Transactional
    public ResponseEntity<Map<String, String>> trocarSenha(@Valid @RequestBody TrocaSenhaRequest req, Authentication autenticado) {
        Usuario u = usuarios.findByLogin(autenticado.getName())
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));
        if (!encoder.matches(req.senhaAtual(), u.getSenhaHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", "A senha atual está incorreta."));
        }
        if (req.novaSenha().equals(req.senhaAtual())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", "A nova senha precisa ser diferente da atual."));
        }
        u.setSenhaHash(encoder.encode(req.novaSenha()));
        usuarios.save(u);
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada."));
    }
}

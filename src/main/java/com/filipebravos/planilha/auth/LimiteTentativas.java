package com.filipebravos.planilha.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freia tentativas de adivinhar senha: depois de {@value #MAXIMO} erros seguidos da mesma origem e login,
 * bloqueia novas tentativas por {@link #BLOQUEIO}. O contador fica em memória (zera se o servidor reiniciar).
 */
@Component
public class LimiteTentativas {

    static final int MAXIMO = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private record Estado(int falhas, Instant desde) {
    }

    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();

    public boolean bloqueado(String chave) {
        Estado e = estados.get(chave);
        if (e == null) {
            return false;
        }
        if (Instant.now().isAfter(e.desde().plus(BLOQUEIO))) {
            estados.remove(chave);
            return false;
        }
        return e.falhas() >= MAXIMO;
    }

    public void registrarFalha(String chave) {
        Instant agora = Instant.now();
        estados.merge(chave, new Estado(1, agora), (atual, novo) ->
                agora.isAfter(atual.desde().plus(BLOQUEIO)) ? novo : new Estado(atual.falhas() + 1, atual.desde()));
    }

    public void limpar(String chave) {
        estados.remove(chave);
    }
}

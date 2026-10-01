package com.filipebravos.planilha;

import com.filipebravos.planilha.auth.Usuario;
import com.filipebravos.planilha.auth.UsuarioRepository;
import com.filipebravos.planilha.auth.UsuariosIniciais;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sem o AutenticadoTestConfig: aqui a segurança é a de verdade. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

    private static final String SENHA = "senha-forte-1";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void criarUsuarios() {
        usuarios.deleteAll();
        usuarios.save(new Usuario("filipe", encoder.encode(SENHA)));
    }

    private static String login(String usuario, String senha) {
        return "{\"usuario\":\"%s\",\"senha\":\"%s\"}".formatted(usuario, senha);
    }

    private MvcResult entrar(String usuario, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(login(usuario, senha))).andReturn();
    }

    private MockHttpSession sessaoLogada() throws Exception {
        MvcResult r = entrar("filipe", SENHA);
        assertEquals(200, r.getResponse().getStatus());
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    @Test
    void semLoginTudoDaApiRetorna401MasASaudeEPublica() throws Exception {
        mvc.perform(get("/api/registros")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/despesas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/emprestimos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/relatorios/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/despesas").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/saude")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void erroDe404NaoViraErroDeLogin() throws Exception {
        MockHttpSession sessao = sessaoLogada();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/despesas/999999")
                .session(sessao).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    void senhaErradaEUsuarioInexistenteDaoARespostaIgual() throws Exception {
        MvcResult errada = entrar("filipe", "outra-senha");
        MvcResult inexistente = entrar("fantasma", "outra-senha");
        assertEquals(401, errada.getResponse().getStatus());
        assertEquals(401, inexistente.getResponse().getStatus());
        assertEquals(errada.getResponse().getContentAsString(), inexistente.getResponse().getContentAsString());
    }

    @Test
    void loginAbreSessaoEIgnoraMaiusculas() throws Exception {
        MvcResult r = entrar("  Filipe ", SENHA);
        assertEquals(200, r.getResponse().getStatus());
        MockHttpSession sessao = (MockHttpSession) r.getRequest().getSession(false);

        mvc.perform(get("/api/auth/eu").session(sessao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario").value("filipe"));
        mvc.perform(get("/api/registros").session(sessao)).andExpect(status().isOk());
    }

    @Test
    void escritaSemTokenCsrfEhRecusada() throws Exception {
        MockHttpSession sessao = sessaoLogada();
        mvc.perform(post("/api/despesas").session(sessao).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sairEncerraASessao() throws Exception {
        MockHttpSession sessao = sessaoLogada();
        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void trocaDeSenha() throws Exception {
        MockHttpSession sessao = sessaoLogada();
        String url = "/api/auth/senha";

        mvc.perform(post(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada\",\"novaSenha\":\"nova-senha-2\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"curta\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"" + SENHA + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"nova-senha-2\"}"))
                .andExpect(status().isOk());

        assertEquals(401, entrar("filipe", SENHA).getResponse().getStatus());
        assertEquals(200, entrar("filipe", "nova-senha-2").getResponse().getStatus());
    }

    @Test
    void depoisDeCincoErrosBloqueiaAteComASenhaCerta() throws Exception {
        usuarios.save(new Usuario("vagner", encoder.encode(SENHA)));
        for (int i = 0; i < 5; i++) {
            assertEquals(401, entrar("vagner", "errada" + i).getResponse().getStatus());
        }
        assertEquals(429, entrar("vagner", SENHA).getResponse().getStatus());
        // outro usuário não é afetado
        assertEquals(200, entrar("filipe", SENHA).getResponse().getStatus());
    }

    @Test
    void usuariosIniciaisSoCriamQuemNaoExisteESenhasValidas() throws Exception {
        new UsuariosIniciais(usuarios, encoder, "Novo1:senha-longa-1, curto:123 ,semsenha, filipe:outra-senha-9").run(null);

        assertTrue(usuarios.findByLogin("novo1").isPresent());
        assertTrue(usuarios.findByLogin("curto").isEmpty());
        assertTrue(usuarios.findByLogin("semsenha").isEmpty());
        // quem já existia mantém a senha
        assertTrue(encoder.matches(SENHA, usuarios.findByLogin("filipe").orElseThrow().getSenhaHash()));
        assertEquals(2, usuarios.count());
    }
}

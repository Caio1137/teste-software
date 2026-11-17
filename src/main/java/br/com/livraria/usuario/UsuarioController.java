package br.com.livraria.usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    UsuarioController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping
    List<UsuarioResposta> listar() {
        return usuarios.findAll().stream().map(UsuarioResposta::de).toList();
    }

    @GetMapping("/{id}")
    UsuarioResposta buscar(@PathVariable Long id) {
        return UsuarioResposta.de(encontrar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResposta criar(@Valid @RequestBody DadosUsuario dados) {
        return salvar(null, dados, codificar(dados.senha()));
    }

    @PutMapping("/{id}")
    UsuarioResposta atualizar(@PathVariable Long id, @Valid @RequestBody DadosUsuario dados) {
        Usuario usuario = encontrar(id);
        String senhaHash = dados.senha() == null || dados.senha().isEmpty()
                ? usuario.senhaHash() : codificar(dados.senha());
        return salvar(id, dados, senhaHash);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable Long id) {
        encontrar(id);
        usuarios.deleteById(id);
    }

    private Usuario encontrar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private String codificar(String senha) {
        if (senha == null || senha.isBlank() || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 8 caracteres e até 72 bytes.");
        }
        return encoder.encode(senha);
    }

    private UsuarioResposta salvar(Long id, DadosUsuario dados, String senhaHash) {
        String email = dados.email().strip().toLowerCase(Locale.ROOT);
        return UsuarioResposta.de(usuarios.save(new Usuario(id, dados.nome().strip(), email, senhaHash)));
    }

    record DadosUsuario(
            @NotBlank(message = "Informe o nome.") @Size(max = 100) String nome,
            @NotBlank(message = "Informe o e-mail.") @Email(message = "Informe um e-mail válido.") @Size(max = 150) String email,
            String senha) {
        @Override
        public String toString() {
            return "DadosUsuario[nome=" + nome + ", email=" + email + "]";
        }
    }

    record UsuarioResposta(Long id, String nome, String email) {
        static UsuarioResposta de(Usuario usuario) {
            return new UsuarioResposta(usuario.id(), usuario.nome(), usuario.email());
        }
    }
}

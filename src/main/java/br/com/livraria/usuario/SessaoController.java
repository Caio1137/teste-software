package br.com.livraria.usuario;

import java.security.Principal;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SessaoController {
    @GetMapping("/api/csrf")
    CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/api/sessao")
    Map<String, String> sessao(Principal usuario) {
        return Map.of("email", usuario.getName());
    }
}

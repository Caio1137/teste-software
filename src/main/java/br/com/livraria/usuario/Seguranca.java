package br.com.livraria.usuario;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
class Seguranca {
    @Bean
    SecurityFilterChain filtro(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/estilo.css", "/app.js", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler((request, response, user) -> response.setStatus(204))
                        .failureHandler((request, response, erro) -> response.setStatus(401)))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, user) -> response.setStatus(204)))
                .requestCache(cache -> cache.disable())
                .exceptionHandling(erro -> erro.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService detalhesUsuario(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .map(usuario -> User.withUsername(usuario.email()).password(usuario.senhaHash()).roles("USUARIO").build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    @Bean
    ApplicationRunner usuarioInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${livraria.usuario.inicial.email}") String email,
            @Value("${livraria.usuario.inicial.senha}") String senha) {
        return args -> {
            if (usuarios.count() == 0) {
                usuarios.save(new Usuario(null, "Administrador", email.strip().toLowerCase(Locale.ROOT),
                        encoder.encode(senha)));
            }
        };
    }
}

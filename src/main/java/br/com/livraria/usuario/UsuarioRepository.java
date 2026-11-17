package br.com.livraria.usuario;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

interface UsuarioRepository extends ListCrudRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}

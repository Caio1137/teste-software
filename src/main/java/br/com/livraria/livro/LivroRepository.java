package br.com.livraria.livro;

import org.springframework.data.repository.ListCrudRepository;

public interface LivroRepository extends ListCrudRepository<Livro, Long> {
}

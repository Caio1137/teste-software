package br.com.livraria.livro;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("livros")
public record Livro(@Id Long id, String titulo, String autor, String categoria, String isbn, BigDecimal preco) {
}

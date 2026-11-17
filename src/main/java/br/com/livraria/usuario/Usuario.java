package br.com.livraria.usuario;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("usuarios")
record Usuario(@Id Long id, String nome, String email, String senhaHash) {
}

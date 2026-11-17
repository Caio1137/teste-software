package br.com.livraria.venda;

import org.springframework.data.repository.ListCrudRepository;

interface VendaRepository extends ListCrudRepository<Venda, Long> {
}

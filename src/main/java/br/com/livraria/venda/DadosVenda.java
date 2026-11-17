package br.com.livraria.venda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

record DadosVenda(
        @NotNull(message = "Selecione um livro.") @Positive Long livroId,
        @NotBlank(message = "Informe o cliente.") @Size(max = 100) String cliente,
        @NotNull(message = "Informe a quantidade.") @Positive(message = "A quantidade deve ser maior que zero.") Integer quantidade,
        @NotNull(message = "Informe a data da venda.") LocalDate dataVenda,
        @NotNull(message = "Informe a forma de pagamento.") Pagamento pagamento) {
}

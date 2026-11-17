package br.com.livraria.venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("vendas")
record Venda(@Id Long id, Long livroId, String cliente, int quantidade, BigDecimal valorUnitario,
             LocalDate dataVenda, Pagamento pagamento) {
    public BigDecimal getTotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}

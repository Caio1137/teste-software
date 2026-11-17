package br.com.livraria.venda;

import br.com.livraria.livro.Livro;
import br.com.livraria.livro.LivroRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {
    private final LocalDate data = LocalDate.of(2026, 11, 17);

    @Mock LivroRepository livros;
    @Mock VendaRepository vendas;
    @InjectMocks VendaService servico;

    @Test
    void calculaTotalPeloPrecoDoLivroEQuantidade() {
        var livro = new Livro(1L, "Dom Casmurro", "Machado de Assis", "Romance",
                "9788535910663", new BigDecimal("29.90"));
        when(livros.findById(1L)).thenReturn(Optional.of(livro));
        when(vendas.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Venda venda = servico.criar(new DadosVenda(1L, "Ana", 3, data, Pagamento.PIX));

        assertEquals(new BigDecimal("29.90"), venda.valorUnitario());
        assertEquals(new BigDecimal("89.70"), venda.getTotal());
        verify(vendas).save(venda);
    }

    @Test
    void preservaPrecoOriginalAoEditarVendaDoMesmoLivro() {
        var original = new Venda(2L, 1L, "Ana", 1, new BigDecimal("25.00"), data, Pagamento.PIX);
        when(vendas.findById(2L)).thenReturn(Optional.of(original));
        when(vendas.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Venda venda = servico.atualizar(2L, new DadosVenda(1L, "Ana", 2, data, Pagamento.CARTAO));

        assertEquals(2L, venda.id());
        assertEquals(new BigDecimal("25.00"), venda.valorUnitario());
        assertEquals(new BigDecimal("50.00"), venda.getTotal());
        assertEquals(Pagamento.CARTAO, venda.pagamento());
        verifyNoInteractions(livros);
    }

    @Test
    void rejeitaVendaQuandoLivroNaoExiste() {
        when(livros.findById(99L)).thenReturn(Optional.empty());

        var erro = assertThrows(ResponseStatusException.class,
                () -> servico.criar(new DadosVenda(99L, "Ana", 1, data, Pagamento.PIX)));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(vendas);
    }
}

package br.com.livraria.venda;

import br.com.livraria.livro.LivroRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class VendaService {
    private final LivroRepository livros;
    private final VendaRepository vendas;

    VendaService(LivroRepository livros, VendaRepository vendas) {
        this.livros = livros;
        this.vendas = vendas;
    }

    List<Venda> listar() {
        return vendas.findAll();
    }

    Venda buscar(Long id) {
        return vendas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda não encontrada."));
    }

    Venda criar(DadosVenda dados) {
        return salvar(null, dados, precoDoLivro(dados.livroId()));
    }

    Venda atualizar(Long id, DadosVenda dados) {
        Venda venda = buscar(id);
        BigDecimal preco = venda.livroId().equals(dados.livroId())
                ? venda.valorUnitario() : precoDoLivro(dados.livroId());
        return salvar(id, dados, preco);
    }

    void excluir(Long id) {
        buscar(id);
        vendas.deleteById(id);
    }

    private BigDecimal precoDoLivro(Long id) {
        return livros.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado."))
                .preco();
    }

    private Venda salvar(Long id, DadosVenda dados, BigDecimal preco) {
        return vendas.save(new Venda(id, dados.livroId(), dados.cliente().strip(), dados.quantidade(),
                preco, dados.dataVenda(), dados.pagamento()));
    }
}

package br.com.livraria.venda;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendas")
class VendaController {
    private final VendaService vendas;

    VendaController(VendaService vendas) {
        this.vendas = vendas;
    }

    @GetMapping
    List<Venda> listar() {
        return vendas.listar();
    }

    @GetMapping("/{id}")
    Venda buscar(@PathVariable Long id) {
        return vendas.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Venda criar(@Valid @RequestBody DadosVenda dados) {
        return vendas.criar(dados);
    }

    @PutMapping("/{id}")
    Venda atualizar(@PathVariable Long id, @Valid @RequestBody DadosVenda dados) {
        return vendas.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable Long id) {
        vendas.excluir(id);
    }
}

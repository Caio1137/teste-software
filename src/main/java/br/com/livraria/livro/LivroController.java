package br.com.livraria.livro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/livros")
class LivroController {
    private final LivroRepository livros;

    LivroController(LivroRepository livros) {
        this.livros = livros;
    }

    @GetMapping
    List<Livro> listar() {
        return livros.findAll();
    }

    @GetMapping("/{id}")
    Livro buscar(@PathVariable Long id) {
        return livros.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Livro criar(@Valid @RequestBody DadosLivro dados) {
        return livros.save(dados.livro(null));
    }

    @PutMapping("/{id}")
    Livro atualizar(@PathVariable Long id, @Valid @RequestBody DadosLivro dados) {
        buscar(id);
        return livros.save(dados.livro(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable Long id) {
        buscar(id);
        livros.deleteById(id);
    }

    record DadosLivro(
            @NotBlank(message = "Informe o título.") @Size(max = 150) String titulo,
            @NotBlank(message = "Informe o autor.") @Size(max = 100) String autor,
            @NotBlank(message = "Informe a categoria.") @Size(max = 60) String categoria,
            @NotNull(message = "Informe o ISBN.") @Pattern(regexp = "[0-9]{13}", message = "Informe um ISBN de 13 dígitos.") String isbn,
            @NotNull(message = "Informe o preço.") @Positive(message = "O preço deve ser maior que zero.")
            @Digits(integer = 8, fraction = 2, message = "Informe um preço com até duas casas decimais.") BigDecimal preco) {
        Livro livro(Long id) {
            return new Livro(id, titulo.strip(), autor.strip(), categoria.strip(), isbn, preco);
        }
    }
}

package br.com.livraria;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class LivrariaApiTest extends TesteComServidor {
    @Test
    void protegeCadastrosEAutenticaUsuarioCriadoPelaApi() {
        requisicao(new CookieFilter()).get("/api/usuarios").then().statusCode(401);
        var admin = new CookieFilter();
        login(admin, EMAIL, SENHA, 204);

        int id = alterar(admin).contentType(ContentType.JSON)
                .body(Map.of("nome", "Ana Silva", "email", "ana@livraria.com", "senha", "Senha123"))
                .post("/api/usuarios").then().statusCode(201)
                .body("email", equalTo("ana@livraria.com"))
                .body("$", not(hasKey("senhaHash"))).extract().path("id");

        login(new CookieFilter(), "ana@livraria.com", "Senha123", 204);
        alterar(admin).contentType(ContentType.JSON)
                .body(Map.of("nome", "Ana Souza", "email", "ana@livraria.com", "senha", "NovaSenha456"))
                .put("/api/usuarios/" + id).then().statusCode(200).body("nome", equalTo("Ana Souza"));
        requisicao(admin).get("/api/usuarios/" + id).then().statusCode(200).body("nome", equalTo("Ana Souza"));
        login(new CookieFilter(), "ana@livraria.com", "Senha123", 401);
        login(new CookieFilter(), "ana@livraria.com", "NovaSenha456", 204);
        alterar(admin).delete("/api/usuarios/" + id).then().statusCode(204);
        login(new CookieFilter(), "ana@livraria.com", "NovaSenha456", 401);
    }

    @Test
    void registraVendaEPreservaPrecoAoEditar() {
        var sessao = new CookieFilter();
        login(sessao, EMAIL, SENHA, 204);
        var livro = Map.of("titulo", "O Alienista", "autor", "Machado de Assis", "categoria", "Conto",
                "isbn", "9788572326076", "preco", 32.50);
        int livroId = alterar(sessao).contentType(ContentType.JSON).body(livro)
                .post("/api/livros").then().statusCode(201).extract().path("id");
        requisicao(sessao).get("/api/livros/" + livroId).then().statusCode(200)
                .body("titulo", equalTo("O Alienista"));
        alterar(sessao).contentType(ContentType.JSON).body(livro)
                .post("/api/livros").then().statusCode(409);

        var venda = Map.of("livroId", livroId, "cliente", "Carlos", "quantidade", 2,
                "dataVenda", "2026-11-17", "pagamento", "PIX");
        int vendaId = alterar(sessao).contentType(ContentType.JSON).body(venda)
                .post("/api/vendas").then().statusCode(201)
                .body("total", equalTo(65.00f)).extract().path("id");
        alterar(sessao).contentType(ContentType.JSON)
                .body(Map.of("titulo", "O Alienista", "autor", "Machado de Assis", "categoria", "Conto",
                        "isbn", "9788572326076", "preco", 40.00))
                .put("/api/livros/" + livroId).then().statusCode(200).body("preco", equalTo(40.00f));
        alterar(sessao).contentType(ContentType.JSON)
                .body(Map.of("livroId", livroId, "cliente", "Carlos", "quantidade", 3,
                        "dataVenda", "2026-11-17", "pagamento", "PIX"))
                .put("/api/vendas/" + vendaId).then().statusCode(200);
        requisicao(sessao).get("/api/vendas/" + vendaId).then().statusCode(200)
                .body("total", equalTo(97.50f));

        alterar(sessao).delete("/api/livros/" + livroId).then().statusCode(409);
        alterar(sessao).delete("/api/vendas/" + vendaId).then().statusCode(204);
        requisicao(sessao).get("/api/vendas/" + vendaId).then().statusCode(404);
        alterar(sessao).delete("/api/livros/" + livroId).then().statusCode(204);
        requisicao(sessao).get("/api/livros/" + livroId).then().statusCode(404);
    }

    private void login(CookieFilter sessao, String email, String senha, int status) {
        alterar(sessao).formParam("email", email).formParam("senha", senha)
                .post("/api/login").then().statusCode(status);
    }

    private RequestSpecification alterar(CookieFilter sessao) {
        var csrf = requisicao(sessao).get("/api/csrf").then().statusCode(200).extract();
        return requisicao(sessao).header(csrf.<String>path("headerName"), csrf.<String>path("token"));
    }

    private RequestSpecification requisicao(CookieFilter sessao) {
        return given().baseUri("http://127.0.0.1").port(porta).filter(sessao).log().ifValidationFails();
    }
}

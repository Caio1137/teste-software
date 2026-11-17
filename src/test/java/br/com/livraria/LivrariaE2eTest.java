package br.com.livraria;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

class LivrariaE2eTest extends TesteComServidor {
    private WebDriver navegador;
    private WebDriverWait espera;

    @BeforeEach
    void abrir() {
        var opcoes = new ChromeOptions();
        opcoes.addArguments("--headless=new", "--window-size=1280,900");
        navegador = new ChromeDriver(opcoes);
        espera = new WebDriverWait(navegador, Duration.ofSeconds(12));
        navegador.get("http://127.0.0.1:" + porta);
    }

    @AfterEach
    void fechar() {
        if (navegador != null) navegador.quit();
    }

    @Test
    void bloqueiaSenhaIncorretaEPermiteEntrarESair() {
        login("senha-incorreta");
        espera.until(textToBePresentInElementLocated(By.id("erro-login"), "E-mail ou senha inválidos."));

        login(SENHA);
        espera.until(visibilityOfElementLocated(By.id("painel")));
        assertTrue(navegador.findElement(By.id("painel")).getText().contains("Novo livro"));

        clicar("#sair");
        espera.until(visibilityOfElementLocated(By.id("form-login")));
        assertTrue(espera.until(invisibilityOfElementLocated(By.id("painel"))));
    }

    @Test
    void cadastraLivroERegistraEditaEExcluiVenda() {
        login(SENHA);
        clicar("#novo");
        String titulo = "Livro Selenium " + UUID.randomUUID().toString().substring(0, 6);
        preencher("campo-titulo", titulo);
        preencher("campo-autor", "Fernanda Lima");
        preencher("campo-categoria", "Romance");
        preencher("campo-isbn", "9786501765435");
        preencher("campo-preco", "35.50");
        salvar();
        espera.until(textToBePresentInElementLocated(By.id("registros"), titulo));

        clicar("[data-entidade='vendas']");
        clicar("#novo");
        new Select(navegador.findElement(By.id("campo-livroId"))).selectByVisibleText(titulo);
        preencher("campo-cliente", "Beatriz");
        preencher("campo-quantidade", "2");
        var campoData = navegador.findElement(By.id("campo-dataVenda"));
        ((JavascriptExecutor) navegador).executeScript("arguments[0].value = '2026-11-17';", campoData);
        new Select(navegador.findElement(By.id("campo-pagamento"))).selectByValue("PIX");
        salvar();
        espera.until(textToBePresentInElementLocated(By.id("registros"), "71,00"));
        assertTrue(navegador.findElement(By.id("registros")).getText().contains("17/11/2026"));

        clicar("#registros .editar");
        preencher("campo-quantidade", "3");
        salvar();
        espera.until(textToBePresentInElementLocated(By.id("registros"), "106,50"));
        excluirPrimeiroRegistro();
        espera.until(textToBePresentInElementLocated(By.id("registros"), "Nenhum registro cadastrado."));

        clicar("[data-entidade='livros']");
        espera.until(textToBePresentInElementLocated(By.id("registros"), titulo));
        excluirPrimeiroRegistro();
        espera.until(textToBePresentInElementLocated(By.id("registros"), "Nenhum registro cadastrado."));
    }

    private void login(String senha) {
        preencher("login-email", EMAIL);
        preencher("login-senha", senha);
        clicar("#form-login button");
    }

    private void preencher(String id, String valor) {
        var campo = espera.until(visibilityOfElementLocated(By.id(id)));
        campo.clear();
        campo.sendKeys(valor);
    }

    private void clicar(String seletor) {
        espera.until(elementToBeClickable(By.cssSelector(seletor))).click();
    }

    private void salvar() {
        clicar("#form-cadastro button[type='submit']");
        espera.until(invisibilityOfElementLocated(By.id("cadastro")));
    }

    private void excluirPrimeiroRegistro() {
        clicar("#registros .excluir");
        espera.until(alertIsPresent()).accept();
    }
}

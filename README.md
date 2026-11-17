# Livraria N2

## Integrantes

- Caio Caetano Marques
- Ruben Borges de Souza
- Pedro Lucas Borges

## Proposta

Sistema para organizar os livros e registrar as vendas de uma livraria. Depois de entrar com e-mail e senha, o usuário pode cadastrar, consultar, editar e excluir registros nas abas Livros, Vendas e Usuários.

- **Livros:** ID, título, autor, categoria, ISBN e preço.
- **Vendas:** ID, livro, cliente, quantidade, valor unitário, data e forma de pagamento.
- **Usuários:** ID, nome, e-mail e senha armazenada com BCrypt.

Ao cadastrar uma venda, o sistema usa o preço atual do livro e calcula o total pela quantidade vendida. Uma mudança no preço do catálogo não altera as vendas já registradas. Um livro vinculado a uma venda só pode ser excluído depois que a venda for removida.

## Tecnologias

Java 17, Spring Boot (Web, Data JDBC e Security), PostgreSQL, HTML, CSS e JavaScript. A interface usa as APIs REST da aplicação na mesma porta. Os testes usam JUnit 5, Selenium e REST Assured, com execução pelo Maven Wrapper.

## Como executar

Use Java 17 e PostgreSQL em execução. Para rodar os testes de interface, tenha o Google Chrome instalado. Na primeira execução, o Maven Wrapper e o Selenium Manager precisam de acesso à internet para baixar as dependências e o driver.

O padrão é conectar ao banco `postgres` em `localhost:5432` com o usuário `postgres`. Se necessário, configure `N2_DB_URL` com a URL JDBC do banco e `N2_DB_USUARIO` com o usuário. O usuário do banco precisa ter permissão para criar as tabelas e, durante os testes, um esquema temporário.

No PowerShell, execute na pasta do projeto:

```powershell
$env:N2_DB_SENHA = Read-Host 'Senha do PostgreSQL'
.\mvnw.cmd spring-boot:run
```

Em Linux ou macOS, exporte a senha em `N2_DB_SENHA` e execute `./mvnw spring-boot:run`.

Acesse <http://127.0.0.1:8080>. A aplicação cria as tabelas ao iniciar. Quando a tabela de usuários está vazia, o primeiro login é `admin@livraria.com` com a senha `Livraria123`. Para escolher outras credenciais iniciais, defina `N2_LOGIN_EMAIL` e `N2_LOGIN_SENHA` antes de iniciar pela primeira vez.

## Testes

Com o PostgreSQL em execução e a senha configurada no terminal, rode todos os testes com:

```powershell
.\mvnw.cmd test
```

Em Linux ou macOS, use `./mvnw test`. Os testes de API e interface sobem um servidor em porta livre e usam um esquema temporário no PostgreSQL, removido no fim da execução.

| Nível | Ferramenta | Casos | O que verifica |
| --- | --- | ---: | --- |
| Unitário | JUnit 5 | 3 | Total da venda, preservação do preço e rejeição de livro inexistente |
| API | REST Assured | 2 | Acesso e CRUD de usuário, além dos CRUDs de livro e venda e do vínculo entre eles |
| Integração (E2E) | Selenium | 2 | Login com senha incorreta, login válido e saída, além do cadastro de livro e do fluxo de venda |

## Vídeo demonstrativo

[Baixar a demonstração](demonstracao.mp4)

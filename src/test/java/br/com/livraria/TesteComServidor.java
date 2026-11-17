package br.com.livraria;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "livraria.usuario.inicial.email=admin@livraria.com",
        "livraria.usuario.inicial.senha=Livraria123"
})
@Import(TesteComServidor.ConfiguracaoBanco.class)
abstract class TesteComServidor {
    static final String EMAIL = "admin@livraria.com";
    static final String SENHA = "Livraria123";

    @LocalServerPort
    int porta;

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoBanco {
        @Bean(destroyMethod = "close")
        BancoTeste bancoTeste(Environment env) throws SQLException {
            return new BancoTeste(env.getRequiredProperty("spring.datasource.url"),
                    env.getRequiredProperty("spring.datasource.username"),
                    env.getRequiredProperty("spring.datasource.password"));
        }

        @Bean(destroyMethod = "")
        DataSource dataSource(BancoTeste banco) {
            return banco.dataSource;
        }
    }

    static class BancoTeste implements AutoCloseable {
        private final String url;
        private final String usuario;
        private final String senha;
        private final String schema = "n2_teste_" + UUID.randomUUID().toString().replace("-", "");
        private final HikariDataSource dataSource = new HikariDataSource();

        BancoTeste(String url, String usuario, String senha) throws SQLException {
            this.url = url;
            this.usuario = usuario;
            this.senha = senha;
            executar("CREATE SCHEMA " + schema);
            dataSource.setJdbcUrl(url);
            dataSource.setUsername(usuario);
            dataSource.setPassword(senha);
            dataSource.setSchema(schema);
        }

        @Override
        public void close() throws SQLException {
            dataSource.close();
            executar("DROP SCHEMA " + schema + " CASCADE");
        }

        private void executar(String sql) throws SQLException {
            try (var conexao = DriverManager.getConnection(url, usuario, senha);
                 var comando = conexao.createStatement()) {
                comando.execute(sql);
            }
        }
    }
}

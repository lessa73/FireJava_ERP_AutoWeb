package test;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import erp.pages.ComercialPage;

public class TestComercial {

    private static final Logger log = LoggerFactory.getLogger(TestComercial.class);

    private static final String URL_DEFAULT = "http://www.fluxis.com.br:8083/fluxis/login.do";
    private static final String USUARIO_DEFAULT = "alexandre.lessa@celer.matriz";
    private static final String SENHA_DEFAULT = "0";

    private WebDriver driver;
    private ComercialPage comercialPage;

    @BeforeEach
    public void setUp() {
        log.info("Iniciando setup do teste");
        FirefoxOptions options = new FirefoxOptions();
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        comercialPage = new ComercialPage(driver);
        log.info("Setup concluído");
    }

    @Test
    public void acessarTelaCadastroProposta() {
        log.info("=== Iniciando teste: acessarTelaCadastroProposta ===");

        try {
            // Arrange - Preparar dados
            String url = System.getProperty("erp.url", URL_DEFAULT);
            String usuario = System.getProperty("username", USUARIO_DEFAULT);
            String senha = System.getProperty("password", SENHA_DEFAULT);

            // Act & Assert - Login
            log.info("Realizando login");
            boolean logou = comercialPage.realizarLoginCompleto(url, usuario, senha);
            assertTrue(logou, "Falha no login!");

            // Act - Navegar e preencher
            log.info("Acessando tela de cadastro");
            comercialPage.acessarTelaCadastro();

            log.info("Preenchendo prazo");
            comercialPage.preencherPrazo("15");

            comercialPage.aguardarSegundos(2, "Aguardando antes de selecionar o cliente");

            log.info("Selecionando cliente");
            comercialPage.selecionarCliente(
                    "DESTOM INDUSTRIA",
                    "20.746.370/0001-80 - DESTOM INDUSTRIA E COMERCIO IMPORTACAO E EXPORTACAO LTDA"
            );

            comercialPage.aguardarSegundos(2, "Aguardando após seleção do cliente");

            log.info("Selecionando vendedor");
            comercialPage.selecionarVendedor(
                    "Alex",
                    "Alexandre Lessa (Fluxis)"
            );

            comercialPage.aguardarSegundos(2, "Aguardando antes de selecionar utilização");

            log.info("Selecionando utilização");
            comercialPage.selecionarUtilizacao("Venda de produto/mercadoria");

            comercialPage.aguardarSegundos(3, "Aguardando visualização final");

            log.info("=== Teste concluído com sucesso ===");

        } catch (Exception e) {
            log.error("=== Teste FALHOU ===", e);
            throw new RuntimeException("Teste falhou: " + e.getMessage(), e);
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                log.info("Aguardando antes de fechar o browser (para visualização)");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.warn("Interrupção durante aguardo final");
                Thread.currentThread().interrupt();
            }

            log.info("Fechando browser");
            driver.quit();
            log.info("Teardown concluído");
        }
    }
}

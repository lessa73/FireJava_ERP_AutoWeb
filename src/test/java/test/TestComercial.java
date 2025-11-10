package test;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import erp.pages.ComercialPage;

public class TestComercial {
    private WebDriver driver;
    private ComercialPage comercialPage;

    @BeforeEach
    public void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();

        comercialPage = new ComercialPage(driver);
    }

    @Test
    public void acessarTelaCadastroProposta() {
        try {
            String url = System.getProperty("erp.url", "http://www.fluxis.com.br:8083/fluxis/login.do");
            String usuario = System.getProperty("username", "alexandre.lessa@celer.matriz");
            String senha = System.getProperty("password", "0");

            boolean logou = comercialPage.realizarLoginCompleto(url, usuario, senha);
            assertTrue(logou, "Falha no login!");

       

            } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Teste falhou: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            // Aguarda um pouco antes de fechar para debug
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }
}
        
    

    

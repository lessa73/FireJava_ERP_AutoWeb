
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TestComercial {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // Método simplificado para aguardar a página carregar
    private void aguardarPaginaCarregar() {
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        });
    }

    @Test
    public void proposta() {
        // Acessa a página de login
        driver.get("http://www.fluxis.com.br:8083/fluxis/login.do");
        aguardarPaginaCarregar();

        // Login
        driver.findElement(By.id("username")).sendKeys("alexandre.lessa@celer.matriz");
        Assertions.assertEquals("alexandre.lessa@celer.matriz",
                driver.findElement(By.id("username")).getAttribute("value"));
        driver.findElement(By.id("password")).sendKeys("0");
        Assertions.assertEquals("0", driver.findElement(By.id("password")).getAttribute("value"));
        driver.findElement(By.cssSelector(".clearButton")).click();

        // Aguarda carregar após o login
        aguardarPaginaCarregar();

        // Navegação pelo menu
        driver.findElement(By.id("themeOffice_Comercial")).click();
        driver.findElement(By.id("cmSubMenuID19_Proposta")).click();
        driver.findElement(By.id("cmSubMenuID35_Cadastro")).click();

        // Aguarda carregar após abrir o cadastro
        aguardarPaginaCarregar();

        driver.switchTo().frame(driver.findElement(
                By.cssSelector("iframe[id^='window_'][id$='_content']")));

        // Aguarda carregar dentro do iframe
        aguardarPaginaCarregar();

        driver.findElement(By.id("prazoDias")).sendKeys("15");
        Assertions.assertEquals("15", driver.findElement(By.id("prazoDias")).getAttribute("value"));

        aguardarPaginaCarregar();

        WebElement campocliente = driver.findElement(By.id("cliente"));
        campocliente.clear();
        campocliente.sendKeys("DESTOM INDUSTRIA");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@title='20.746.370/0001-80 - DESTOM INDUSTRIA E COMERCIO IMPORTACAO E EXPORTACAO LTDA']")));
        option.click();

        aguardarPaginaCarregar();

        WebElement campoRepresentante = driver.findElement(By.id("representante"));
        campoRepresentante.clear();

        String nomeVendedor = "Alexandre";
        for (char c : nomeVendedor.toCharArray()) {
            campoRepresentante.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='Autocomplete_representante']//div[contains(@title,'Alexandre Lessa (Fluxis)')]")));
        option.click();

        aguardarPaginaCarregar();

        WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("utilizacao")));
        String textoVisivel = "Venda de produto/mercadoria";

        try {
            // Primeira tentativa: usar o Select padrão
            Select select = new Select(selectElement);
            select.selectByVisibleText(textoVisivel);

            // Confirma se selecionou corretamente
            Assertions.assertEquals(textoVisivel, select.getFirstSelectedOption().getText());
            System.out.println("[INFO] Seleção via Selenium Select realizada com sucesso.");

        } catch (Exception e) {
            System.out.println("[WARN] Campo <select> não interativo. Tentando via JavaScriptExecutor...");

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Localiza a opção desejada dinamicamente pelo texto visível
            WebElement opcao = selectElement.findElement(By.xpath(".//option[normalize-space(text())='" + textoVisivel + "']"));
            String valorUtilizacao = opcao.getAttribute("value");

            if (valorUtilizacao == null || valorUtilizacao.isEmpty()) {
                throw new RuntimeException("Não foi possível localizar o valor da opção: " + textoVisivel);
            }

            // Seta o valor e dispara o evento change
            js.executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'));",
                    selectElement, valorUtilizacao
            );

            // Espera o sistema reagir
            //Thread.sleep(1000);
            WebDriverWait waitChange = new WebDriverWait(driver, Duration.ofSeconds(10));
            waitChange.until(ExpectedConditions.attributeToBe(selectElement, "value", valorUtilizacao));

            // Valida se o valor realmente foi aplicado
            String valorSelecionado = selectElement.getAttribute("value");
            Assertions.assertEquals(valorUtilizacao, valorSelecionado);

            System.out.println("[INFO] Seleção via JavaScriptExecutor realizada com sucesso (" + textoVisivel + ").");
        }

        aguardarPaginaCarregar();

        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    }

    // @AfterEach
    // public void tearDown() {
    // if (driver != null) {
    // driver.quit();
    // }
    // }
}

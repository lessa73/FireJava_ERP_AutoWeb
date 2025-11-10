package erp.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePages {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait;

    public BasePages(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // Métodos utilitários reutilizáveis
    protected void aguardarElementoClicavel(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void clicarElemento(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    protected void preencherCampo(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(texto);
    }

    protected void mudarParaIframe(String selectorIframe) {
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(selectorIframe)));
        driver.switchTo().frame(iframe);
    }

    protected void voltarDoIframe() {
        driver.switchTo().defaultContent();
    }

    protected void scrollParaElemento(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    protected boolean elementoExiste(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

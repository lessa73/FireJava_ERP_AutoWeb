package erp.utils;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitUtils {
    
    // Aguarda a página carregar completamente
    public static void aguardarPaginaCarregar(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
            webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete")
        );
    }
    
    // Aguarda jQuery finalizar (se o sistema usar)
    public static void aguardarJQuery(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
            webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return jQuery.active == 0")
        );
    }
// Aguarda Ajax finalizar
    public static void aguardarAjax(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                webDriver -> {
                    JavascriptExecutor js = (JavascriptExecutor) webDriver;
                    return (Boolean) js.executeScript(
                        "return (typeof jQuery !== 'undefined' ? jQuery.active == 0 : true)"
                    );
                }
            );
        } catch (Exception e) {
            // Sistema pode não usar jQuery
        }
    }
}

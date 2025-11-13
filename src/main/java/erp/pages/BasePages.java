package erp.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePages {

    private static final Logger log = LoggerFactory.getLogger(BasePages.class);

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait;
    protected JavascriptExecutor js;

    // Constantes de timeout
    private static final int TIMEOUT_PADRAO = 15;
    private static final int TIMEOUT_LONGO = 30;
    private static final int TIMEOUT_AJAX = 10;
    private static final int POLLING_INTERVAL = 500; // ms

    public BasePages(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_PADRAO));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_LONGO));
        this.js = (JavascriptExecutor) driver;
    }

    // =======================================================
    // MÉTODOS DE ESPERA E SINCRONIZAÇÃO
    // =======================================================

    /**
     * Aguarda elemento estar clicável
     * @param locator Localizador do elemento
     */
    protected void aguardarElementoClicavel(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Aguarda elemento estar visível
     * @param locator Localizador do elemento
     */
    protected void aguardarElementoVisivel(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Aguarda elemento estar presente no DOM
     * @param locator Localizador do elemento
     */
    protected void aguardarElementoPresente(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Aguarda a página carregar completamente
     * Verifica o document.readyState
     */
    protected void aguardarPaginaCarregar() {
        try {
            log.debug("Aguardando página carregar completamente");
            
            wait.until((ExpectedCondition<Boolean>) wd -> 
                js.executeScript("return document.readyState").equals("complete")
            );
            
            log.debug("Página carregada");
        } catch (Exception e) {
            log.warn("Timeout ao aguardar página carregar: {}", e.getMessage());
        }
    }

    /**
     * Aguarda requisições AJAX completarem (jQuery)
     * Verifica se jQuery.active === 0
     */
    protected void aguardarAjaxCompletar() {
        try {
            log.debug("Aguardando requisições AJAX completarem");
            
            WebDriverWait ajaxWait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_AJAX));
            
            ajaxWait.until((ExpectedCondition<Boolean>) wd -> {
                Boolean jQueryDefined = (Boolean) js.executeScript("return typeof jQuery != 'undefined'");
                
                if (jQueryDefined) {
                    Long activeConnections = (Long) js.executeScript("return jQuery.active");
                    return activeConnections == 0;
                }
                return true; // Se jQuery não está definido, considera completo
            });
            
            log.debug("Requisições AJAX concluídas");
        } catch (Exception e) {
            log.warn("Timeout ou erro ao aguardar AJAX: {}", e.getMessage());
        }
    }

    /**
     * Aguarda iframe carregar completamente
     * Muda para o iframe e verifica se está carregado
     */
    protected void aguardarIframeCarregarCompletamente() {
        try {
            log.debug("Aguardando iframe carregar completamente");
            
            // Aguarda um pouco para o iframe inicializar
            Thread.sleep(1000);
            
            // Verifica se o documento do iframe está carregado
            wait.until((ExpectedCondition<Boolean>) wd -> {
                try {
                    return js.executeScript("return document.readyState").equals("complete");
                } catch (Exception e) {
                    return false;
                }
            });
            
            log.debug("Iframe carregado");
        } catch (Exception e) {
            log.warn("Timeout ao aguardar iframe carregar: {}", e.getMessage());
        }
    }

    /**
     * Aguarda um intervalo específico em milissegundos
     * @param milissegundos Tempo de espera
     */
    protected void aguardarIntervalo(int milissegundos) {
        try {
            log.trace("Aguardando {} ms", milissegundos);
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            log.warn("Interrupção durante aguardo: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Aguarda elemento desaparecer/ficar invisível
     * @param locator Localizador do elemento
     */
    protected void aguardarElementoDesaparecer(By locator) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            log.debug("Elemento já invisível ou não encontrado: {}", locator);
        }
    }

    // =======================================================
    // MÉTODOS DE INTERAÇÃO COM ELEMENTOS
    // =======================================================

    /**
     * Clica em um elemento aguardando estar clicável
     * @param locator Localizador do elemento
     */
    protected void clicarElemento(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
        log.debug("Elemento clicado: {}", locator);
    }

    /**
     * Clica em um elemento usando JavaScript
     * Útil quando o clique normal não funciona
     * @param locator Localizador do elemento
     */
    protected void clicarElementoJS(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].click();", element);
        log.debug("Elemento clicado via JS: {}", locator);
    }

    /**
     * Preenche um campo de texto
     * @param locator Localizador do campo
     * @param texto Texto a ser preenchido
     */
    protected void preencherCampo(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(texto);
        log.debug("Campo preenchido: {} = '{}'", locator, texto);
    }

    /**
     * Preenche campo caractere por caractere (útil para autocomplete)
     * @param locator Localizador do campo
     * @param texto Texto a ser digitado
     * @param delayEntreCaracteres Delay em ms entre cada caractere
     */
    protected void digitarTextoDevagar(By locator, String texto, int delayEntreCaracteres) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            
            for (char c : texto.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                Thread.sleep(delayEntreCaracteres);
            }
            
            log.debug("Texto digitado devagar: {} = '{}'", locator, texto);
        } catch (InterruptedException e) {
            log.warn("Interrupção ao digitar texto: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Obtém o texto de um elemento
     * @param locator Localizador do elemento
     * @return Texto do elemento
     */
    protected String obterTexto(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText();
    }

    /**
     * Obtém o valor de um atributo
     * @param locator Localizador do elemento
     * @param atributo Nome do atributo
     * @return Valor do atributo
     */
    protected String obterAtributo(By locator, String atributo) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return element.getAttribute(atributo);
    }

    // =======================================================
    // MÉTODOS DE IFRAME
    // =======================================================

    /**
     * Muda o contexto para um iframe
     * @param selectorIframe Seletor CSS do iframe
     */
    protected void mudarParaIframe(String selectorIframe) {
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(selectorIframe)));
        driver.switchTo().frame(iframe);
        log.debug("Mudou para iframe: {}", selectorIframe);
    }

    /**
     * Muda o contexto para um iframe por índice
     * @param indice Índice do iframe (começando em 0)
     */
    protected void mudarParaIframePorIndice(int indice) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(indice));
        log.debug("Mudou para iframe de índice: {}", indice);
    }

    /**
     * Volta para o contexto principal (fora dos iframes)
     */
    protected void voltarDoIframe() {
        driver.switchTo().defaultContent();
        log.debug("Voltou para contexto principal");
    }

    // =======================================================
    // MÉTODOS DE SCROLL E NAVEGAÇÃO
    // =======================================================

    /**
     * Rola a página até um elemento
     * @param element Elemento para rolar até
     */
    protected void scrollParaElemento(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        log.debug("Rolou até o elemento");
    }

    /**
     * Rola a página até um elemento por locator
     * @param locator Localizador do elemento
     */
    protected void scrollParaElemento(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollParaElemento(element);
    }

    /**
     * Rola a página até o topo
     */
    protected void scrollParaTopo() {
        js.executeScript("window.scrollTo(0, 0);");
        log.debug("Rolou para o topo da página");
    }

    /**
     * Rola a página até o final
     */
    protected void scrollParaFinal() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        log.debug("Rolou para o final da página");
    }

    // =======================================================
    // MÉTODOS DE VALIDAÇÃO
    // =======================================================

    /**
     * Verifica se um elemento existe no DOM
     * @param locator Localizador do elemento
     * @return true se existe, false caso contrário
     */
    protected boolean elementoExiste(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se um elemento está visível
     * @param locator Localizador do elemento
     * @return true se visível, false caso contrário
     */
    protected boolean elementoVisivel(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se um elemento está habilitado
     * @param locator Localizador do elemento
     * @return true se habilitado, false caso contrário
     */
    protected boolean elementoHabilitado(By locator) {
        try {
            return driver.findElement(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se um elemento está selecionado (checkbox/radio)
     * @param locator Localizador do elemento
     * @return true se selecionado, false caso contrário
     */
    protected boolean elementoSelecionado(By locator) {
        try {
            return driver.findElement(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // =======================================================
    // MÉTODOS JAVASCRIPT
    // =======================================================

    /**
     * Executa um script JavaScript
     * @param script Script a ser executado
     * @param args Argumentos do script
     * @return Resultado da execução
     */
    protected Object executarJavaScript(String script, Object... args) {
        return js.executeScript(script, args);
    }

    /**
     * Remove atributo de um elemento via JavaScript
     * @param locator Localizador do elemento
     * @param atributo Nome do atributo a remover
     */
    protected void removerAtributo(By locator, String atributo) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].removeAttribute(arguments[1]);", element, atributo);
        log.debug("Atributo '{}' removido do elemento: {}", atributo, locator);
    }

    /**
     * Define um atributo de um elemento via JavaScript
     * @param locator Localizador do elemento
     * @param atributo Nome do atributo
     * @param valor Valor do atributo
     */
    protected void definirAtributo(By locator, String atributo, String valor) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, atributo, valor);
        log.debug("Atributo '{}' definido como '{}' no elemento: {}", atributo, valor, locator);
    }

    /**
     * Destaca um elemento na tela (útil para debug)
     * @param locator Localizador do elemento
     */
    protected void destacarElemento(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        String originalStyle = element.getAttribute("style");
        js.executeScript("arguments[0].setAttribute('style', 'border: 3px solid red; background: yellow;');", element);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        js.executeScript("arguments[0].setAttribute('style', '" + originalStyle + "');", element);
    }
}


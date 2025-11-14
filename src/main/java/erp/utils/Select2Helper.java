package erp.utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para manipular campos Select2 Select2 é um plugin jQuery
 * que transforma selects normais em componentes avançados
 */
public class Select2Helper {

    private static final Logger log = LoggerFactory.getLogger(Select2Helper.class);

    // Constantes de configuração
    private static final int TIMEOUT_SELECT2 = 10;
    private static final int DELAY_APOS_CLICK = 800; // ms
    private static final int DELAY_APOS_SELECAO = 1000; // ms

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    /**
     * Construtor
     *
     * @param driver Instância do WebDriver
     */
    public Select2Helper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SELECT2));
        this.js = (JavascriptExecutor) driver;
    }

    /**
     * Seleciona uma opção em um campo Select2
     *
     * @param selectId ID do select original (ex: "utilizacao")
     * @param textoOpcao Texto da opção a ser selecionada (ex: "Venda de
     * produto/mercadoria")
     * @throws InterruptedException Se a thread for interrompida
     */
    public void selecionarOpcao(String selectId, String textoOpcao) throws InterruptedException {
        log.info("Iniciando seleção Select2");
        log.debug("Select ID: {}", selectId);
        log.debug("Opção: '{}'", textoOpcao);

        try {
            // 1. Verificar se o select existe
            aguardarSelectDisponivel(selectId);

            // 2. Abrir o dropdown do Select2
            abrirDropdown(selectId);

            // 3. Aguardar dropdown estar visível
            aguardarDropdownVisivel();

            // 4. Localizar a opção específica
            WebElement opcao = localizarOpcao(textoOpcao);

            // 5. Clicar na opção
            clicarOpcao(opcao);

            // 6. Aguardar seleção ser processada
            aguardarAposSelecao();

            // 7. Validar seleção
            validarSelecao(selectId, textoOpcao);

            log.info("Seleção Select2 concluída com sucesso");

        } catch (InterruptedException e) {
            log.error("Thread interrompida durante seleção Select2");
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.error("Erro ao selecionar opção Select2: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar Select2: " + textoOpcao, e);
        }
    }

    /**
     * Seleciona opção por valor (value) ao invés de texto
     *
     * @param selectId ID do select
     * @param valor Valor do option (ex: "1")
     * @throws InterruptedException
     */
    public void selecionarPorValor(String selectId, String valor) throws InterruptedException {
        log.info("Selecionando Select2 por valor: {}", valor);

        try {
            aguardarSelectDisponivel(selectId);

            // Usar JavaScript para selecionar diretamente pelo valor
            String script = String.format(
                    "$('#%s').val('%s').trigger('change');",
                    selectId, valor
            );

            js.executeScript(script);

            Thread.sleep(DELAY_APOS_SELECAO);

            log.info("Seleção por valor concluída");

        } catch (Exception e) {
            log.error("Erro ao selecionar por valor: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar Select2 por valor: " + valor, e);
        }
    }

    // =======================================================
    // MÉTODOS PRIVADOS - LÓGICA INTERNA
    // =======================================================
    /**
     * Aguarda o select estar disponível no DOM
     */
    private void aguardarSelectDisponivel(String selectId) {
        log.debug("Aguardando select estar disponível: #{}", selectId);

        By selectLocator = By.id(selectId);
        wait.until(ExpectedConditions.presenceOfElementLocated(selectLocator));

        log.debug("Select disponível no DOM");
    }

    /**
     * Abre o dropdown do Select2 clicando no container
     */
    private void abrirDropdown(String selectId) throws InterruptedException {
        log.debug("Abrindo dropdown do Select2");

        // O Select2 cria um container com ID s2id_{selectId}
        String containerId = "s2id_" + selectId;
        By containerLocator = By.id(containerId);

        // Aguardar container estar presente
        WebElement container = wait.until(ExpectedConditions.presenceOfElementLocated(containerLocator));
        wait.until(ExpectedConditions.visibilityOf(container));

        // Scroll até o elemento
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", container);
        Thread.sleep(300);

        // Tentar clicar no container
        try {
            log.debug("Tentativa 1: Click no container");
            container.click();
            Thread.sleep(DELAY_APOS_CLICK);
        } catch (Exception e) {
            log.debug("Tentativa 1 falhou, tentando via JavaScript");
            js.executeScript("arguments[0].click();", container);
            Thread.sleep(DELAY_APOS_CLICK);
        }

        log.debug("Dropdown aberto");
    }

    /**
     * Aguarda o dropdown estar visível na tela
     */
    private void aguardarDropdownVisivel() throws InterruptedException {
        log.debug("Aguardando dropdown ficar visível");

        Thread.sleep(500);

        // Aguardar elementos com classe select2-result-label (opções)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("select2-result-label")
            ));
            log.debug("Opções do dropdown detectadas");
        } catch (Exception e) {
            log.warn("Não foi possível detectar opções via wait, continuando...");
        }
    }

    /**
     * Localiza a opção específica pelo texto
     */
    private WebElement localizarOpcao(String textoOpcao) {
        log.debug("Localizando opção: '{}'", textoOpcao);

        // XPath para localizar div com classe select2-result-label contendo o texto
        String xpathExpression = String.format(
                "//div[@class='select2-result-label' and contains(text(), '%s')]",
                textoOpcao
        );

        By opcaoXPath = By.xpath(xpathExpression);
        log.debug("XPath usado: {}", xpathExpression);

        WebElement opcao = wait.until(ExpectedConditions.presenceOfElementLocated(opcaoXPath));
        wait.until(ExpectedConditions.visibilityOf(opcao));

        String textoEncontrado = opcao.getText();
        log.debug("Opção encontrada: '{}'", textoEncontrado);

        return opcao;
    }

    /**
     * Clica na opção do dropdown
     */
    private void clicarOpcao(WebElement opcao) throws InterruptedException {
        log.debug("Clicando na opção");

        // Scroll até a opção
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", opcao);
        Thread.sleep(200);

        // Tentar click normal
        try {
            opcao.click();
            log.debug("Click normal executado");
        } catch (Exception e) {
            log.debug("Click normal falhou, usando JavaScript");
            js.executeScript("arguments[0].click();", opcao);
        }

        Thread.sleep(DELAY_APOS_CLICK);
    }

    /**
     * Aguarda após a seleção para o valor ser processado
     */
    private void aguardarAposSelecao() throws InterruptedException {
        log.debug("Aguardando processamento da seleção ({} ms)", DELAY_APOS_SELECAO);
        Thread.sleep(DELAY_APOS_SELECAO);

        // Aguardar callbacks do Select2 (onchange, etc)
        try {
            WebDriverWait ajaxWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            ajaxWait.until(wd -> {
                Boolean jQueryDefined = (Boolean) js.executeScript("return typeof jQuery != 'undefined'");
                if (jQueryDefined) {
                    Long activeConnections = (Long) js.executeScript("return jQuery.active");
                    return activeConnections == 0;
                }
                return true;
            });
            log.debug("Callbacks processados");
        } catch (Exception e) {
            log.debug("Não foi possível verificar jQuery.active");
        }
    }

    /**
     * Valida se a seleção foi bem-sucedida
     */
    private void validarSelecao(String selectId, String textoEsperado) throws InterruptedException {
        Thread.sleep(300);

        log.debug("Validando seleção");

        // Verificar o texto exibido no Select2 (span.select2-chosen)
        String containerId = "s2id_" + selectId;

        try {
            // Localizar o span que mostra o texto selecionado
            By chosenLocator = By.cssSelector("#" + containerId + " .select2-chosen");
            WebElement chosenSpan = driver.findElement(chosenLocator);

            String textoExibido = chosenSpan.getText();
            log.debug("Texto exibido no Select2: '{}'", textoExibido);

            if (textoExibido != null && textoExibido.contains(textoEsperado)) {
                log.info("✓ Validação OK: Select2 exibindo '{}'", textoExibido);
            } else if (textoExibido == null || textoExibido.trim().isEmpty()) {
                log.error("❌ FALHA: Select2 está vazio após seleção");
                throw new RuntimeException("Select2 ficou vazio após tentativa de seleção");
            } else {
                log.warn("⚠ AVISO: Texto difere do esperado: '{}'", textoExibido);
            }

        } catch (Exception e) {
            log.warn("Não foi possível validar pelo span, validando pelo select");

            // Fallback: verificar o valor do select original
            WebElement select = driver.findElement(By.id(selectId));
            String valorSelecionado = select.getAttribute("value");

            log.debug("Valor no select original: '{}'", valorSelecionado);

            if (valorSelecionado != null && !valorSelecionado.trim().isEmpty()) {
                log.info("✓ Validação OK: Select tem valor '{}'", valorSelecionado);
            } else {
                log.error("❌ FALHA: Select sem valor após seleção");
                throw new RuntimeException("Select2 não tem valor após tentativa de seleção");
            }
        }
    }
}

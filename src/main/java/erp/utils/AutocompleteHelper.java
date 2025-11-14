
package erp.utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para manipular campos com autocomplete
 * Encapsula toda a lógica de interação com campos que possuem sugestões dinâmicas
 */
public class AutocompleteHelper {

    private static final Logger log = LoggerFactory.getLogger(AutocompleteHelper.class);

    // Constantes de configuração
    private static final int TIMEOUT_AUTOCOMPLETE = 15;
    private static final int DELAY_ENTRE_CARACTERES = 150; // ms
    private static final int DELAY_AGUARDAR_LISTA = 1500; // ms
    private static final int DELAY_APOS_HOVER = 300; // ms
    private static final int DELAY_APOS_CLICK = 1500; // ms - aumentado
    private static final int DELAY_APOS_BLUR = 2000; // ms

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;
    private final JavascriptExecutor js;

    /**
     * Construtor
     * @param driver Instância do WebDriver
     */
    public AutocompleteHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_AUTOCOMPLETE));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
    }

    /**
     * Seleciona uma opção em um campo com autocomplete
     * 
     * @param campoLocator Locator do campo de input (By)
     * @param textoDigitar Texto a ser digitado para filtrar as opções
     * @param textoOpcaoCompleto Texto completo da opção a ser selecionada (usado no XPath)
     * @throws InterruptedException Se a thread for interrompida
     */
    public void selecionarOpcao(By campoLocator, String textoDigitar, String textoOpcaoCompleto) 
            throws InterruptedException {

        log.info("Iniciando seleção de autocomplete");
        log.debug("Campo: {}", campoLocator);
        log.debug("Texto para digitar: '{}'", textoDigitar);
        log.debug("Opção completa: '{}'", textoOpcaoCompleto);

        try {
            // 1. Aguardar e preparar o campo
            WebElement campo = aguardarCampoDisponivel(campoLocator);

            // 2. Verificar se o campo já está preenchido corretamente
            String valorAtual = campo.getAttribute("value");
            if (valorAtual != null && valorAtual.contains(textoOpcaoCompleto)) {
                log.info("Campo já está preenchido com o valor correto: '{}'", valorAtual);
                return;
            }

            // 3. Limpar e focar no campo
            limparEFocarCampo(campo);

            // 4. Digitar texto caractere por caractere (para acionar autocomplete)
            digitarTextoDevagar(campo, textoDigitar);

            // 5. Aguardar lista de sugestões aparecer
            aguardarListaSugestoes();

            // 6. Localizar a opção específica
            WebElement opcao = localizarOpcao(textoOpcaoCompleto);

            // 7. Armazenar valor antes da seleção (para debug)
            String valorAntes = campo.getAttribute("value");
            log.debug("Valor do campo ANTES da seleção: '{}'", valorAntes);

            // 8. Realizar seleção usando múltiplas estratégias
            selecionarOpcaoComMultiplasEstrategias(opcao, campo);

            // 9. Aguardar um pouco para o valor ser preenchido
            Thread.sleep(800);

            // 10. Verificar se o valor foi preenchido corretamente
            String valorDepois = campo.getAttribute("value");
            log.debug("Valor do campo DEPOIS da seleção: '{}'", valorDepois);

            // 11. Disparar eventos para carregar dados adicionais (APENAS SE NECESSÁRIO)
            boolean deveDispararBlur = deveTriggerBlur(campoLocator);
            if (deveDispararBlur) {
                dispararEventosCarregamento(campo);
                aguardarCarregamentoDados();
            } else {
                log.info("Evento blur não será disparado para evitar limpeza do campo");
                // Apenas aguardar AJAX sem disparar blur
                aguardarAjaxQuietamente();
            }

            // 12. Validar se a seleção foi bem-sucedida
            validarSelecao(campo, textoDigitar, textoOpcaoCompleto);

            log.info("Seleção de autocomplete concluída com sucesso");

        } catch (InterruptedException e) {
            log.error("Thread interrompida durante seleção de autocomplete");
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.error("Erro ao selecionar opção de autocomplete: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar autocomplete: " + textoDigitar, e);
        }
    }

    /**
     * Decide se deve disparar blur baseado no tipo de campo
     */
    private boolean deveTriggerBlur(By campoLocator) {
        String locatorStr = campoLocator.toString().toLowerCase();

        // Campo cliente: SIM, precisa carregar dados adicionais
        if (locatorStr.contains("cliente")) {
            return true;
        }

        // Campo vendedor/representante: NÃO, pode limpar o valor
        if (locatorStr.contains("vendedor") || locatorStr.contains("representante")) {
            log.info("Campo de vendedor detectado - blur será evitado");
            return false;
        }

        // Outros campos: SIM por padrão
        return true;
    }

    /**
     * Aguarda AJAX sem disparar eventos
     */
    private void aguardarAjaxQuietamente() throws InterruptedException {
        log.debug("Aguardando AJAX completar (sem disparar eventos)");
        Thread.sleep(1000);

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
        } catch (Exception e) {
            log.debug("jQuery.active não disponível");
        }
    }

    /**
     * Seleciona opção usando seletor CSS para o campo
     */
    public void selecionarOpcaoPorCss(String campoCssSelector, String textoDigitar, String textoOpcaoCompleto) 
            throws InterruptedException {
        selecionarOpcao(By.cssSelector(campoCssSelector), textoDigitar, textoOpcaoCompleto);
    }

    /**
     * Seleciona opção usando ID do campo
     */
    public void selecionarOpcaoPorId(String campoId, String textoDigitar, String textoOpcaoCompleto) 
            throws InterruptedException {
        selecionarOpcao(By.id(campoId), textoDigitar, textoOpcaoCompleto);
    }

    // =======================================================
    // MÉTODOS PRIVADOS - LÓGICA INTERNA
    // =======================================================

    /**
     * Aguarda o campo estar disponível e retorna o elemento
     */
    private WebElement aguardarCampoDisponivel(By locator) {
        log.debug("Aguardando campo estar disponível: {}", locator);

        WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        wait.until(ExpectedConditions.visibilityOf(campo));
        wait.until(ExpectedConditions.elementToBeClickable(campo));

        log.debug("Campo disponível");
        return campo;
    }

    /**
     * Limpa o campo e foca nele para ativar o autocomplete
     */
    private void limparEFocarCampo(WebElement campo) throws InterruptedException {
        log.debug("Limpando e focando no campo");

        // Usar JavaScript para garantir limpeza completa
        js.executeScript("arguments[0].value = '';", campo);
        Thread.sleep(200);

        // Focar no campo
        campo.click();
        Thread.sleep(200);

        log.debug("Campo limpo e focado");
    }

    /**
     * Digita o texto caractere por caractere com delay
     */
    private void digitarTextoDevagar(WebElement campo, String texto) throws InterruptedException {
        log.debug("Iniciando digitação lenta do texto: '{}'", texto);

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            campo.sendKeys(String.valueOf(c));

            log.trace("Caractere digitado: '{}' ({}/{})", c, i + 1, texto.length());

            Thread.sleep(DELAY_ENTRE_CARACTERES);
        }

        log.debug("Digitação concluída");
    }

    /**
     * Aguarda a lista de sugestões do autocomplete aparecer
     */
    private void aguardarListaSugestoes() throws InterruptedException {
        log.debug("Aguardando lista de sugestões aparecer ({} ms)", DELAY_AGUARDAR_LISTA);
        Thread.sleep(DELAY_AGUARDAR_LISTA);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@onclick, 'Autocomplete')]")
            ));
            log.debug("Lista de sugestões detectada");
        } catch (Exception e) {
            log.warn("Lista de sugestões não detectada via wait, continuando...");
        }
    }

    /**
     * Localiza a opção específica na lista de autocomplete
     */
    private WebElement localizarOpcao(String textoCompleto) {
        log.debug("Localizando opção: '{}'", textoCompleto);

        String xpathExpression = String.format(
            "//div[contains(@onclick, 'Autocomplete') and contains(., '%s')]",
            textoCompleto
        );

        By opcaoXPath = By.xpath(xpathExpression);
        log.debug("XPath usado: {}", xpathExpression);

        WebElement opcao = wait.until(ExpectedConditions.presenceOfElementLocated(opcaoXPath));
        wait.until(ExpectedConditions.visibilityOf(opcao));

        String textoEncontrado = opcao.getText();
        String onclickAttr = opcao.getAttribute("onclick");
        log.debug("Opção encontrada com texto: '{}'", textoEncontrado);
        log.debug("Atributo onclick: '{}'", onclickAttr);

        return opcao;
    }

    /**
     * Seleciona a opção usando múltiplas estratégias
     */
    private void selecionarOpcaoComMultiplasEstrategias(WebElement opcao, WebElement campo) 
            throws InterruptedException {

        String onclickAttr = opcao.getAttribute("onclick");
        log.info("Tentando selecionar opção com onclick: '{}'", onclickAttr);

        // ESTRATÉGIA 1: Executar o código onclick diretamente via JavaScript
        boolean sucesso = tentarSelecaoViaJavaScript(opcao, onclickAttr);
        if (sucesso) return;

        // ESTRATÉGIA 2: Hover + Click via Actions
        sucesso = tentarSelecaoViaActions(opcao);
        if (sucesso) return;

        // ESTRATÉGIA 3: Click direto via WebDriver
        sucesso = tentarSelecaoViaClickDireto(opcao);
        if (sucesso) return;

        // ESTRATÉGIA 4: Click via JavaScript
        sucesso = tentarSelecaoViaClickJS(opcao);
        if (sucesso) return;

        log.warn("Todas as estratégias de seleção foram tentadas");
    }

    /**
     * ESTRATÉGIA 1: Executa o código onclick via JavaScript
     */
    private boolean tentarSelecaoViaJavaScript(WebElement opcao, String onclickAttr) 
            throws InterruptedException {
        try {
            log.debug("ESTRATÉGIA 1: Executando onclick via JavaScript");
            js.executeScript(onclickAttr);
            Thread.sleep(DELAY_APOS_CLICK);
            log.info("✓ Seleção via JavaScript executada");
            return true;
        } catch (Exception e) {
            log.warn("✗ Estratégia 1 falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ESTRATÉGIA 2: Hover + Click via Actions
     */
    private boolean tentarSelecaoViaActions(WebElement opcao) throws InterruptedException {
        try {
            log.debug("ESTRATÉGIA 2: Hover + Click via Actions");
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", opcao);
            Thread.sleep(200);
            actions.moveToElement(opcao).perform();
            Thread.sleep(DELAY_APOS_HOVER);
            actions.click(opcao).perform();
            Thread.sleep(DELAY_APOS_CLICK);
            log.info("✓ Seleção via Actions executada");
            return true;
        } catch (Exception e) {
            log.warn("✗ Estratégia 2 falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ESTRATÉGIA 3: Click direto via WebDriver
     */
    private boolean tentarSelecaoViaClickDireto(WebElement opcao) throws InterruptedException {
        try {
            log.debug("ESTRATÉGIA 3: Click direto");
            opcao.click();
            Thread.sleep(DELAY_APOS_CLICK);
            log.info("✓ Seleção via click direto executada");
            return true;
        } catch (Exception e) {
            log.warn("✗ Estratégia 3 falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ESTRATÉGIA 4: Click via JavaScript
     */
    private boolean tentarSelecaoViaClickJS(WebElement opcao) throws InterruptedException {
        try {
            log.debug("ESTRATÉGIA 4: Click via JavaScript");
            js.executeScript("arguments[0].click();", opcao);
            Thread.sleep(DELAY_APOS_CLICK);
            log.info("✓ Seleção via click JS executada");
            return true;
        } catch (Exception e) {
            log.warn("✗ Estratégia 4 falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Dispara eventos para carregar dados adicionais
     */
    private void dispararEventosCarregamento(WebElement campo) throws InterruptedException {
        log.info("Disparando eventos para carregar dados adicionais");

        try {
            log.debug("Disparando evento 'change'");
            js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", campo);
            Thread.sleep(300);

            log.debug("Disparando evento 'blur'");
            js.executeScript("arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));", campo);
            Thread.sleep(300);

            log.debug("Executando função loadCustomer diretamente");
            js.executeScript("if (typeof loadCustomer === 'function') { loadCustomer('cliente', arguments[0]); }", campo);
            Thread.sleep(300);

            log.debug("Simulando tecla TAB");
            campo.sendKeys(Keys.TAB);
            Thread.sleep(300);

            log.info("Eventos disparados com sucesso");
        } catch (Exception e) {
            log.warn("Erro ao disparar eventos: {}", e.getMessage());
        }
    }

    /**
     * Aguarda o carregamento dos dados via AJAX
     */
    private void aguardarCarregamentoDados() throws InterruptedException {
        log.info("Aguardando carregamento de dados via AJAX ({} ms)", DELAY_APOS_BLUR);
        Thread.sleep(DELAY_APOS_BLUR);

        try {
            WebDriverWait ajaxWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            ajaxWait.until(wd -> {
                Boolean jQueryDefined = (Boolean) js.executeScript("return typeof jQuery != 'undefined'");
                if (jQueryDefined) {
                    Long activeConnections = (Long) js.executeScript("return jQuery.active");
                    return activeConnections == 0;
                }
                return true;
            });
            log.debug("Requisições AJAX concluídas");
        } catch (Exception e) {
            log.debug("Não foi possível verificar jQuery.active, continuando...");
        }
    }

    /**
     * Valida se a seleção foi bem-sucedida
     */
    private void validarSelecao(WebElement campo, String textoDigitado, String textoEsperado) 
            throws InterruptedException {

        Thread.sleep(500);
        String valorFinal = campo.getAttribute("value");

        log.debug("Validando seleção");
        log.debug("Texto digitado: '{}'", textoDigitado);
        log.debug("Texto esperado: '{}'", textoEsperado);
        log.debug("Valor final no campo: '{}'", valorFinal);

        if (valorFinal == null || valorFinal.trim().isEmpty()) {
            log.error("❌ FALHA: Campo está vazio após seleção!");
            throw new RuntimeException("Campo de autocomplete ficou vazio após tentativa de seleção");
        } 

        // Verifica se o valor contém parte do texto esperado
        if (valorFinal.toUpperCase().contains(textoEsperado.toUpperCase()) ||
            textoEsperado.toUpperCase().contains(valorFinal.toUpperCase())) {
            log.info("✓ Validação OK: Campo preenchido corretamente com '{}'", valorFinal);
        } else if (valorFinal.equals(textoDigitado)) {
            log.error("❌ FALHA: Valor do campo não mudou após seleção");
            throw new RuntimeException("Seleção de autocomplete não foi efetivada - campo não mudou");
        } else {
            log.warn("⚠ AVISO: Valor do campo difere do esperado, mas foi alterado: '{}'", valorFinal);
        }
    }
}








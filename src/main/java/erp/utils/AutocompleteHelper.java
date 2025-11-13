package erp.utils;

import java.time.Duration;

import org.openqa.selenium.By;
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
    private static final int DELAY_ENTRE_CARACTERES = 200; // ms
    private static final int DELAY_AGUARDAR_LISTA = 2000; // ms
    private static final int DELAY_APOS_HOVER = 500; // ms
    private static final int DELAY_APOS_CLICK = 1000; // ms
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;
    
    /**
     * Construtor
     * @param driver Instância do WebDriver
     */
    public AutocompleteHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_AUTOCOMPLETE));
        this.actions = new Actions(driver);
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
            
            // 2. Limpar e focar no campo
            limparEFocarCampo(campo);
            
            // 3. Digitar texto caractere por caractere (para acionar autocomplete)
            digitarTextoDevagar(campo, textoDigitar);
            
            // 4. Aguardar lista de sugestões aparecer
            aguardarListaSugestoes();
            
            // 5. Localizar a opção específica
            WebElement opcao = localizarOpcao(textoOpcaoCompleto);
            
            // 6. Realizar hover e clicar na opção
            clicarOpcaoComHover(opcao);
            
            // 7. Validar se a seleção foi bem-sucedida
            validarSelecao(campo, textoDigitar);
            
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
     * Seleciona opção usando seletor CSS para o campo
     * @param campoCssSelector Seletor CSS do campo
     * @param textoDigitar Texto para digitar
     * @param textoOpcaoCompleto Texto completo da opção
     * @throws InterruptedException
     */
    public void selecionarOpcaoPorCss(String campoCssSelector, String textoDigitar, String textoOpcaoCompleto) 
            throws InterruptedException {
        selecionarOpcao(By.cssSelector(campoCssSelector), textoDigitar, textoOpcaoCompleto);
    }
    
    /**
     * Seleciona opção usando ID do campo
     * @param campoId ID do campo
     * @param textoDigitar Texto para digitar
     * @param textoOpcaoCompleto Texto completo da opção
     * @throws InterruptedException
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
        
        // Aguarda presença no DOM
        WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        
        // Aguarda visibilidade
        wait.until(ExpectedConditions.visibilityOf(campo));
        
        // Aguarda estar clicável
        wait.until(ExpectedConditions.elementToBeClickable(campo));
        
        log.debug("Campo disponível");
        return campo;
    }
    
    /**
     * Limpa o campo e foca nele para ativar o autocomplete
     */
    private void limparEFocarCampo(WebElement campo) {
        log.debug("Limpando e focando no campo");
        
        campo.clear();
        campo.click();
        
        log.debug("Campo limpo e focado");
    }
    
    /**
     * Digita o texto caractere por caractere com delay
     * Necessário para que o autocomplete processe cada caractere
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
        log.debug("Lista de sugestões deve estar visível");
    }
    
    /**
     * Localiza a opção específica na lista de autocomplete
     * Usa XPath para encontrar div com onclick contendo 'Autocomplete' e o texto especificado
     */
    private WebElement localizarOpcao(String textoCompleto) {
        log.debug("Localizando opção: '{}'", textoCompleto);
        
        // XPath baseado no código original que funcionava
        String xpathExpression = String.format(
            "//div[contains(@onclick, 'Autocomplete') and contains(text(), '%s')]",
            textoCompleto
        );
        
        By opcaoXPath = By.xpath(xpathExpression);
        
        log.debug("XPath usado: {}", xpathExpression);
        
        // Aguarda a opção estar presente
        WebElement opcao = wait.until(ExpectedConditions.presenceOfElementLocated(opcaoXPath));
        
        // Aguarda estar visível
        wait.until(ExpectedConditions.visibilityOf(opcao));
        
        String textoEncontrado = opcao.getText();
        log.debug("Opção encontrada com texto: '{}'", textoEncontrado);
        
        return opcao;
    }
    
    /**
     * Realiza hover sobre a opção e clica
     * Usa Actions para simular movimento real do mouse
     */
    private void clicarOpcaoComHover(WebElement opcao) throws InterruptedException {
        log.debug("Realizando hover na opção");
        
        // Move o mouse até a opção (hover)
        actions.moveToElement(opcao).perform();
        
        log.debug("Aguardando após hover ({} ms)", DELAY_APOS_HOVER);
        Thread.sleep(DELAY_APOS_HOVER);
        
        // Clica na opção
        log.debug("Clicando na opção");
        actions.click(opcao).perform();
        
        log.debug("Aguardando após click ({} ms)", DELAY_APOS_CLICK);
        Thread.sleep(DELAY_APOS_CLICK);
        
        log.debug("Clique realizado");
    }
    
    /**
     * Valida se a seleção foi bem-sucedida
     * Verifica se o valor do campo mudou do texto digitado
     */
    private void validarSelecao(WebElement campo, String textoDigitado) {
        String valorFinal = campo.getAttribute("value");
        
        log.debug("Validando seleção");
        log.debug("Texto digitado: '{}'", textoDigitado);
        log.debug("Valor final no campo: '{}'", valorFinal);
        
        if (valorFinal == null || valorFinal.trim().isEmpty()) {
            log.warn("AVISO: Campo está vazio após seleção!");
        } else if (valorFinal.equals(textoDigitado)) {
            log.warn("AVISO: Valor do campo não mudou. Pode não ter sido selecionado corretamente.");
        } else {
            log.info("Validação OK: Campo preenchido com '{}'", valorFinal);
        }
    }
    
    // =======================================================
    // MÉTODOS ESTÁTICOS LEGADOS (para compatibilidade)
    // =======================================================
    
    /**
     * Método estático simplificado (mantido para compatibilidade)
     * Recomenda-se usar a instância da classe para maior controle
     * 
     * @deprecated Use a instância da classe para melhor controle e logs
     */
    @Deprecated
    public static void selecionarValorAutocomplete(
            WebDriver driver,
            String campoAutocompleteCssSelector,
            String valorParaSelecionar) {
        
        log.warn("Usando método estático deprecated. Considere usar instância da classe.");
        
        WebElement campoAutocomplete = driver.findElement(
            By.cssSelector(campoAutocompleteCssSelector)
        );
        
        campoAutocomplete.clear();
        campoAutocomplete.sendKeys(valorParaSelecionar);
    }
}


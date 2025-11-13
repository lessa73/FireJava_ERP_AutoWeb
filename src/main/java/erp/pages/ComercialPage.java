package erp.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import erp.utils.AutocompleteHelper;

public class ComercialPage extends BasePages {

    private static final Logger log = LoggerFactory.getLogger(ComercialPage.class);
    private final AutocompleteHelper autocompleteHelper;

    // =======================================================
    // CONSTANTES
    // =======================================================
    private static final String IFRAME_PROPOSTA = "iframe[src*='proposta.do']";
    private static final int DELAY_PADRAO = 3000;

    // =======================================================
    // LOCATORS DE LOGIN
    // =======================================================
    private final By inputUsuario = By.id("username");
    private final By inputSenha = By.id("password");
    private final By btnLogin = By.cssSelector("input[type='submit'][value='Login']");
    private final By menuPrincipal = By.id("menuDiv");

    // =======================================================
    // LOCATORS DO MENU
    // =======================================================
    private final By menuComercial = By.xpath("//div[@id='ThemeOffice_Comercial' and text()='Comercial']");
    private final By menuProposta = By.xpath("//div[@id='cmSubMenuID19_Proposta' and text()='Proposta']");
    private final By menuCadastro = By.xpath("//div[@id='cmSubMenuID35_Cadastro' and text()='Cadastro']");

    // =======================================================
    // LOCATORS DOS CAMPOS
    // =======================================================
    private final By inputPrazoDias = By.id("prazoDias");
    private final By inputCliente = By.id("cliente");
    private final By inputVendedor = By.id("representante");
    private final By inputUtilizacao = By.id("utilizacao");
    private final By inputTabelaPreco = By.id("tabelaPreco");
    private final By inputIndicadorOperacao = By.id("indicadorOperacao");

    // =======================================================
    // CONSTRUTOR
    // =======================================================
    public ComercialPage(WebDriver driver) {
        super(driver);
        this.autocompleteHelper = new AutocompleteHelper(driver);
    }

    // =======================================================
    // MÉTODOS DE LOGIN
    // =======================================================
    
    /**
     * Abre a URL do sistema
     * @param url URL do sistema ERP
     */
    public void abrir(String url) {
        log.info("Abrindo URL: {}", url);
        driver.get(url);
    }

    /**
     * Preenche o campo de usuário
     * @param usuario Nome do usuário
     */
    public void preencherUsuario(String usuario) {
        log.debug("Preenchendo usuário: {}", usuario);
        wait.until(ExpectedConditions.elementToBeClickable(inputUsuario)).sendKeys(usuario);
    }

    /**
     * Preenche o campo de senha
     * @param senha Senha do usuário
     */
    public void preencherSenha(String senha) {
        log.debug("Preenchendo senha");
        wait.until(ExpectedConditions.elementToBeClickable(inputSenha)).sendKeys(senha);
    }

    /**
     * Clica no botão de login
     */
    public void clicarLogin() {
        log.debug("Clicando no botão de login");
        wait.until(ExpectedConditions.elementToBeClickable(btnLogin)).click();
    }

    /**
     * Valida se o login foi realizado com sucesso
     * @return true se o menu principal estiver visível
     */
    public boolean loginComSucesso() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(menuPrincipal));
            log.info("Login realizado com sucesso");
            return true;
        } catch (Exception e) {
            log.error("Falha no login: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Realiza o login completo no sistema
     * @param url URL do sistema
     * @param usuario Nome do usuário
     * @param senha Senha do usuário
     * @return true se o login foi bem-sucedido
     */
    public boolean realizarLoginCompleto(String url, String usuario, String senha) {
        log.info("Iniciando processo de login completo");
        abrir(url);
        preencherUsuario(usuario);
        preencherSenha(senha);
        clicarLogin();
        return loginComSucesso();
    }

    // =======================================================
    // MÉTODOS DE NAVEGAÇÃO
    // =======================================================
    
    /**
     * Acessa a tela de cadastro de proposta através do menu
     */
    public void acessarTelaCadastro() {
        try {
            log.info("Acessando tela de cadastro de proposta...");

            // Clica no menu Comercial
            clicarElemento(menuComercial);

            // Hover no menu Proposta
            Actions actions = new Actions(driver);
            WebElement proposta = wait.until(ExpectedConditions.visibilityOfElementLocated(menuProposta));
            actions.moveToElement(proposta).perform();

            // Clica em Cadastro
            clicarElemento(menuCadastro);

            // Muda para o iframe da proposta
            mudarParaIframe(IFRAME_PROPOSTA);
            aguardarIframeCarregarCompletamente();
            
            // Aguarda campo prazo estar disponível (indicador de carregamento completo)
            aguardarElementoClicavel(inputPrazoDias);

            log.info("Tela de cadastro carregada com sucesso");

        } catch (Exception e) {
            log.error("Erro ao acessar tela de cadastro: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao acessar tela de cadastro de proposta", e);
        }
    }

    // =======================================================
    // MÉTODOS DE PREENCHIMENTO DE CAMPOS
    // =======================================================
    
    /**
     * Preenche o campo de prazo em dias
     * @param dias Quantidade de dias
     */
    public void preencherPrazo(String dias) {
        log.info("Preenchendo prazo: {} dias", dias);
        preencherCampo(inputPrazoDias, dias);
        aguardarPaginaCarregar();
    }

    /**
     * Seleciona cliente usando autocomplete
     * Utiliza a classe AutocompleteHelper para realizar a seleção
     * 
     * @param nomeCliente Nome do cliente para busca (ex: "DESTOM INDUSTRIA")
     * @param cnpjENomeCompleto CNPJ e nome completo da opção (ex: "20.746.370/0001-80 - DESTOM INDUSTRIA E COMERCIO IMPORTACAO E EXPORTACAO LTDA")
     */
    public void selecionarCliente(String nomeCliente, String cnpjENomeCompleto) {
        try {
            log.info("Iniciando seleção de cliente: {}", nomeCliente);
            
            // Aguarda a página estar estável
            aguardarPaginaCarregar();
            aguardarIntervalo(DELAY_PADRAO);
            aguardarElementoClicavel(inputCliente);
            aguardarAjaxCompletar();
            
            // Delega a seleção para o AutocompleteHelper
            autocompleteHelper.selecionarOpcao(inputCliente, nomeCliente, cnpjENomeCompleto);
            
            log.info("Cliente '{}' selecionado com sucesso", nomeCliente);
            
        } catch (Exception e) {
            log.error("Erro ao selecionar cliente '{}': {}", nomeCliente, e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar cliente: " + nomeCliente, e);
        }
    }

    /**
     * Seleciona vendedor usando autocomplete
     * @param nomeVendedor Nome do vendedor para busca
     * @param opcaoCompleta Texto completo da opção no autocomplete
     */
    public void selecionarVendedor(String nomeVendedor, String opcaoCompleta) {
        try {
            log.info("Selecionando vendedor: {}", nomeVendedor);
            
            aguardarPaginaCarregar();
            aguardarElementoClicavel(inputVendedor);
            aguardarAjaxCompletar();
            
            autocompleteHelper.selecionarOpcao(inputVendedor, nomeVendedor, opcaoCompleta);
            
            log.info("Vendedor selecionado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao selecionar vendedor: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar vendedor: " + nomeVendedor, e);
        }
    }

    /**
     * Seleciona utilização usando autocomplete
     * @param utilizacao Texto da utilização
     * @param opcaoCompleta Texto completo da opção no autocomplete
     */
    public void selecionarUtilizacao(String utilizacao, String opcaoCompleta) {
        try {
            log.info("Selecionando utilização: {}", utilizacao);
            
            aguardarPaginaCarregar();
            aguardarElementoClicavel(inputUtilizacao);
            aguardarAjaxCompletar();
            
            autocompleteHelper.selecionarOpcao(inputUtilizacao, utilizacao, opcaoCompleta);
            
            log.info("Utilização selecionada com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao selecionar utilização: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar utilização: " + utilizacao, e);
        }
    }

    /**
     * Seleciona tabela de preço usando autocomplete
     * @param tabelaPreco Nome da tabela
     * @param opcaoCompleta Texto completo da opção no autocomplete
     */
    public void selecionarTabelaPreco(String tabelaPreco, String opcaoCompleta) {
        try {
            log.info("Selecionando tabela de preço: {}", tabelaPreco);
            
            aguardarPaginaCarregar();
            aguardarElementoClicavel(inputTabelaPreco);
            aguardarAjaxCompletar();
            
            autocompleteHelper.selecionarOpcao(inputTabelaPreco, tabelaPreco, opcaoCompleta);
            
            log.info("Tabela de preço selecionada com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao selecionar tabela de preço: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar tabela de preço: " + tabelaPreco, e);
        }
    }

    // =======================================================
    // MÉTODOS UTILITÁRIOS
    // =======================================================
    
    /**
     * Aguarda um tempo específico com log detalhado
     * @param segundos Tempo em segundos
     * @param motivo Motivo da espera (para log)
     */
    public void aguardarSegundos(int segundos, String motivo) {
        try {
            log.info("Aguardando {} segundos - Motivo: {}", segundos, motivo);
            Thread.sleep(segundos * 1000);
            log.debug("Aguardo de {} segundos concluído", segundos);
        } catch (InterruptedException e) {
            log.warn("Interrupção durante aguardo: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    // ATENÇÃO: MÉTODOS REMOVIDOS DAQUI
    // aguardarPaginaCarregar(), aguardarIntervalo(), aguardarAjaxCompletar()
    // e aguardarIframeCarregarCompletamente() já estão implementados na BasePages
}

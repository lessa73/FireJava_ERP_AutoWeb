package erp.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import erp.utils.AutocompleteHelper;
import erp.utils.Select2Helper;

public class ComercialPage {

    private static final Logger log = LoggerFactory.getLogger(ComercialPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    private final AutocompleteHelper autocompleteHelper;
    private final Select2Helper select2Helper;

    // Locators
    private final By campoUsuario = By.id("usuario");
    private final By campoSenha = By.id("senha");
    private final By botaoEntrar = By.cssSelector("button[type='submit']");

    private final By menuComercial = By.xpath("//a[@href='/fluxis/comercial/comercial_menu.do']");
    private final By opcaoNovaProposta = By.xpath("//a[@href='/fluxis/comercial/cadastro_proposta.do']");

    private final By campoPrazo = By.id("prazo");
    private final By campoCliente = By.id("cliente");
    private final By campoVendedor = By.id("representante");
    private final By selectUtilizacao = By.id("utilizacao");

    public ComercialPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
        this.autocompleteHelper = new AutocompleteHelper(driver);
        this.select2Helper = new Select2Helper(driver);
        log.info("ComercialPage inicializada");
    }

    /**
     * Realiza o login completo no sistema
     */
    public boolean realizarLoginCompleto(String url, String usuario, String senha) {
        try {
            log.info("Acessando URL: {}", url);
            driver.get(url);

            aguardarSegundos(2, "Aguardando página de login carregar");

            log.info("Preenchendo usuário: {}", usuario);
            WebElement campoUser = wait.until(ExpectedConditions.presenceOfElementLocated(campoUsuario));
            campoUser.clear();
            campoUser.sendKeys(usuario);

            log.info("Preenchendo senha");
            WebElement campoPass = driver.findElement(campoSenha);
            campoPass.clear();
            campoPass.sendKeys(senha);

            log.info("Clicando em Entrar");
            WebElement btnEntrar = driver.findElement(botaoEntrar);
            btnEntrar.click();

            aguardarSegundos(3, "Aguardando login ser processado");

            String urlAtual = driver.getCurrentUrl();
            log.info("URL após login: {}", urlAtual);

            boolean loginSucesso = !urlAtual.contains("login.do");

            if (loginSucesso) {
                log.info("✓ Login realizado com sucesso!");
            } else {
                log.error("✗ Login falhou - ainda na página de login");
            }

            return loginSucesso;

        } catch (Exception e) {
            log.error("Erro ao realizar login: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Acessa a tela de cadastro de proposta
     */
    public void acessarTelaCadastro() {
        try {
            log.info("Navegando para menu Comercial");

            WebElement menuCom = wait.until(ExpectedConditions.elementToBeClickable(menuComercial));
            menuCom.click();

            aguardarSegundos(2, "Aguardando menu expandir");

            log.info("Clicando em Nova Proposta");
            WebElement novaProp = wait.until(ExpectedConditions.elementToBeClickable(opcaoNovaProposta));
            novaProp.click();

            aguardarSegundos(2, "Aguardando tela de cadastro carregar");

            log.info("✓ Tela de cadastro acessada");

        } catch (Exception e) {
            log.error("Erro ao acessar tela de cadastro: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao acessar tela de cadastro", e);
        }
    }

    /**
     * Preenche o campo Prazo
     */
    public void preencherPrazo(String prazo) {
        try {
            log.info("Preenchendo prazo: {} dias", prazo);

            WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(campoPrazo));
            wait.until(ExpectedConditions.visibilityOf(campo));

            campo.clear();
            campo.sendKeys(prazo);

            log.info("✓ Prazo preenchido");

        } catch (Exception e) {
            log.error("Erro ao preencher prazo: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao preencher prazo", e);
        }
    }

    /**
     * Seleciona o cliente usando autocomplete
     */
    public void selecionarCliente(String textoDigitar, String opcaoCompleta) {
        try {
            log.info("Selecionando cliente");
            autocompleteHelper.selecionarOpcao(campoCliente, textoDigitar, opcaoCompleta);
            log.info("✓ Cliente selecionado");
        } catch (Exception e) {
            log.error("Erro ao selecionar cliente: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar cliente", e);
        }
    }

    /**
     * Seleciona o vendedor usando autocomplete
     */
    public void selecionarVendedor(String textoDigitar, String opcaoCompleta) {
        try {
            log.info("Selecionando vendedor");
            autocompleteHelper.selecionarOpcao(campoVendedor, textoDigitar, opcaoCompleta);
            log.info("✓ Vendedor selecionado");
        } catch (Exception e) {
            log.error("Erro ao selecionar vendedor: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar vendedor", e);
        }
    }

    /**
     * Seleciona a utilização usando Select2
     */
    public void selecionarUtilizacao(String textoOpcao) {
        try {
            log.info("Selecionando utilização: {}", textoOpcao);
            select2Helper.selecionarOpcao("utilizacao", textoOpcao);
            log.info("✓ Utilização selecionada");
        } catch (Exception e) {
            log.error("Erro ao selecionar utilização: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar utilização", e);
        }
    }

    /**
     * Seleciona a utilização por valor (value do option)
     */
    public void selecionarUtilizacaoPorValor(String valor) {
        try {
            log.info("Selecionando utilização por valor: {}", valor);
            select2Helper.selecionarPorValor("utilizacao", valor);
            log.info("✓ Utilização selecionada por valor");
        } catch (Exception e) {
            log.error("Erro ao selecionar utilização por valor: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao selecionar utilização por valor", e);
        }
    }

    /**
     * Aguarda um tempo específico
     */
    public void aguardarSegundos(int segundos, String mensagem) {
        try {
            if (mensagem != null && !mensagem.isEmpty()) {
                log.info(mensagem);
            }
            Thread.sleep(segundos * 1000L);
        } catch (InterruptedException e) {
            log.warn("Aguardo interrompido");
            Thread.currentThread().interrupt();
        }
    }
}

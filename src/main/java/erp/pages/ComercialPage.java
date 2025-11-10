package erp.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ComercialPage extends BasePages {

    private static final Logger log = LoggerFactory.getLogger(ComercialPage.class);

    public ComercialPage(WebDriver driver) {
        // Chama o construtor da classe pai (BasePages), 
        // passando o objeto 'driver' para inicializar 'this.driver' e 'this.wait' lá.
        super(driver); 
    }

    // =======================================================
    // LOCATORS DE LOGIN (MOVIDOS DA ANTIGA LoginPage)
    // =======================================================
    private final By inputUsuario = By.id("username");
    private final By inputSenha   = By.id("password");
    private final By btnLogin     = By.cssSelector("input[type='submit'][value='Login']");
    private final By menuPrincipal = By.id("menuDiv"); // Validação de sucesso
    // =======================================================


    // Locators do menu
    private final By menuComercial = By.xpath("//div[@id='ThemeOffice_Comercial' and text()='Comercial']");
    private final By menuProposta = By.xpath("//div[@id='cmSubMenuID19_Proposta' and text()='Proposta']");
    private final By menuCadastro = By.xpath("//div[@id='cmSubMenuID35_Cadastro' and text()='Cadastro']");

    // Locators dos campos
    private final By inputPrazoDias = By.id("prazoDias");
    private final By inputCliente = By.id("cliente");
    private final By inputVendedor = By.id("representante");
    private final By inputUtilizacao = By.id("utilizacao");
    private final By inputTabelaPreco = By.id("tabelaPreco");
    private final By inputIndicadorOperacao = By.id("indicadorOperacao");

    // Após login, valida pelo container do menu
    //private final By menuPrincipal = By.id("menuDiv");

    public void abrir(String url) {
        driver.get(url);
    }

    public void preencherUsuario(String usuario) {
        wait.until(ExpectedConditions.elementToBeClickable(inputUsuario)).sendKeys(usuario);
    }

    public void preencherSenha(String senha) {
        wait.until(ExpectedConditions.elementToBeClickable(inputSenha)).sendKeys(senha);
    }

    public void clicarLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(btnLogin)).click();
    }

    public boolean loginComSucesso() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(menuPrincipal));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean realizarLoginCompleto(String url, String usuario, String senha) {
        abrir(url);
        preencherUsuario(usuario);
        preencherSenha(senha);
        clicarLogin();
        return loginComSucesso();
    }
}
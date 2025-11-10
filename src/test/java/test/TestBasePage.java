package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import erp.config.ConfigManager;


public abstract class TestBasePage {
    protected WebDriver driver;
    
    @BeforeEach
    public void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        
        // Configurações do Firefox
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", 
            "application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        if (Boolean.parseBoolean(ConfigManager.getProperty("browser.headless", "false"))) {
            options.addArguments("--headless");
        }
        
        driver = new FirefoxDriver(options);
        
        if (Boolean.parseBoolean(ConfigManager.getProperty("browser.maximize", "true"))) {
            driver.manage().window().maximize();
        }
    }
    
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    protected void login() {
        // Implementar login reutilizável
    }
}
package erp.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();
    
    static {
        try {
            FileInputStream file = new FileInputStream("src/test/resources/config.properties");
            properties.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dados de teste - podem ser movidos para arquivo de configuração
 /*    private static final String URL = System.getProperty("erp.url", "http://www.fluxis.com.br:8083/fluxis/login.do");
    private static final String USUARIO = System.getProperty("username", "alexandre.lessa@celer.matriz");
    private static final String SENHA = System.getProperty("password", "0"); */
    
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
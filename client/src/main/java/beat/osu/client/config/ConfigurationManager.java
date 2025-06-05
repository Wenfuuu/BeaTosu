package beat.osu.client.config;

import beat.osu.client.service.ClientService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Properties properties;

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    private ConfigurationManager() {
        loadProperties();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    private String getStringProperty(String key) {
        return properties.getProperty(key);
    }

    private Integer getIntegerProperty(String key) {
        String value = getStringProperty(key);
        return Integer.parseInt(value);
    }

    public String getServerHost() {
        return getStringProperty("server.host");
    }

    public int getServerPort() {
        return getIntegerProperty("server.port");
    }

    public int getConnectionTimeout() {
        return getIntegerProperty("connection.timeout");
    }

    public int getRetryAttempts() {
        return getIntegerProperty("connection.retry.attempts");
    }
}

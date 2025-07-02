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
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        InputStream settingsInput = getClass().getClassLoader().getResourceAsStream("settings.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
            if (settingsInput == null) {
                throw new RuntimeException("Unable to find settings.properties");
            }
            properties.load(settingsInput);
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

    private Double getDoubleProperty(String key) {
        String value = getStringProperty(key);
        return Double.parseDouble(value);
    }

    public String getKeybind1() {
        return getStringProperty("keybind.1");
    }

    public String getKeybind2() {
        return getStringProperty("keybind.2");
    }

    public double getBgmVolume() {
        return getDoubleProperty("bgm.volume");
    }

    public double getSfxVolume() {
        return getDoubleProperty("sfx.volume");
    }

    public double getBackgroundDim() {
        return getDoubleProperty("background.dim");
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

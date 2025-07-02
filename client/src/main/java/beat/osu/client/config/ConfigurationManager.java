package beat.osu.client.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Properties properties;
    private Properties settingsProperties;
    private String settingsFilePath;

    private void loadProperties() {
        properties = new Properties();
        settingsProperties = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        InputStream settingsInput = getClass().getClassLoader().getResourceAsStream("settings.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
            
            if (settingsInput == null) {
                throw new RuntimeException("Unable to find settings.properties");
            }
            settingsProperties.load(settingsInput);
            
            // Get the path to settings.properties in src/main/resources for writing
            settingsFilePath = findSourceSettingsFile();
            System.out.println("Settings file path: " + settingsFilePath);
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
        // Check settings.properties first, then config.properties
        String value = settingsProperties.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key);
        }
        return value;
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

    public synchronized void updateSetting(String key, String value) {
        try {
            settingsProperties.setProperty(key, value);
            properties.setProperty(key, value);

            writeSettingsToFile();
            
            System.out.println("Updated setting: " + key + " = " + value);
        } catch (Exception e) {
            System.err.println("Error updating setting " + key + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void writeSettingsToFile() throws IOException {
        if (settingsFilePath == null) {
            System.err.println("Settings file path not found, cannot write settings");
            return;
        }
        
        File settingsFile = new File(settingsFilePath);
        try (FileOutputStream output = new FileOutputStream(settingsFile)) {
            settingsProperties.store(output, "");
//            System.out.println("Successfully wrote settings to: " + settingsFilePath);
        } catch (IOException e) {
            throw e;
        }
    }
    
    public void setKeybind1(String keybind) {
        updateSetting("keybind.1", keybind);
    }
    
    public void setKeybind2(String keybind) {
        updateSetting("keybind.2", keybind);
    }
    
    public void setBgmVolume(double volume) {
        updateSetting("bgm.volume", String.valueOf(volume));
    }
    
    public void setSfxVolume(double volume) {
        updateSetting("sfx.volume", String.valueOf(volume));
    }
    
    public void setBackgroundDim(double dim) {
        updateSetting("background.dim", String.valueOf(dim));
    }
    
    private String findSourceSettingsFile() {
        try {
            String currentDir = System.getProperty("user.dir");
            System.out.println("Current working directory: " + currentDir);
            
            String[] possiblePaths = {
                currentDir + "/src/main/resources/settings.properties",
                currentDir + "/client/src/main/resources/settings.properties"
            };
            
            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists() && file.canWrite()) {
                    System.out.println("Found writable settings file at: " + path);
                    return file.getAbsolutePath();
                }
            }
            
            // If not found, create the path to the expected location
            String defaultPath = currentDir + "/client/src/main/resources/settings.properties";
            File defaultFile = new File(defaultPath);
            if (defaultFile.getParentFile().exists()) {
                System.out.println("Using default settings file path: " + defaultPath);
                return defaultFile.getAbsolutePath();
            }
            
            System.err.println("Could not find source settings.properties file, using fallback");
            return null;
            
        } catch (Exception e) {
            System.err.println("Error finding source settings file: " + e.getMessage());
            return null;
        }
    }
}

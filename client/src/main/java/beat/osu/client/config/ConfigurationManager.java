package beat.osu.client.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Properties properties;
    private String settingsFilePath;

    private void loadProperties() {
        properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);

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

    public boolean getIgnoreBeatmapHitsounds() {
        String value = getStringProperty("ignore.beatmap.hitsounds");
        return value != null ? Boolean.parseBoolean(value) : true; // Default to true
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
            properties.store(output, null);
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

    public void setIgnoreBeatmapHitsounds(boolean ignore) {
        updateSetting("ignore.beatmap.hitsounds", String.valueOf(ignore));
    }

    private String findSourceSettingsFile() {
        try {
            String currentDir = System.getProperty("user.dir");
            System.out.println("Current working directory: " + currentDir);

            String[] possiblePaths = {
                    currentDir + "/src/main/resources/config.properties",
                    currentDir + "/client/src/main/resources/config.properties"
            };

            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists() && file.canWrite()) {
                    System.out.println("Found writable settings file at: " + path);
                    return file.getAbsolutePath();
                }
            }

            String defaultPath = currentDir + "/client/src/main/resources/config.properties";
            File defaultFile = new File(defaultPath);
            if (defaultFile.getParentFile().exists()) {
                System.out.println("Using default settings file path: " + defaultPath);
                return defaultFile.getAbsolutePath();
            }

            System.err.println("Could not find source config.properties file, using fallback");
            return null;

        } catch (Exception e) {
            System.err.println("Error finding source settings file: " + e.getMessage());
            return null;
        }
    }
}

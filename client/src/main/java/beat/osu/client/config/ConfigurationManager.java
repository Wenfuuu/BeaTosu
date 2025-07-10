package beat.osu.client.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Properties properties;
    private LinkedHashMap<String, String> orderedProperties;
    private String settingsFilePath;

    private void loadProperties() {
        properties = new Properties();
        orderedProperties = new LinkedHashMap<>();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.properties"))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        int equalIndex = line.indexOf('=');
                        if (equalIndex > 0) {
                            String key = line.substring(0, equalIndex).trim();
                            String value = line.substring(equalIndex + 1).trim();
                            orderedProperties.put(key, value);
                        }
                    }
                }
            }

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
        return value != null ? Boolean.parseBoolean(value) : true;
    }

    public String getServerHost() {
        return getStringProperty("server.host");
    }

    public int getServerPort() {
        return getIntegerProperty("server.port");
    }

    public synchronized void updateSetting(String key, String value) {
        try {
            properties.setProperty(key, value);
            orderedProperties.put(key, value);

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
        try (PrintWriter writer = new PrintWriter(new FileWriter(settingsFile))) {
            writer.println("# Application Settings");
            
            for (Map.Entry<String, String> entry : orderedProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key.equals("server.host")) {
                    writer.println();
                    writer.println("# Server Configuration");
                } else if (key.equals("connection.timeout")) {
                    writer.println();
                    writer.println("# Connection Configuration");
                }
                
                writer.println(key + "=" + value);
            }
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
}
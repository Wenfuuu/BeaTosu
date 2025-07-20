package beat.osu.client.config;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Properties properties;
    private LinkedHashMap<String, String> orderedProperties;

    private void loadProperties() {
        properties = new Properties();
        orderedProperties = new LinkedHashMap<>();

        File configFile = new File("config.properties");

        if (!configFile.exists()) {
            if (!configFile.exists()) {
                orderedProperties.put("keybind.1", "Z");
                orderedProperties.put("keybind.2", "X");
                orderedProperties.put("sfx.volume", "0.5");
                orderedProperties.put("background.dim", "0.8");
                orderedProperties.put("ignore.beatmap.hitsounds", "true");
                orderedProperties.put("bgm.volume", "0.15");

                orderedProperties.put("server.host", "localhost");
                orderedProperties.put("server.port", "8081");

                orderedProperties.put("connection.timeout", "5000");
                orderedProperties.put("connection.retry.attempts", "3");

                properties.putAll(orderedProperties);

                try {
                    writeSettingsToFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write default configuration", e);
                }

                return;
            }
        }

        try (FileInputStream input = new FileInputStream(configFile)) {
            properties.load(input);

            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
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
        File settingsFile = new File("config.properties");
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
package beat.osu.server.config;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
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
            orderedProperties.put("server.host", "localhost");
            orderedProperties.put("server.port", "8081");
            orderedProperties.put("connection.timeout", "5000");
            orderedProperties.put("connection.retry.attempts", "3");

            properties.putAll(orderedProperties);

            try {
                writeDefaultsToFile(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write default server config", e);
            }

            return;
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

    private void writeDefaultsToFile(File configFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            writer.println("# Server Configuration");
            writer.println("server.host=localhost");
            writer.println("server.port=8081");
            writer.println();
            writer.println("# Connection Configuration");
            writer.println("connection.timeout=5000");
            writer.println("connection.retry.attempts=3");
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

package dev.carlosrr.sdds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages application configuration and settings
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "sdds_config.properties";
    private static final String LAST_DB_KEY = "last_database_path";

    private static final Properties properties;

    static {
        properties = new Properties();
        loadConfig();
    }

    /**
     * Loads configuration from the properties file
     */
    private static void loadConfig() {
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Saves configuration to the properties file
     */
    private static void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "SDDS Configuration");
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }

    /**
     * Gets the last used database path
     * @return The path to the last used database file, or null if none
     */
    public static String getLastDatabasePath() {
        return properties.getProperty(LAST_DB_KEY);
    }

    /**
     * Sets and saves the last used database path
     * @param path The path to the database file
     */
    public static void setLastDatabasePath(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(LAST_DB_KEY, path);
            saveConfig();
        }
    }

    /**
     * Clears the saved database path
     */
    public static void clearLastDatabasePath() {
        properties.remove(LAST_DB_KEY);
        saveConfig();
    }
}
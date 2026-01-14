package com.hytalecrates.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hytalecrates.CratesPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages loading and saving of all configuration files.
 */
public class ConfigManager {

    private final CratesPlugin plugin;
    private final Gson gson;
    private final File configFolder;
    private final File cratesFolder;

    private MainConfig mainConfig;
    private Map<String, CrateConfig> crateConfigs;

    public ConfigManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        this.configFolder = new File(plugin.getDataFolder(), "config");
        this.cratesFolder = new File(configFolder, "crates");
        this.crateConfigs = new HashMap<>();
    }

    /**
     * Loads all configuration files.
     */
    public void loadConfigs() {
        createDefaultConfigs();
        loadMainConfig();
        loadCrateConfigs();
    }

    /**
     * Creates default config files if they don't exist.
     */
    private void createDefaultConfigs() {
        // Create folders
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
        }

        // Copy default config.json
        File mainConfigFile = new File(configFolder, "config.json");
        if (!mainConfigFile.exists()) {
            copyResource("config/config.json", mainConfigFile);
        }

        // Copy default vote_crate.json
        File voteCrateFile = new File(cratesFolder, "vote_crate.json");
        if (!voteCrateFile.exists()) {
            copyResource("config/crates/vote_crate.json", voteCrateFile);
        }
    }

    /**
     * Copies a resource file from the JAR to the target file.
     */
    private void copyResource(String resourcePath, File target) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                Files.copy(in, target.toPath());
                plugin.getLogger().info("Created default config: " + target.getName());
            } else {
                // Resource not found, create default
                plugin.getLogger().warning("Resource not found: " + resourcePath + ", creating default");
                createDefaultFile(target, resourcePath);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to copy resource: " + resourcePath, e);
        }
    }

    /**
     * Creates a default file with basic content if resource is not available.
     */
    private void createDefaultFile(File target, String resourcePath) {
        try {
            if (resourcePath.equals("config/config.json")) {
                MainConfig defaultConfig = new MainConfig();
                saveJsonFile(target, defaultConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create default file: " + target.getName(), e);
        }
    }

    /**
     * Loads the main configuration file.
     */
    private void loadMainConfig() {
        File configFile = new File(configFolder, "config.json");
        try {
            mainConfig = loadJsonFile(configFile, MainConfig.class);
            if (mainConfig == null) {
                mainConfig = new MainConfig();
            }
            plugin.getLogger().info("Loaded main configuration.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load config.json, using defaults", e);
            mainConfig = new MainConfig();
        }
    }

    /**
     * Loads all crate configuration files from the crates folder.
     */
    private void loadCrateConfigs() {
        crateConfigs.clear();

        if (!cratesFolder.exists() || !cratesFolder.isDirectory()) {
            plugin.getLogger().warning("Crates folder not found!");
            return;
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No crate configuration files found!");
            return;
        }

        for (File file : files) {
            try {
                CrateConfig config = loadJsonFile(file, CrateConfig.class);
                if (config != null && config.isValid()) {
                    crateConfigs.put(config.getId(), config);
                    plugin.getLogger().info("Loaded crate: " + config.getId());
                } else {
                    plugin.getLogger().warning("Invalid crate config: " + file.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load crate config: " + file.getName(), e);
            }
        }

        plugin.getLogger().info("Loaded " + crateConfigs.size() + " crate(s).");
    }

    /**
     * Loads a JSON file and deserializes it to the specified class.
     */
    private <T> T loadJsonFile(File file, Class<T> clazz) {
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reading JSON file: " + file.getName(), e);
            return null;
        }
    }

    /**
     * Saves an object to a JSON file.
     */
    public <T> void saveJsonFile(File file, T object) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(object, writer);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error writing JSON file: " + file.getName(), e);
        }
    }

    /**
     * Saves a crate configuration to file.
     */
    public void saveCrateConfig(CrateConfig config) {
        File file = new File(cratesFolder, config.getId() + ".json");
        saveJsonFile(file, config);
        crateConfigs.put(config.getId(), config);
    }

    /**
     * Deletes a crate configuration file.
     */
    public boolean deleteCrateConfig(String crateId) {
        File file = new File(cratesFolder, crateId + ".json");
        if (file.exists() && file.delete()) {
            crateConfigs.remove(crateId);
            return true;
        }
        return false;
    }

    // Getters
    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public Map<String, CrateConfig> getCrateConfigs() {
        return crateConfigs;
    }

    public CrateConfig getCrateConfig(String id) {
        return crateConfigs.get(id);
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public File getCratesFolder() {
        return cratesFolder;
    }

    public Gson getGson() {
        return gson;
    }
}

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
import java.util.stream.Stream;

/**
 * Manages loading and saving of all configuration files.
 */
public class ConfigManager {

    private final CratesPlugin plugin;
    private final Gson gson;
    private final Path configFolder;
    private final Path cratesFolder;

    private MainConfig mainConfig;
    private Map<String, CrateConfig> crateConfigs;

    public ConfigManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        this.configFolder = plugin.getDataDirectory().resolve("config");
        this.cratesFolder = configFolder.resolve("crates");
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
        try {
            // Create folders
            if (!Files.exists(configFolder)) {
                Files.createDirectories(configFolder);
            }
            if (!Files.exists(cratesFolder)) {
                Files.createDirectories(cratesFolder);
            }

            // Copy default config.json
            Path mainConfigPath = configFolder.resolve("config.json");
            if (!Files.exists(mainConfigPath)) {
                copyResource("config/config.json", mainConfigPath);
            }

            // Copy default vote_crate.json
            Path voteCratePath = cratesFolder.resolve("vote_crate.json");
            if (!Files.exists(voteCratePath)) {
                copyResource("config/crates/vote_crate.json", voteCratePath);
            }
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to create default config directories");
        }
    }

    /**
     * Copies a resource file from the JAR to the target path.
     */
    private void copyResource(String resourcePath, Path target) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                Files.copy(in, target);
                plugin.getLogger().at(Level.INFO).log("Created default config: %s", target.getFileName());
            } else {
                // Resource not found, create default
                plugin.getLogger().at(Level.WARNING).log("Resource not found: %s, creating default", resourcePath);
                createDefaultFile(target, resourcePath);
            }
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to copy resource: %s", resourcePath);
        }
    }

    /**
     * Creates a default file with basic content if resource is not available.
     */
    private void createDefaultFile(Path target, String resourcePath) {
        try {
            if (resourcePath.equals("config/config.json")) {
                MainConfig defaultConfig = new MainConfig();
                saveJsonFile(target, defaultConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to create default file: %s", target.getFileName());
        }
    }

    /**
     * Loads the main configuration file.
     */
    private void loadMainConfig() {
        Path configPath = configFolder.resolve("config.json");
        try {
            mainConfig = loadJsonFile(configPath, MainConfig.class);
            if (mainConfig == null) {
                mainConfig = new MainConfig();
            }
            plugin.getLogger().at(Level.INFO).log("Loaded main configuration.");
        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to load config.json, using defaults");
            mainConfig = new MainConfig();
        }
    }

    /**
     * Loads all crate configuration files from the crates folder.
     */
    private void loadCrateConfigs() {
        crateConfigs.clear();

        if (!Files.exists(cratesFolder) || !Files.isDirectory(cratesFolder)) {
            plugin.getLogger().at(Level.WARNING).log("Crates folder not found!");
            return;
        }

        try (Stream<Path> paths = Files.list(cratesFolder)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                 .forEach(this::loadCrateConfig);
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to list crate configuration files");
            return;
        }

        if (crateConfigs.isEmpty()) {
            plugin.getLogger().at(Level.WARNING).log("No crate configuration files found!");
            return;
        }

        plugin.getLogger().at(Level.INFO).log("Loaded %d crate(s).", crateConfigs.size());
    }

    /**
     * Loads a single crate configuration file.
     */
    private void loadCrateConfig(Path path) {
        try {
            CrateConfig config = loadJsonFile(path, CrateConfig.class);
            if (config != null && config.isValid()) {
                crateConfigs.put(config.getId(), config);
                plugin.getLogger().at(Level.INFO).log("Loaded crate: %s", config.getId());
            } else {
                plugin.getLogger().at(Level.WARNING).log("Invalid crate config: %s", path.getFileName());
            }
        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to load crate config: %s", path.getFileName());
        }
    }

    /**
     * Loads a JSON file and deserializes it to the specified class.
     */
    private <T> T loadJsonFile(Path path, Class<T> clazz) {
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Error reading JSON file: %s", path.getFileName());
            return null;
        }
    }

    /**
     * Saves an object to a JSON file.
     */
    public <T> void saveJsonFile(Path path, T object) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(object, writer);
        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Error writing JSON file: %s", path.getFileName());
        }
    }

    /**
     * Saves a crate configuration to file.
     */
    public void saveCrateConfig(CrateConfig config) {
        Path path = cratesFolder.resolve(config.getId() + ".json");
        saveJsonFile(path, config);
        crateConfigs.put(config.getId(), config);
    }

    /**
     * Deletes a crate configuration file.
     */
    public boolean deleteCrateConfig(String crateId) {
        Path path = cratesFolder.resolve(crateId + ".json");
        try {
            if (Files.exists(path) && Files.deleteIfExists(path)) {
                crateConfigs.remove(crateId);
                return true;
            }
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).withCause(e).log("Failed to delete crate config: %s", crateId);
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

    public Path getConfigFolder() {
        return configFolder;
    }

    public Path getCratesFolder() {
        return cratesFolder;
    }

    public Gson getGson() {
        return gson;
    }
}

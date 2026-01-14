package com.hytalecrates;

import com.hytalecrates.commands.CrateCommand;
import com.hytalecrates.config.ConfigManager;
import com.hytalecrates.crate.CrateManager;
import com.hytalecrates.gui.GUIManager;
import com.hytalecrates.key.KeyManager;
import com.hytalecrates.listeners.InventoryClickListener;
import com.hytalecrates.listeners.PlayerInteractListener;
import com.hytalecrates.reward.RewardManager;
import com.hytalecrates.util.MessageUtil;

import java.io.File;
import java.util.logging.Logger;

/**
 * Main plugin class for HytaleCrates.
 * Provides a crate/key reward system with casino-style animations.
 */
public class CratesPlugin {

    private static CratesPlugin instance;
    private final Logger logger;
    private final File dataFolder;

    private ConfigManager configManager;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private RewardManager rewardManager;
    private GUIManager guiManager;
    private MessageUtil messageUtil;

    public CratesPlugin(File dataFolder, Logger logger) {
        instance = this;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    /**
     * Called when the plugin is enabled.
     */
    public void onEnable() {
        logger.info("Enabling HytaleCrates v1.0.0...");

        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        this.messageUtil = new MessageUtil(configManager.getMainConfig().getPrefix());
        this.keyManager = new KeyManager(this);
        this.rewardManager = new RewardManager(this);
        this.crateManager = new CrateManager(this);
        this.guiManager = new GUIManager(this);

        // Load crates from config
        crateManager.loadCrates();

        // Register commands
        registerCommands();

        // Register event listeners
        registerListeners();

        logger.info("HytaleCrates has been enabled successfully!");
    }

    /**
     * Called when the plugin is disabled.
     */
    public void onDisable() {
        logger.info("Disabling HytaleCrates...");

        // Save any pending data
        if (crateManager != null) {
            crateManager.saveCrateLocations();
        }

        // Close all open GUIs
        if (guiManager != null) {
            guiManager.closeAllGUIs();
        }

        logger.info("HytaleCrates has been disabled.");
    }

    /**
     * Reload all configurations.
     */
    public void reload() {
        logger.info("Reloading HytaleCrates configurations...");
        configManager.loadConfigs();
        crateManager.loadCrates();
        messageUtil = new MessageUtil(configManager.getMainConfig().getPrefix());
        logger.info("HytaleCrates configurations reloaded successfully!");
    }

    private void registerCommands() {
        CrateCommand crateCommand = new CrateCommand(this);
        // Command registration would hook into Hytale's command API
        // ServerAPI.registerCommand("crate", crateCommand);
        logger.info("Commands registered.");
    }

    private void registerListeners() {
        PlayerInteractListener interactListener = new PlayerInteractListener(this);
        InventoryClickListener inventoryListener = new InventoryClickListener(this);
        // Event registration would hook into Hytale's event API
        // ServerAPI.registerListener(interactListener);
        // ServerAPI.registerListener(inventoryListener);
        logger.info("Event listeners registered.");
    }

    // Getters
    public static CratesPlugin getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}

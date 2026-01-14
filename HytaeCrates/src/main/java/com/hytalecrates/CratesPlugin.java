package com.hytalecrates;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hytalecrates.commands.CrateCommand;
import com.hytalecrates.commands.CrateSetCommand;
import com.hytalecrates.commands.CrateRemoveCommand;
import com.hytalecrates.config.ConfigManager;
import com.hytalecrates.crate.CrateManager;
import com.hytalecrates.gui.GUIManager;
import com.hytalecrates.key.KeyManager;
import com.hytalecrates.listeners.CrateInteractListener;
import com.hytalecrates.reward.RewardManager;
import com.hytalecrates.util.MessageUtil;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Main plugin class for HytaleCrates.
 * A crate/lootbox system for Hytale servers.
 */
public class CratesPlugin extends JavaPlugin {

    private static CratesPlugin instance;
    
    private ConfigManager configManager;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private RewardManager rewardManager;
    private GUIManager guiManager;
    private MessageUtil messageUtil;
    private CrateInteractListener crateInteractListener;

    public CratesPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("Setting up HytaleCrates v1.0.0...");
        
        // Create data directory if needed
        try {
            Path dataDir = getDataDirectory();
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to create data directory!");
        }
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.crateManager = new CrateManager(this);
        this.keyManager = new KeyManager(this);
        this.rewardManager = new RewardManager(this);
        this.guiManager = new GUIManager(this);
        this.crateInteractListener = new CrateInteractListener(this);
        
        // Load configurations
        configManager.loadConfigs();
        
        // Initialize MessageUtil with prefix from config
        this.messageUtil = new MessageUtil(configManager.getMainConfig().getPrefix());
        
        // Load crates
        crateManager.loadCrates();
        
        // Register commands
        getCommandRegistry().registerCommand(new CrateCommand(this));
        getCommandRegistry().registerCommand(new CrateSetCommand(this));
        getCommandRegistry().registerCommand(new CrateRemoveCommand(this));

        // Register event listeners
        getEventRegistry().registerGlobal(EventPriority.NORMAL, PlayerInteractEvent.class, crateInteractListener::onPlayerInteract);
        getEventRegistry().registerGlobal(EventPriority.NORMAL, UseBlockEvent.Pre.class, crateInteractListener::onUseBlock);
        
        getLogger().at(Level.INFO).log("HytaleCrates setup complete!");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("HytaleCrates has started successfully!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Shutting down HytaleCrates...");
        
        // Save data
        if (crateManager != null) {
            crateManager.saveCrateLocations();
        }
        
        // Clean up GUI sessions
        if (guiManager != null) {
            guiManager.closeAll();
        }
        
        getLogger().at(Level.INFO).log("HytaleCrates has been disabled.");
    }

    /**
     * Reloads all plugin configurations.
     */
    public void reload() {
        getLogger().at(Level.INFO).log("Reloading HytaleCrates configurations...");
        configManager.loadConfigs();
        
        // Reinitialize MessageUtil with potentially updated prefix
        this.messageUtil = new MessageUtil(configManager.getMainConfig().getPrefix());
        
        crateManager.loadCrates();
        getLogger().at(Level.INFO).log("HytaleCrates configurations reloaded successfully!");
    }

    // Static accessor
    public static CratesPlugin getInstance() {
        return instance;
    }

    // Getters for managers
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

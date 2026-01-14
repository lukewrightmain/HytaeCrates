package com.hytalecrates;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hytalecrates.commands.CrateCommand;
import com.hytalecrates.commands.CrateSetCommand;
import com.hytalecrates.commands.CrateRemoveCommand;
import com.hytalecrates.commands.CrateOpenCommand;
import com.hytalecrates.config.ConfigManager;
import com.hytalecrates.crate.CrateManager;
import com.hytalecrates.gui.GUIManager;
import com.hytalecrates.key.KeyManager;
import com.hytalecrates.listeners.CrateInteractListener;
import com.hytalecrates.listeners.CrateUseBlockEcsSystem;
import com.hytalecrates.reward.RewardManager;
import com.hytalecrates.util.MessageUtil;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
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
    private final Set<String> worldListenerRegistered = new HashSet<>();

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
        getCommandRegistry().registerCommand(new CrateOpenCommand(this));
        getLogger().at(Level.INFO).log("[SETUP] Commands registered (including /crateopen)");

        // Register plugin-global event listeners
        try {
            getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerInteractEvent.class, crateInteractListener::onPlayerInteract);
            getLogger().at(Level.INFO).log("[SETUP] Registered GLOBAL PlayerInteractEvent handler");
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("[SETUP] FAILED to register PlayerInteractEvent handler");
        }

        // Register PlayerMouseButtonEvent - might be more reliable than PlayerInteractEvent
        try {
            getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerMouseButtonEvent.class, crateInteractListener::onMouseButton);
            getLogger().at(Level.INFO).log("[SETUP] Registered GLOBAL PlayerMouseButtonEvent handler");
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("[SETUP] FAILED to register PlayerMouseButtonEvent handler");
        }

        // Register ECS handler for UseBlockEvent.Pre (F-to-open flow). This is NOT an IEvent.
        // It must be registered through the EntityStore ECS registry.
        try {
            getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Pre.class);
            getEntityStoreRegistry().registerSystem(new CrateUseBlockEcsSystem(crateInteractListener));
            getLogger().at(Level.INFO).log("[SETUP] Registered ECS UseBlockEvent.Pre system");
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("[SETUP] FAILED to register ECS UseBlockEvent.Pre system");
        }

        // Register per-world listeners when worlds are available (reliable for PlayerInteractEvent)
        try {
            getEventRegistry().registerGlobal(EventPriority.NORMAL, AddPlayerToWorldEvent.class, this::ensureWorldListeners);
            getEventRegistry().registerGlobal(EventPriority.NORMAL, DrainPlayerFromWorldEvent.class, this::cleanupWorldListeners);
            getLogger().at(Level.INFO).log("[SETUP] Registered AddPlayerToWorldEvent and DrainPlayerFromWorldEvent handlers");
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("[SETUP] FAILED to register world event handlers");
        }
        
        getLogger().at(Level.INFO).log("HytaleCrates setup complete!");
    }

    private void ensureWorldListeners(AddPlayerToWorldEvent event) {
        getLogger().at(Level.INFO).log("[EVENT] AddPlayerToWorldEvent FIRED");
        
        if (event == null) {
            getLogger().at(Level.WARNING).log("[EVENT] AddPlayerToWorldEvent: event is null");
            return;
        }
        if (event.getWorld() == null) {
            getLogger().at(Level.WARNING).log("[EVENT] AddPlayerToWorldEvent: world is null");
            return;
        }

        String worldName = event.getWorld().getName();
        getLogger().at(Level.INFO).log("[EVENT] AddPlayerToWorldEvent: player joining world '%s'", worldName);
        
        if (worldName == null) return;

        if (worldListenerRegistered.contains(worldName)) {
            getLogger().at(Level.INFO).log("[EVENT] World '%s' already has listeners registered", worldName);
            return;
        }

        // Register on the world's own event registry
        try {
            event.getWorld().getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerInteractEvent.class, crateInteractListener::onPlayerInteract);
            event.getWorld().getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerMouseButtonEvent.class, crateInteractListener::onMouseButton);
            worldListenerRegistered.add(worldName);
            getLogger().at(Level.INFO).log("[EVENT] SUCCESS: Registered world-scoped PlayerInteractEvent + PlayerMouseButtonEvent for world '%s'", worldName);
        } catch (Throwable t) {
            getLogger().at(Level.SEVERE).withCause(t).log("[EVENT] FAILED to register world-scoped listener for world '%s'", worldName);
        }
    }

    private void cleanupWorldListeners(DrainPlayerFromWorldEvent event) {
        // Best-effort cleanup: when a world drains/changes, allow re-registering.
        if (event == null || event.getWorld() == null) return;
        String worldName = event.getWorld().getName();
        if (worldName != null) {
            worldListenerRegistered.remove(worldName);
        }
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

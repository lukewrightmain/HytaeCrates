package com.hytalecrates.gui;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages all crate-related GUIs.
 */
public class GUIManager {

    private final CratesPlugin plugin;
    private final Map<UUID, ActiveGui> activeGuis;

    public GUIManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.activeGuis = new HashMap<>();
    }

    /**
     * Types of GUIs managed by this manager.
     */
    public enum GuiType {
        PREVIEW,
        SPIN,
        ADMIN_SETUP
    }

    /**
     * Opens the preview GUI for a crate.
     */
    public void openPreviewGui(UUID playerUuid, Crate crate) {
        // Close any existing GUI first
        closeGui(playerUuid);

        CratePreviewGUI previewGui = new CratePreviewGUI(plugin, crate);
        previewGui.open(playerUuid);

        activeGuis.put(playerUuid, new ActiveGui(GuiType.PREVIEW, crate, previewGui));
        plugin.getLogger().at(Level.INFO).log("Opened preview GUI for player %s - Crate: %s", playerUuid, crate.getId());
    }

    /**
     * Opens the spin GUI for a crate.
     */
    public void openSpinGui(UUID playerUuid, String playerName, Crate crate) {
        // Close any existing GUI first
        closeGui(playerUuid);

        CrateSpinGUI spinGui = new CrateSpinGUI(plugin, crate, playerUuid, playerName);
        spinGui.open(playerUuid);

        activeGuis.put(playerUuid, new ActiveGui(GuiType.SPIN, crate, spinGui));
        plugin.getLogger().at(Level.INFO).log("Opened spin GUI for player %s - Crate: %s", playerUuid, crate.getId());

        // Start the animation
        spinGui.startAnimation();
    }

    /**
     * Opens the admin setup GUI for creating/editing a crate.
     */
    public void openAdminSetupGui(UUID playerUuid, Crate crate) {
        // Close any existing GUI first
        closeGui(playerUuid);

        // For now, just log - would create AdminSetupGUI
        activeGuis.put(playerUuid, new ActiveGui(GuiType.ADMIN_SETUP, crate, null));
        plugin.getLogger().at(Level.INFO).log("Opened admin setup GUI for player %s", playerUuid);
    }

    /**
     * Checks if a player has an active GUI.
     */
    public boolean hasActiveGui(UUID playerUuid) {
        return activeGuis.containsKey(playerUuid);
    }

    /**
     * Gets the type of GUI a player has open.
     */
    public GuiType getGuiType(UUID playerUuid) {
        ActiveGui gui = activeGuis.get(playerUuid);
        return gui != null ? gui.type : null;
    }

    /**
     * Gets the crate associated with a player's active GUI.
     */
    public Crate getActiveCrate(UUID playerUuid) {
        ActiveGui gui = activeGuis.get(playerUuid);
        return gui != null ? gui.crate : null;
    }

    /**
     * Closes a player's active GUI.
     */
    public void closeGui(UUID playerUuid) {
        ActiveGui gui = activeGuis.remove(playerUuid);
        if (gui != null) {
            // In actual implementation, would close the inventory
            // player.closeInventory();
            plugin.getLogger().at(Level.INFO).log("Closed GUI for player %s", playerUuid);
        }
    }

    /**
     * Cleans up a player's GUI data without forcing close.
     */
    public void cleanupGui(UUID playerUuid) {
        activeGuis.remove(playerUuid);
    }

    /**
     * Closes all active GUIs (used on plugin disable).
     */
    public void closeAll() {
        for (UUID playerUuid : activeGuis.keySet()) {
            closeGui(playerUuid);
        }
        activeGuis.clear();
    }

    /**
     * Handles admin save action.
     */
    public void handleAdminSave(UUID playerUuid) {
        ActiveGui gui = activeGuis.get(playerUuid);
        if (gui == null || gui.type != GuiType.ADMIN_SETUP) {
            return;
        }

        // Save the crate configuration
        // Would gather data from the GUI and save it
        plugin.getLogger().at(Level.INFO).log("Admin saved crate configuration");
        closeGui(playerUuid);
    }

    /**
     * Handles a click on a reward slot in admin setup.
     */
    public void handleRewardSlotClick(UUID playerUuid, int slot) {
        ActiveGui gui = activeGuis.get(playerUuid);
        if (gui == null || gui.type != GuiType.ADMIN_SETUP) {
            return;
        }

        // Would handle adding/editing/removing rewards
        plugin.getLogger().at(Level.INFO).log("Admin clicked reward slot %d", slot);
    }

    /**
     * Represents an active GUI for a player.
     */
    private static class ActiveGui {
        final GuiType type;
        final Crate crate;
        final Object guiInstance;

        ActiveGui(GuiType type, Crate crate, Object guiInstance) {
            this.type = type;
            this.crate = crate;
            this.guiInstance = guiInstance;
        }
    }
}

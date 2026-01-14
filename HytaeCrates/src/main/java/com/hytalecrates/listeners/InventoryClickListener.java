package com.hytalecrates.listeners;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.gui.GUIManager;

import java.util.UUID;

/**
 * Listens for inventory/GUI click events.
 * This class would implement Hytale's inventory event listener interface.
 */
public class InventoryClickListener {

    private final CratesPlugin plugin;

    public InventoryClickListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles a click in an inventory/GUI.
     *
     * @param playerUuid The player's UUID
     * @param guiTitle The title of the GUI
     * @param slot The slot that was clicked
     * @param clickType The type of click (LEFT, RIGHT, SHIFT_LEFT, etc.)
     * @return true if the click should be cancelled
     */
    public boolean onInventoryClick(UUID playerUuid, String guiTitle, int slot, String clickType) {
        GUIManager guiManager = plugin.getGuiManager();

        // Check if player has an active crate GUI open
        if (!guiManager.hasActiveGui(playerUuid)) {
            return false;
        }

        // Get the GUI type
        GUIManager.GuiType guiType = guiManager.getGuiType(playerUuid);
        if (guiType == null) {
            return false;
        }

        switch (guiType) {
            case PREVIEW:
                return handlePreviewClick(playerUuid, slot);
            case SPIN:
                // During spin animation, cancel all clicks
                return true;
            case ADMIN_SETUP:
                return handleAdminSetupClick(playerUuid, slot);
            default:
                return false;
        }
    }

    /**
     * Handles a click in the preview GUI.
     */
    private boolean handlePreviewClick(UUID playerUuid, int slot) {
        // Preview GUI is display-only, cancel all clicks
        // Could add navigation buttons here if we want pagination
        return true;
    }

    /**
     * Handles a click in the admin setup GUI.
     */
    private boolean handleAdminSetupClick(UUID playerUuid, int slot) {
        // Handle admin setup actions based on slot
        // For example: add reward, remove reward, save, cancel
        GUIManager guiManager = plugin.getGuiManager();

        switch (slot) {
            case 45: // Save button (bottom left)
                guiManager.handleAdminSave(playerUuid);
                return true;
            case 53: // Cancel button (bottom right)
                guiManager.closeGui(playerUuid);
                return true;
            default:
                // Other slots might be reward slots
                if (slot < 45) {
                    guiManager.handleRewardSlotClick(playerUuid, slot);
                }
                return true;
        }
    }

    /**
     * Handles closing of an inventory/GUI.
     *
     * @param playerUuid The player's UUID
     * @param guiTitle The title of the GUI being closed
     */
    public void onInventoryClose(UUID playerUuid, String guiTitle) {
        GUIManager guiManager = plugin.getGuiManager();

        // Check if this was a crate GUI
        if (guiManager.hasActiveGui(playerUuid)) {
            GUIManager.GuiType guiType = guiManager.getGuiType(playerUuid);

            // If spin animation is in progress, don't clean up yet
            // The animation will handle cleanup when complete
            if (guiType != GUIManager.GuiType.SPIN) {
                guiManager.cleanupGui(playerUuid);
            }
        }
    }

    /**
     * Handles dragging items in an inventory.
     *
     * @param playerUuid The player's UUID
     * @return true if the drag should be cancelled
     */
    public boolean onInventoryDrag(UUID playerUuid) {
        // Cancel all drags in crate GUIs
        return plugin.getGuiManager().hasActiveGui(playerUuid);
    }
}

package com.hytalecrates.listeners;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.crate.CrateLocation;
import com.hytalecrates.key.CrateKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Listens for player interactions with crate blocks.
 * This class would implement Hytale's event listener interface.
 */
public class PlayerInteractListener {

    private final CratesPlugin plugin;
    private final Map<UUID, Long> cooldowns;

    public PlayerInteractListener(CratesPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    /**
     * Handles player right-click/interact on a block.
     * This would be called by Hytale's event system.
     *
     * @param playerUuid The player's UUID
     * @param playerName The player's display name
     * @param worldName The world name
     * @param blockX Block X coordinate
     * @param blockY Block Y coordinate
     * @param blockZ Block Z coordinate
     * @param heldItemNbt NBT data of held item (null if empty hand)
     * @return true if the event should be cancelled
     */
    public boolean onPlayerInteract(UUID playerUuid, String playerName, String worldName,
                                    int blockX, int blockY, int blockZ,
                                    Map<String, Object> heldItemNbt) {

        CrateLocation location = new CrateLocation(worldName, blockX, blockY, blockZ);

        // Check if this location is a crate
        Optional<Crate> crateOpt = plugin.getCrateManager().getCrateAt(location);
        if (crateOpt.isEmpty()) {
            return false; // Not a crate, don't cancel
        }

        Crate crate = crateOpt.get();

        // Check cooldown
        if (isOnCooldown(playerUuid)) {
            int remaining = getRemainingCooldown(playerUuid);
            sendMessage(playerUuid, plugin.getMessageUtil().onCooldown(remaining));
            return true; // Cancel the event
        }

        // Check if player has a key
        if (plugin.getConfigManager().getMainConfig().getSettings().isRequireKeyInHand()) {
            if (heldItemNbt == null || heldItemNbt.isEmpty()) {
                sendMessage(playerUuid, plugin.getMessageUtil().noKey());
                // Open preview GUI instead
                openPreviewGui(playerUuid, crate);
                return true;
            }

            // Validate the key
            Optional<CrateKey> keyOpt = plugin.getKeyManager().validateKeyItem(heldItemNbt);
            if (keyOpt.isEmpty()) {
                sendMessage(playerUuid, plugin.getMessageUtil().noKey());
                openPreviewGui(playerUuid, crate);
                return true;
            }

            CrateKey key = keyOpt.get();
            if (!key.getCrateId().equals(crate.getId())) {
                sendMessage(playerUuid, plugin.getMessageUtil().wrongKey());
                return true;
            }

            // Valid key - proceed to open crate
            openCrate(playerUuid, playerName, crate, key);
        } else {
            // Key not required in hand - open preview
            openPreviewGui(playerUuid, crate);
        }

        return true; // Cancel the event to prevent normal block interaction
    }

    /**
     * Handles player attempt to break a crate block.
     *
     * @param playerUuid The player's UUID
     * @param worldName The world name
     * @param blockX Block X coordinate
     * @param blockY Block Y coordinate
     * @param blockZ Block Z coordinate
     * @param hasAdminPermission Whether player has crates.admin permission
     * @return true if the break should be cancelled
     */
    public boolean onBlockBreak(UUID playerUuid, String worldName, int blockX, int blockY, int blockZ,
                                boolean hasAdminPermission) {

        if (!plugin.getConfigManager().getMainConfig().getSettings().isPreventCrateBreak()) {
            return false;
        }

        CrateLocation location = new CrateLocation(worldName, blockX, blockY, blockZ);
        if (plugin.getCrateManager().isCrateLocation(location)) {
            if (!hasAdminPermission) {
                sendMessage(playerUuid, plugin.getMessageUtil().format("&cYou cannot break a crate!"));
                return true; // Cancel break
            }
            // Admin breaking crate - remove the crate location
            plugin.getCrateManager().removeCrateLocation(location);
        }

        return false;
    }

    /**
     * Opens the crate with animation.
     */
    private void openCrate(UUID playerUuid, String playerName, Crate crate, CrateKey key) {
        // Consume the key if configured
        if (plugin.getConfigManager().getMainConfig().getSettings().isConsumeKeyOnUse()) {
            plugin.getKeyManager().consumeKey(playerUuid.toString());
        }

        // Set cooldown
        setCooldown(playerUuid);

        // Open the spin GUI
        plugin.getGuiManager().openSpinGui(playerUuid, playerName, crate);
    }

    /**
     * Opens the preview GUI for a crate.
     */
    private void openPreviewGui(UUID playerUuid, Crate crate) {
        plugin.getGuiManager().openPreviewGui(playerUuid, crate);
    }

    /**
     * Checks if a player is on cooldown.
     */
    private boolean isOnCooldown(UUID playerUuid) {
        int cooldownSeconds = plugin.getConfigManager().getMainConfig().getSettings().getCooldownSeconds();
        if (cooldownSeconds <= 0) {
            return false;
        }

        Long lastUse = cooldowns.get(playerUuid);
        if (lastUse == null) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - lastUse;
        return elapsed < (cooldownSeconds * 1000L);
    }

    /**
     * Gets remaining cooldown time in seconds.
     */
    private int getRemainingCooldown(UUID playerUuid) {
        int cooldownSeconds = plugin.getConfigManager().getMainConfig().getSettings().getCooldownSeconds();
        Long lastUse = cooldowns.get(playerUuid);
        if (lastUse == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = (cooldownSeconds * 1000L) - elapsed;
        return Math.max(0, (int) (remaining / 1000));
    }

    /**
     * Sets the cooldown for a player.
     */
    private void setCooldown(UUID playerUuid) {
        cooldowns.put(playerUuid, System.currentTimeMillis());
    }

    /**
     * Placeholder for sending messages to players.
     * Would integrate with Hytale's messaging API.
     */
    private void sendMessage(UUID playerUuid, String message) {
        // In actual implementation:
        // Player player = ServerAPI.getPlayer(playerUuid);
        // player.sendMessage(message);
        plugin.getLogger().info("[Message to " + playerUuid + "] " + message);
    }

    /**
     * Clears expired cooldowns (call periodically).
     */
    public void cleanupCooldowns() {
        int cooldownSeconds = plugin.getConfigManager().getMainConfig().getSettings().getCooldownSeconds();
        long threshold = System.currentTimeMillis() - (cooldownSeconds * 1000L * 2);
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < threshold);
    }
}

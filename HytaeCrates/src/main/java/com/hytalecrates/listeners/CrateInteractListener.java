package com.hytalecrates.listeners;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.key.CrateKey;
import com.hytalecrates.reward.Reward;
import com.hytalecrates.util.MessageUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Real server event listener for opening crates.
 * 
 * Handles multiple interaction methods:
 * - Right-click (Secondary) on crate block - preferred method
 * - F key (Use) on crate block via ECS event - if UseBlockEvent.Pre fires
 */
public final class CrateInteractListener {

    private final CratesPlugin plugin;

    public CrateInteractListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event == null) {
            return;
        }

        // DIAGNOSTIC: Log ALL PlayerInteractEvent calls to see what triggers them
        try {
            Vector3i target = event.getTargetBlock();
            var held = event.getItemInHand();
            plugin.getLogger().at(Level.INFO).log(
                    "[DIAG] PlayerInteractEvent FIRED: actionType=%s target=%s itemId=%s player=%s",
                    event.getActionType(),
                    target != null ? (target.x + "," + target.y + "," + target.z) : "null",
                    held != null ? held.getItemId() : "null",
                    event.getPlayer() != null ? event.getPlayer().getDisplayName() : "null"
            );
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).log("[DIAG] PlayerInteractEvent FIRED but error logging: %s", t.getMessage());
        }

        // Handle crate interactions for:
        // - Secondary (right-click) - always handle
        // - Use (F key) - handle if this event fires for it
        InteractionType actionType = event.getActionType();
        if (actionType != InteractionType.Secondary && actionType != InteractionType.Use) {
            // Not a crate-relevant interaction type (e.g., Primary/attack)
            return;
        }

        // Only handle block interactions
        Vector3i target = event.getTargetBlock();
        if (target == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Resolve world name from the player's entity store
        String worldName = null;
        try {
            EntityStore es = (EntityStore) event.getPlayerRef().getStore().getExternalData();
            worldName = es.getWorld().getName();
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).withCause(t).log("Failed to resolve world from PlayerInteractEvent");
        }

        // Fallback: if we can't resolve world name, don't handle.
        if (worldName == null) return;

        boolean handled = handleCrateUse(player, worldName, target, event.getItemInHand());
        if (handled) {
            // It's a crate: cancel default interaction (opening chest, etc.)
            event.setCancelled(true);
            plugin.getLogger().at(Level.INFO).log("[DIAG] Cancelled PlayerInteractEvent for crate interaction");
        }
    }

    /**
     * Handles mouse button clicks - this might be more reliable than PlayerInteractEvent.
     * We listen for RIGHT-CLICK on crate blocks.
     */
    public void onMouseButton(PlayerMouseButtonEvent event) {
        if (event == null) {
            return;
        }

        // DIAGNOSTIC: Log ALL mouse button events
        try {
            var mouseButton = event.getMouseButton();
            Vector3i target = event.getTargetBlock();
            Item heldItem = event.getItemInHand();
            plugin.getLogger().at(Level.INFO).log(
                    "[DIAG] PlayerMouseButtonEvent FIRED: button=%s state=%s target=%s itemId=%s player=%s",
                    mouseButton != null ? mouseButton.mouseButtonType : "null",
                    mouseButton != null ? mouseButton.state : "null",
                    target != null ? (target.x + "," + target.y + "," + target.z) : "null",
                    heldItem != null ? heldItem.getId() : "null",
                    event.getPlayer() != null ? event.getPlayer().getDisplayName() : "null"
            );
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).log("[DIAG] PlayerMouseButtonEvent FIRED but error logging: %s", t.getMessage());
        }

        // Only handle right-click
        var mouseButton = event.getMouseButton();
        if (mouseButton == null || mouseButton.mouseButtonType != MouseButtonType.Right) {
            return;
        }

        // Only handle block interactions
        Vector3i target = event.getTargetBlock();
        if (target == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Resolve world name from the player's entity store
        String worldName = null;
        try {
            // PlayerEvent has getPlayerRef() returning Ref<EntityStore>
            var ref = event.getPlayerRef();
            if (ref != null) {
                EntityStore es = (EntityStore) ref.getStore().getExternalData();
                if (es != null && es.getWorld() != null) {
                    worldName = es.getWorld().getName();
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).withCause(t).log("Failed to resolve world from PlayerMouseButtonEvent");
        }

        if (worldName == null) return;

        // Get the held item - need to convert from Item config to ItemStack for validation
        Item heldItemConfig = event.getItemInHand();
        ItemStack heldItem = null;
        if (heldItemConfig != null) {
            // Create a temporary ItemStack for validation
            try {
                heldItem = new ItemStack(heldItemConfig.getId(), 1);
            } catch (Throwable t) {
                plugin.getLogger().at(Level.FINE).log("Could not create ItemStack from Item config: %s", t.getMessage());
            }
        }

        boolean handled = handleCrateUse(player, worldName, target, heldItem);
        if (handled) {
            event.setCancelled(true);
            plugin.getLogger().at(Level.INFO).log("[DIAG] Cancelled PlayerMouseButtonEvent for crate interaction");
        }
    }

    /**
     * Handles the "use block" action (typically the on-screen prompt like "Press F to open").
     * This is a common hook for opening crates placed as containers (chests, etc).
     */
    public void onUseBlock(UseBlockEvent.Pre event) {
        if (event == null) {
            return;
        }

        // DIAGNOSTIC: Log when onUseBlock is called from the ECS system
        plugin.getLogger().at(Level.INFO).log("[DIAG] onUseBlock called from ECS system");

        Vector3i target = event.getTargetBlock();
        if (target == null) {
            plugin.getLogger().at(Level.INFO).log("[DIAG] onUseBlock: target is null, returning");
            return;
        }

        var ctx = event.getContext();
        if (ctx == null) {
            plugin.getLogger().at(Level.INFO).log("[DIAG] onUseBlock: context is null, returning");
            return;
        }

        // Resolve player + world from the interaction context entity ref.
        Player player;
        String worldName;
        ItemStack heldItem;
        try {
            var ref = ctx.getEntity();
            if (ref == null || !ref.isValid()) {
                return;
            }
            var store = ref.getStore();
            EntityStore es = (EntityStore) store.getExternalData();
            worldName = es.getWorld().getName();
            player = store.getComponent(ref, Player.getComponentType());
            heldItem = ctx.getHeldItem();
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).withCause(t).log("Failed to resolve player/world from UseBlockEvent");
            return;
        }

        if (player == null) {
            return;
        }

        boolean handled = handleCrateUse(player, worldName, target, heldItem);
        if (handled) {
            // It's a crate: stop default chest open
            event.setCancelled(true);
        }
    }

    /**
     * Returns true if the targeted block was a crate location (i.e. we handled it).
     */
    private boolean handleCrateUse(Player player, String worldName, Vector3i targetBlock, ItemStack heldItem) {
        if (player == null || worldName == null || targetBlock == null) {
            return false;
        }

        // Some interactions report the target block slightly offset (e.g., container top/bottom).
        Optional<Crate> crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y, targetBlock.z);
        if (crateOpt.isEmpty()) {
            crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y + 1, targetBlock.z);
        }
        if (crateOpt.isEmpty()) {
            crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y - 1, targetBlock.z);
        }
        if (crateOpt.isEmpty()) {
            return false;
        }

        Crate crate = crateOpt.get();
        plugin.getLogger().at(Level.INFO).log("Crate interaction detected: crate=%s world=%s pos=%d,%d,%d player=%s",
                crate.getId(), worldName, targetBlock.x, targetBlock.y, targetBlock.z, player.getDisplayName());
        var settings = plugin.getConfigManager().getMainConfig().getSettings();

        if (!settings.isRequireKeyInHand()) {
            player.sendMessage(MessageUtil.legacyToMessage("&7Crate preview/spin GUI is not implemented yet."));
            return true;
        }

        if (heldItem == null || heldItem.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return true;
        }

        Optional<CrateKey> keyOpt = plugin.getKeyManager().validateKeyItem(heldItem);
        if (keyOpt.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return true;
        }

        CrateKey key = keyOpt.get();
        if (!key.getCrateId().equals(crate.getId())) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThat key doesn't match this crate!"));
            return true;
        }

        Reward reward = plugin.getRewardManager().selectReward(crate);
        if (reward == null) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThis crate has no rewards configured."));
            return true;
        }

        boolean granted = plugin.getRewardManager().giveReward(player, reward);
        if (!granted) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYour inventory is full!"));
            return true;
        }

        if (settings.isConsumeKeyOnUse()) {
            plugin.getKeyManager().consumeKey(player);
        }

        player.sendMessage(MessageUtil.legacyToMessage(
                "&aYou opened " + crate.getDisplayName() + "&a and won &e" + reward.getItem().getAmount() + "x &f" + reward.getItem().getMaterial()
        ));

        return true;
    }
}



package com.hytalecrates.listeners;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.key.CrateKey;
import com.hytalecrates.reward.Reward;
import com.hytalecrates.util.MessageUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Real server event listener for opening crates by right-clicking crate blocks.
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

        // Only handle block interactions
        var target = event.getTargetBlock();
        if (target == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Resolve world name from the player's entity store
        String worldName;
        try {
            EntityStore es = (EntityStore) event.getPlayerRef().getStore().getExternalData();
            worldName = es.getWorld().getName();
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).withCause(t).log("Failed to resolve world from PlayerInteractEvent");
            return;
        }

        Optional<Crate> crateOpt = plugin.getCrateManager().getCrateAt(worldName, target.x, target.y, target.z);
        if (crateOpt.isEmpty()) {
            return;
        }

        // It's a crate: cancel default interaction (opening chest, etc.)
        event.setCancelled(true);

        Crate crate = crateOpt.get();

        // Key requirements
        var settings = plugin.getConfigManager().getMainConfig().getSettings();
        if (!settings.isRequireKeyInHand()) {
            // If keys aren't required, just show preview (GUI is still placeholder).
            player.sendMessage(MessageUtil.legacyToMessage("&7This crate requires no key (preview GUI not implemented yet)."));
            return;
        }

        var itemInHand = event.getItemInHand();
        if (itemInHand == null || itemInHand.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return;
        }

        Optional<CrateKey> keyOpt = plugin.getKeyManager().validateKeyItem(itemInHand);
        if (keyOpt.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return;
        }

        CrateKey key = keyOpt.get();
        if (!key.getCrateId().equals(crate.getId())) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThat key doesn't match this crate!"));
            return;
        }

        // Pick a reward + grant it
        Reward reward = plugin.getRewardManager().selectReward(crate);
        if (reward == null) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThis crate has no rewards configured."));
            return;
        }

        boolean granted = plugin.getRewardManager().giveReward(player, reward);
        if (!granted) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYour inventory is full!"));
            return;
        }

        // Consume key after successful grant
        if (settings.isConsumeKeyOnUse()) {
            plugin.getKeyManager().consumeKey(player);
        }

        // Tell player what they won (basic behavior; spin GUI is still a placeholder)
        Message wonMsg = Message.empty()
                .insert(MessageUtil.legacyToMessage("&aYou opened "))
                .insert(MessageUtil.legacyToMessage(crate.getDisplayName()))
                .insert(MessageUtil.legacyToMessage("&a and won &e" + reward.getItem().getAmount() + "x &f" + reward.getItem().getMaterial()));
        player.sendMessage(wonMsg);
    }

    /**
     * Handles the "use block" action (typically the on-screen prompt like "Press F to open").
     * This is the most reliable hook for opening crates placed as containers (chests, etc).
     */
    public void onUseBlock(UseBlockEvent.Pre event) {
        if (event == null) {
            return;
        }

        var target = event.getTargetBlock();
        if (target == null) {
            return;
        }

        var ctx = event.getContext();
        if (ctx == null) {
            return;
        }

        // Resolve player + world from the interaction context entity ref.
        Player player;
        String worldName;
        try {
            var ref = ctx.getEntity();
            if (ref == null || !ref.isValid()) {
                return;
            }
            var store = ref.getStore();
            EntityStore es = (EntityStore) store.getExternalData();
            worldName = es.getWorld().getName();
            player = store.getComponent(ref, Player.getComponentType());
        } catch (Throwable t) {
            plugin.getLogger().at(Level.WARNING).withCause(t).log("Failed to resolve player/world from UseBlockEvent");
            return;
        }

        if (player == null) {
            return;
        }

        Optional<Crate> crateOpt = plugin.getCrateManager().getCrateAt(worldName, target.x, target.y, target.z);
        if (crateOpt.isEmpty()) {
            return;
        }

        // It's a crate: stop default chest open
        event.setCancelled(true);

        Crate crate = crateOpt.get();
        var settings = plugin.getConfigManager().getMainConfig().getSettings();

        if (!settings.isRequireKeyInHand()) {
            player.sendMessage(MessageUtil.legacyToMessage("&7Crate preview/spin GUI is not implemented yet."));
            return;
        }

        var itemInHand = ctx.getHeldItem();
        if (itemInHand == null || itemInHand.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return;
        }

        Optional<CrateKey> keyOpt = plugin.getKeyManager().validateKeyItem(itemInHand);
        if (keyOpt.isEmpty()) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            return;
        }

        CrateKey key = keyOpt.get();
        if (!key.getCrateId().equals(crate.getId())) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThat key doesn't match this crate!"));
            return;
        }

        Reward reward = plugin.getRewardManager().selectReward(crate);
        if (reward == null) {
            player.sendMessage(MessageUtil.legacyToMessage("&cThis crate has no rewards configured."));
            return;
        }

        boolean granted = plugin.getRewardManager().giveReward(player, reward);
        if (!granted) {
            player.sendMessage(MessageUtil.legacyToMessage("&cYour inventory is full!"));
            return;
        }

        if (settings.isConsumeKeyOnUse()) {
            plugin.getKeyManager().consumeKey(player);
        }

        player.sendMessage(MessageUtil.legacyToMessage(
                "&aYou opened " + crate.getDisplayName() + "&a and won &e" + reward.getItem().getAmount() + "x &f" + reward.getItem().getMaterial()
        ));
    }
}



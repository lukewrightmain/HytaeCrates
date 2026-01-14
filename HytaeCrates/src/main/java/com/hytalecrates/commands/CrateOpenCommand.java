package com.hytalecrates.commands;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.key.CrateKey;
import com.hytalecrates.reward.Reward;
import com.hytalecrates.util.MessageUtil;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import java.util.Optional;

/**
 * /crateopen - Opens the crate block you're looking at.
 * 
 * This is a command-based fallback for opening crates, since event-based
 * interaction may not work in all cases.
 * 
 * Usage: Look at a crate block while holding the correct key, then run /crateopen
 */
public class CrateOpenCommand extends AbstractTargetPlayerCommand {

    private static final double MAX_DISTANCE = 10.0; // Max raycast distance in blocks
    
    private final CratesPlugin plugin;

    public CrateOpenCommand(CratesPlugin plugin) {
        super("crateopen", "Open the crate you're looking at");
        this.plugin = plugin;
        addAliases("openbox", "opencrate");
        requirePermission("crates.use");
    }

    @Override
    protected void execute(CommandContext ctx, 
                          Ref<EntityStore> senderRef, 
                          Ref<EntityStore> targetEntityRef,
                          PlayerRef playerRef, 
                          World world, 
                          Store<EntityStore> store) {
        
        // Get the Player component
        Player player = store.getComponent(senderRef, Player.getComponentType());
        if (player == null) {
            ctx.sendMessage(Message.raw("Could not find player data."));
            return;
        }

        // Get the block the player is looking at
        Vector3i targetBlock = TargetUtil.getTargetBlock(senderRef, MAX_DISTANCE, store);
        if (targetBlock == null) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cNo block found! Look at a crate block within " + (int)MAX_DISTANCE + " blocks."));
            return;
        }

        // Get world name
        String worldName = world.getName();

        // Check if this is a crate location (with y±1 fallback for container blocks)
        Optional<Crate> crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y, targetBlock.z);
        if (crateOpt.isEmpty()) {
            crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y + 1, targetBlock.z);
        }
        if (crateOpt.isEmpty()) {
            crateOpt = plugin.getCrateManager().getCrateAt(worldName, targetBlock.x, targetBlock.y - 1, targetBlock.z);
        }

        if (crateOpt.isEmpty()) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cThe block you're looking at is not a crate."));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Looking at: &f" + targetBlock.x + ", " + targetBlock.y + ", " + targetBlock.z));
            return;
        }

        Crate crate = crateOpt.get();
        var settings = plugin.getConfigManager().getMainConfig().getSettings();

        // Check if key is required
        if (!settings.isRequireKeyInHand()) {
            // No key required - just give reward
            giveRewardToPlayer(ctx, player, crate, settings.isConsumeKeyOnUse());
            return;
        }

        // Get the item in the player's hand
        ItemStack heldItem = null;
        try {
            heldItem = player.getInventory().getActiveHotbarItem();
        } catch (Throwable t) {
            plugin.getLogger().at(java.util.logging.Level.WARNING).withCause(t).log("Failed to get held item");
        }

        if (heldItem == null || heldItem.isEmpty()) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cYou need a key to open this crate!"));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Required key: &e" + crate.getKeyId()));
            return;
        }

        // Validate the key
        Optional<CrateKey> keyOpt = plugin.getKeyManager().validateKeyItem(heldItem);
        if (keyOpt.isEmpty()) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cThe item you're holding is not a valid crate key."));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Required key: &e" + crate.getKeyId()));
            return;
        }

        CrateKey key = keyOpt.get();
        if (!key.getCrateId().equals(crate.getId())) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cThat key doesn't match this crate!"));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Your key: &e" + key.getDisplayName()));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Required: &e" + crate.getKeyId()));
            return;
        }

        // Give reward
        giveRewardToPlayer(ctx, player, crate, settings.isConsumeKeyOnUse());
    }

    private void giveRewardToPlayer(CommandContext ctx, Player player, Crate crate, boolean consumeKey) {
        // Select a random reward
        Reward reward = plugin.getRewardManager().selectReward(crate);
        if (reward == null) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cThis crate has no rewards configured."));
            return;
        }

        // Give the reward
        boolean granted = plugin.getRewardManager().giveReward(player, reward);
        if (!granted) {
            ctx.sendMessage(MessageUtil.legacyToMessage("&cYour inventory is full!"));
            return;
        }

        // Consume the key
        if (consumeKey) {
            plugin.getKeyManager().consumeKey(player);
        }

        // Success message
        ctx.sendMessage(MessageUtil.legacyToMessage(
                "&a\u2605 You opened " + crate.getDisplayName() + " &a\u2605"
        ));
        ctx.sendMessage(MessageUtil.legacyToMessage(
                "&aYou won: &e" + reward.getItem().getAmount() + "x &f" + reward.getItem().getMaterial()
        ));
    }
}


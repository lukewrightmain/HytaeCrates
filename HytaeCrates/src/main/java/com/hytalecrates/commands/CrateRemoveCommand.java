package com.hytalecrates.commands;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.CrateLocation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

/**
 * /crateremove - Removes the crate from the block the player is looking at.
 * Uses the player's target block (raycast) instead of manual coordinates.
 */
public class CrateRemoveCommand extends AbstractTargetPlayerCommand {

    private static final double MAX_DISTANCE = 10.0;
    
    private final CratesPlugin plugin;

    public CrateRemoveCommand(CratesPlugin plugin) {
        super("crateremove", "Remove a crate from the block you're looking at");
        this.plugin = plugin;
        requirePermission("crates.admin");
    }

    @Override
    protected void execute(CommandContext ctx, 
                          Ref<EntityStore> senderRef, 
                          Ref<EntityStore> targetEntityRef,
                          PlayerRef playerRef, 
                          World world, 
                          Store<EntityStore> store) {
        
        // Get the block the player is looking at
        Vector3i targetBlock = TargetUtil.getTargetBlock(senderRef, MAX_DISTANCE, store);

        if (targetBlock == null) {
            ctx.sendMessage(Message.raw("No block found! Look at a block within " + (int)MAX_DISTANCE + " blocks."));
            return;
        }

        // Get world name
        String worldName = world.getName();

        // Create location and remove the crate
        CrateLocation location = new CrateLocation(worldName, targetBlock.x, targetBlock.y, targetBlock.z);
        
        // Check if there's a crate at this location
        var crateOpt = plugin.getCrateManager().getCrateAt(location);
        if (crateOpt.isEmpty()) {
            ctx.sendMessage(Message.raw("No crate found at " + location.toDisplayString()));
            return;
        }

        boolean success = plugin.getCrateManager().removeCrateLocation(location);

        if (success) {
            ctx.sendMessage(Message.raw("Crate removed from " + location.toDisplayString()));
        } else {
            ctx.sendMessage(Message.raw("Failed to remove crate."));
        }
    }
}



package com.hytalecrates.commands;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.crate.CrateLocation;
import com.hytalecrates.util.MessageUtil;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.StringArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

/**
 * /crate set <name> - Sets the block the player is looking at as a crate location.
 * Uses the player's target block (raycast) instead of manual coordinates.
 */
public class CrateSetCommand extends AbstractTargetPlayerCommand {

    private static final double MAX_DISTANCE = 10.0; // Max raycast distance in blocks
    
    private final CratesPlugin plugin;
    private final OptionalArg<String> crateArg;

    public CrateSetCommand(CratesPlugin plugin) {
        super("crateset", "Set the block you're looking at as a crate location");
        this.plugin = plugin;
        requirePermission("crates.admin");
        this.crateArg = withOptionalArg("crate", "The crate type to set", StringArgumentType.word());
    }

    @Override
    protected void execute(CommandContext ctx, 
                          Ref<EntityStore> senderRef, 
                          Ref<EntityStore> targetEntityRef,
                          PlayerRef playerRef, 
                          World world, 
                          Store<EntityStore> store) {
        
        if (!ctx.provided(crateArg)) {
            ctx.sendMessage(Message.raw("Usage: /crateset --crate=<name>"));
            ctx.sendMessage(Message.raw("Look at a block and run this command to set it as a crate."));
            return;
        }

        String crateId = ctx.get(crateArg).toLowerCase();
        var crateOpt = plugin.getCrateManager().getCrate(crateId);

        if (crateOpt.isEmpty()) {
            ctx.sendMessage(Message.raw("Crate not found: " + crateId));
            ctx.sendMessage(Message.raw("Available crates:"));
            for (Crate c : plugin.getCrateManager().getAllCrates()) {
                ctx.sendMessage(Message.raw("  - " + c.getId()));
            }
            return;
        }

        // Get the block the player is looking at
        Vector3i targetBlock = TargetUtil.getTargetBlock(senderRef, MAX_DISTANCE, store);

        if (targetBlock == null) {
            ctx.sendMessage(Message.raw("No block found! Look at a block within " + (int)MAX_DISTANCE + " blocks."));
            return;
        }

        // Get world name
        String worldName = world.getName();

        // Create location and set the crate
        CrateLocation location = new CrateLocation(worldName, targetBlock.x, targetBlock.y, targetBlock.z);
        boolean success = plugin.getCrateManager().setCrateLocation(crateId, location);

        if (success) {
            Crate crate = crateOpt.get();
            ctx.sendMessage(MessageUtil.legacyToMessage("&aSuccess! Set " + crate.getDisplayName() + " &aat &e" + location.toDisplayString()));
            ctx.sendMessage(MessageUtil.legacyToMessage("&7Players can now right-click this block with a &e" + crate.getKeyId() + " &7to open it!"));
        } else {
            ctx.sendMessage(Message.raw("Failed to set crate location."));
        }
    }
}



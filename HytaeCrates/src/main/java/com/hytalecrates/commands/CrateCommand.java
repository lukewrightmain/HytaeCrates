package com.hytalecrates.commands;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.crate.CrateLocation;
import com.hytalecrates.util.MessageUtil;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.StringArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Main command handler for /crate commands.
 * Handles both player and admin subcommands.
 */
public class CrateCommand extends AbstractCommand {

    private final CratesPlugin plugin;

    public CrateCommand(CratesPlugin plugin) {
        super("crate", "Main crate command for HytaleCrates");
        this.plugin = plugin;
        
        // Add aliases
        addAliases("crates", "cr");
        
        // Add subcommands
        addSubCommand(new ListSubCommand(plugin));
        addSubCommand(new PreviewSubCommand(plugin));
        addSubCommand(new InfoSubCommand(plugin));
        addSubCommand(new SetSubCommand(plugin));
        addSubCommand(new RemoveSubCommand(plugin));
        addSubCommand(new GiveSubCommand(plugin));
        addSubCommand(new ReloadSubCommand(plugin));
        addSubCommand(new HelpSubCommand(plugin));
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Show help by default
        sendHelp(ctx, ctx.sender().hasPermission("crates.admin"));
        return CompletableFuture.completedFuture(null);
    }

    private void sendHelp(CommandContext ctx, boolean isAdmin) {
        ctx.sendMessage(Message.raw("=== HytaleCrates Commands ==="));
        ctx.sendMessage(Message.raw("/crate list - List all crates"));
        ctx.sendMessage(Message.raw("/crate preview <name> - Preview crate rewards"));
        ctx.sendMessage(Message.raw("/crate info <name> - Show crate details"));

        if (isAdmin) {
            ctx.sendMessage(Message.raw("--- Admin Commands ---"));
            ctx.sendMessage(Message.raw("/crateset --crate=<name> - Set target block as crate (EASY!)"));
            ctx.sendMessage(Message.raw("/crateremove - Remove crate from target block"));
            ctx.sendMessage(Message.raw("/crate give --player=<name> --key=<key> - Give keys"));
            ctx.sendMessage(Message.raw("/crate reload - Reload configs"));
        }
    }

    // === SUBCOMMANDS ===

    /**
     * /crate list - Lists all available crates.
     */
    private static class ListSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;

        ListSubCommand(CratesPlugin plugin) {
            super("list", "List all available crates");
            this.plugin = plugin;
            requirePermission("crates.use");
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            Collection<Crate> crates = plugin.getCrateManager().getAllCrates();
            if (crates.isEmpty()) {
                ctx.sendMessage(Message.raw("No crates configured."));
                return CompletableFuture.completedFuture(null);
            }

            ctx.sendMessage(Message.raw("Available Crates:"));
            for (Crate crate : crates) {
                String info = String.format("- %s (%d rewards)",
                        crate.getDisplayName(),
                        crate.getRewardCount());
                ctx.sendMessage(MessageUtil.legacyToMessage(info));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate preview <name> - Opens preview GUI for a crate.
     */
    private static class PreviewSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;
        private final OptionalArg<String> crateArg;

        PreviewSubCommand(CratesPlugin plugin) {
            super("preview", "Preview crate rewards");
            this.plugin = plugin;
            requirePermission("crates.use");
            this.crateArg = withOptionalArg("crate", "The crate to preview", StringArgumentType.word());
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            if (!ctx.isPlayer()) {
                ctx.sendMessage(Message.raw("This command can only be used by players."));
                return CompletableFuture.completedFuture(null);
            }

            if (!ctx.provided(crateArg)) {
                ctx.sendMessage(Message.raw("Usage: /crate preview <name>"));
                return CompletableFuture.completedFuture(null);
            }

            String crateId = ctx.get(crateArg).toLowerCase();
            var crateOpt = plugin.getCrateManager().getCrate(crateId);

            if (crateOpt.isEmpty()) {
                ctx.sendMessage(Message.raw("Crate not found: " + crateId));
                return CompletableFuture.completedFuture(null);
            }

            plugin.getGuiManager().openPreviewGui(ctx.sender().getUuid(), crateOpt.get());
            ctx.sendMessage(MessageUtil.legacyToMessage("Opening preview for " + crateOpt.get().getDisplayName()));
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate info <name> - Shows detailed info about a crate.
     */
    private static class InfoSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;
        private final OptionalArg<String> crateArg;

        InfoSubCommand(CratesPlugin plugin) {
            super("info", "Show crate details");
            this.plugin = plugin;
            requirePermission("crates.use");
            this.crateArg = withOptionalArg("crate", "The crate to inspect", StringArgumentType.word());
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            if (!ctx.provided(crateArg)) {
                ctx.sendMessage(Message.raw("Usage: /crate info <name>"));
                return CompletableFuture.completedFuture(null);
            }

            String crateId = ctx.get(crateArg).toLowerCase();
            var crateOpt = plugin.getCrateManager().getCrate(crateId);

            if (crateOpt.isEmpty()) {
                ctx.sendMessage(Message.raw("Crate not found: " + crateId));
                return CompletableFuture.completedFuture(null);
            }

            Crate crate = crateOpt.get();

            ctx.sendMessage(Message.raw("=== Crate Info ==="));
            ctx.sendMessage(MessageUtil.legacyToMessage("Name: " + crate.getDisplayName()));
            ctx.sendMessage(Message.raw("ID: " + crate.getId()));
            ctx.sendMessage(Message.raw("Key: " + crate.getKeyId()));
            ctx.sendMessage(Message.raw("Rewards: " + crate.getRewardCount()));

            if (crate.hasLegendaryRewards()) {
                ctx.sendMessage(Message.raw("Contains LEGENDARY rewards!"));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate set <name> <world> <x> <y> <z> - Sets a block as a crate location.
     */
    private static class SetSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;
        private final OptionalArg<String> crateArg;
        private final OptionalArg<String> worldArg;
        private final OptionalArg<String> xArg;
        private final OptionalArg<String> yArg;
        private final OptionalArg<String> zArg;

        SetSubCommand(CratesPlugin plugin) {
            super("set", "Set a block as a crate location");
            this.plugin = plugin;
            requirePermission("crates.admin");
            this.crateArg = withOptionalArg("crate", "The crate type", StringArgumentType.word());
            this.worldArg = withOptionalArg("world", "World name", StringArgumentType.word());
            this.xArg = withOptionalArg("x", "X coordinate", StringArgumentType.word());
            this.yArg = withOptionalArg("y", "Y coordinate", StringArgumentType.word());
            this.zArg = withOptionalArg("z", "Z coordinate", StringArgumentType.word());
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            if (!ctx.provided(crateArg) || !ctx.provided(worldArg) || 
                !ctx.provided(xArg) || !ctx.provided(yArg) || !ctx.provided(zArg)) {
                ctx.sendMessage(Message.raw("Usage: /crate set <name> <world> <x> <y> <z>"));
                return CompletableFuture.completedFuture(null);
            }

            String crateId = ctx.get(crateArg).toLowerCase();
            var crateOpt = plugin.getCrateManager().getCrate(crateId);

            if (crateOpt.isEmpty()) {
                ctx.sendMessage(Message.raw("Crate not found: " + crateId));
                return CompletableFuture.completedFuture(null);
            }

            try {
                String world = ctx.get(worldArg);
                int x = Integer.parseInt(ctx.get(xArg));
                int y = Integer.parseInt(ctx.get(yArg));
                int z = Integer.parseInt(ctx.get(zArg));

                CrateLocation location = new CrateLocation(world, x, y, z);
                boolean success = plugin.getCrateManager().setCrateLocation(crateId, location);

                if (success) {
                    ctx.sendMessage(MessageUtil.legacyToMessage(
                            "Crate " + crateOpt.get().getDisplayName() + " set at " + location.toDisplayString()
                    ));
                } else {
                    ctx.sendMessage(Message.raw("Failed to set crate location."));
                }
            } catch (NumberFormatException e) {
                ctx.sendMessage(Message.raw("Invalid coordinates. Use numbers for x, y, z."));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate remove <world> <x> <y> <z> - Removes a crate from a location.
     */
    private static class RemoveSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;
        private final OptionalArg<String> worldArg;
        private final OptionalArg<String> xArg;
        private final OptionalArg<String> yArg;
        private final OptionalArg<String> zArg;

        RemoveSubCommand(CratesPlugin plugin) {
            super("remove", "Remove a crate from a location");
            this.plugin = plugin;
            requirePermission("crates.admin");
            this.worldArg = withOptionalArg("world", "World name", StringArgumentType.word());
            this.xArg = withOptionalArg("x", "X coordinate", StringArgumentType.word());
            this.yArg = withOptionalArg("y", "Y coordinate", StringArgumentType.word());
            this.zArg = withOptionalArg("z", "Z coordinate", StringArgumentType.word());
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            if (!ctx.provided(worldArg) || !ctx.provided(xArg) || 
                !ctx.provided(yArg) || !ctx.provided(zArg)) {
                ctx.sendMessage(Message.raw("Usage: /crate remove <world> <x> <y> <z>"));
                return CompletableFuture.completedFuture(null);
            }

            try {
                String world = ctx.get(worldArg);
                int x = Integer.parseInt(ctx.get(xArg));
                int y = Integer.parseInt(ctx.get(yArg));
                int z = Integer.parseInt(ctx.get(zArg));

                CrateLocation location = new CrateLocation(world, x, y, z);
                boolean success = plugin.getCrateManager().removeCrateLocation(location);

                if (success) {
                    ctx.sendMessage(Message.raw("Crate removed from " + location.toDisplayString()));
                } else {
                    ctx.sendMessage(Message.raw("No crate found at that location."));
                }
            } catch (NumberFormatException e) {
                ctx.sendMessage(Message.raw("Invalid coordinates. Use numbers for x, y, z."));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate give <player> <key> [amount] - Gives keys to a player.
     */
    private static class GiveSubCommand extends AbstractTargetPlayerCommand {
        private final CratesPlugin plugin;
        private final OptionalArg<String> keyArg;
        private final OptionalArg<String> amountArg;

        GiveSubCommand(CratesPlugin plugin) {
            super("give", "Give keys to a player");
            this.plugin = plugin;
            requirePermission("crates.admin");
            this.keyArg = withOptionalArg("key", "Key type", StringArgumentType.word());
            this.amountArg = withOptionalArg("amount", "Amount of keys", StringArgumentType.word());
        }

        @Override
        protected void execute(CommandContext ctx,
                               Ref<EntityStore> senderRef,
                               Ref<EntityStore> targetEntityRef,
                               PlayerRef targetPlayerRef,
                               World world,
                               Store<EntityStore> store) {
            if (!ctx.provided(keyArg)) {
                ctx.sendMessage(Message.raw("Usage: /crate give --player=<name> --key=<key> [--amount=<amount>]"));
                return;
            }

            String keyId = ctx.get(keyArg).toLowerCase();
            int amount = 1;

            if (ctx.provided(amountArg)) {
                try {
                    amount = Integer.parseInt(ctx.get(amountArg));
                    if (amount < 1) amount = 1;
                    if (amount > 64) amount = 64;
                } catch (NumberFormatException e) {
                    ctx.sendMessage(Message.raw("Invalid amount: " + ctx.get(amountArg)));
                    return;
                }
            }

            // Check if key exists
            var keyOpt = plugin.getKeyManager().getKey(keyId);
            if (keyOpt.isEmpty()) {
                ctx.sendMessage(Message.raw("Key not found: " + keyId));
                return;
            }

            // Give the key
            boolean success = plugin.getKeyManager().giveKey(store, targetEntityRef, keyId, amount);

            if (success) {
                String targetName = targetPlayerRef != null ? targetPlayerRef.getUsername() : "player";
                ctx.sendMessage(MessageUtil.legacyToMessage(
                        "&aGave &e" + amount + "x " + keyOpt.get().getDisplayName() + " &ato &e" + targetName
                ));
            } else {
                ctx.sendMessage(MessageUtil.legacyToMessage("&cFailed to give keys (insufficient inventory space?)"));
            }
        }
    }

    /**
     * /crate reload - Reloads all configurations.
     */
    private static class ReloadSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;

        ReloadSubCommand(CratesPlugin plugin) {
            super("reload", "Reload plugin configurations");
            this.plugin = plugin;
            requirePermission("crates.admin");
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            plugin.reload();
            ctx.sendMessage(MessageUtil.legacyToMessage("&aHytaleCrates configurations reloaded!"));
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * /crate help - Shows help.
     */
    private static class HelpSubCommand extends AbstractCommand {
        private final CratesPlugin plugin;

        HelpSubCommand(CratesPlugin plugin) {
            super("help", "Show help information");
            this.plugin = plugin;
        }

        @Override
        protected CompletableFuture<Void> execute(CommandContext ctx) {
            boolean isAdmin = ctx.sender().hasPermission("crates.admin");
            
            ctx.sendMessage(Message.raw("=== HytaleCrates Commands ==="));
            ctx.sendMessage(Message.raw("/crate list - List all crates"));
            ctx.sendMessage(Message.raw("/crate preview <name> - Preview crate rewards"));
            ctx.sendMessage(Message.raw("/crate info <name> - Show crate details"));

            if (isAdmin) {
                ctx.sendMessage(Message.raw("--- Admin Commands ---"));
                ctx.sendMessage(Message.raw("/crateset --crate=<name> - Set target block as crate"));
                ctx.sendMessage(Message.raw("/crateremove - Remove crate from target block"));
                ctx.sendMessage(Message.raw("/crate give --player=<name> --key=<key> - Give keys"));
                ctx.sendMessage(Message.raw("/crate reload - Reload configs"));
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}

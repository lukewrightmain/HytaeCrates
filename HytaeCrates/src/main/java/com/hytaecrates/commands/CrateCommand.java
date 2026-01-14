package com.hytaecrates.commands;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.crate.Crate;
import com.hytaecrates.crate.CrateLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Main command handler for /crate commands.
 * Handles both player and admin subcommands.
 */
public class CrateCommand {

    private final CratesPlugin plugin;

    public CrateCommand(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the crate command.
     *
     * @param senderUuid UUID of the command sender (null if console)
     * @param senderName Name of the command sender
     * @param args Command arguments
     * @param hasUsePermission Whether sender has crates.use permission
     * @param hasAdminPermission Whether sender has crates.admin permission
     * @return true if command was handled
     */
    public boolean execute(UUID senderUuid, String senderName, String[] args,
                           boolean hasUsePermission, boolean hasAdminPermission) {

        if (args.length == 0) {
            sendHelp(senderUuid, hasAdminPermission);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                return handleList(senderUuid, hasUsePermission);
            case "preview":
                return handlePreview(senderUuid, args, hasUsePermission);
            case "set":
                return handleSet(senderUuid, senderName, args, hasAdminPermission);
            case "remove":
                return handleRemove(senderUuid, senderName, hasAdminPermission);
            case "give":
                return handleGive(senderUuid, args, hasAdminPermission);
            case "reload":
                return handleReload(senderUuid, hasAdminPermission);
            case "create":
                return handleCreate(senderUuid, args, hasAdminPermission);
            case "delete":
                return handleDelete(senderUuid, args, hasAdminPermission);
            case "info":
                return handleInfo(senderUuid, args, hasUsePermission);
            case "help":
            default:
                sendHelp(senderUuid, hasAdminPermission);
                return true;
        }
    }

    /**
     * Handles /crate list - Lists all available crates.
     */
    private boolean handleList(UUID senderUuid, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        Collection<Crate> crates = plugin.getCrateManager().getAllCrates();
        if (crates.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&7No crates configured."));
            return true;
        }

        sendMessage(senderUuid, plugin.getMessageUtil().format("&6Available Crates:"));
        for (Crate crate : crates) {
            String info = String.format("&7- %s &8(%d rewards, %d locations)",
                    crate.getDisplayName(),
                    crate.getRewardCount(),
                    crate.getLocations().size());
            sendMessage(senderUuid, plugin.getMessageUtil().format(info));
        }

        return true;
    }

    /**
     * Handles /crate preview <name> - Opens preview GUI for a crate.
     */
    private boolean handlePreview(UUID senderUuid, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (senderUuid == null) {
            sendMessage(null, plugin.getMessageUtil().format("&cThis command can only be used by players."));
            return true;
        }

        if (args.length < 2) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate preview <name>"));
            return true;
        }

        String crateId = args[1].toLowerCase();
        var crateOpt = plugin.getCrateManager().getCrate(crateId);

        if (crateOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return true;
        }

        plugin.getGuiManager().openPreviewGui(senderUuid, crateOpt.get());
        return true;
    }

    /**
     * Handles /crate set <name> - Sets the looked-at block as a crate location.
     */
    private boolean handleSet(UUID senderUuid, String senderName, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (senderUuid == null) {
            sendMessage(null, plugin.getMessageUtil().format("&cThis command can only be used by players."));
            return true;
        }

        if (args.length < 2) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate set <name>"));
            return true;
        }

        String crateId = args[1].toLowerCase();
        var crateOpt = plugin.getCrateManager().getCrate(crateId);

        if (crateOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return true;
        }

        // In actual implementation, would get the block the player is looking at
        // Block targetBlock = player.getTargetBlock(5);
        // CrateLocation location = new CrateLocation(targetBlock.getWorld(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());

        // Placeholder - would get actual target block
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Look at a block and run this command to set a crate location."));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7(Implementation requires Hytale's raycast API)"));

        return true;
    }

    /**
     * Sets a crate at a specific location (called from game with actual coordinates).
     */
    public boolean setCrateAtLocation(UUID senderUuid, String crateId, String world, int x, int y, int z) {
        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return false;
        }

        CrateLocation location = new CrateLocation(world, x, y, z);
        boolean success = plugin.getCrateManager().setCrateLocation(crateId, location);

        if (success) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateSet(crateOpt.get().getDisplayName(), location.toDisplayString()));
        } else {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cFailed to set crate location."));
        }

        return success;
    }

    /**
     * Handles /crate remove - Removes a crate from the looked-at location.
     */
    private boolean handleRemove(UUID senderUuid, String senderName, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (senderUuid == null) {
            sendMessage(null, plugin.getMessageUtil().format("&cThis command can only be used by players."));
            return true;
        }

        // In actual implementation, would get the block the player is looking at
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Look at a crate and run this command to remove it."));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7(Implementation requires Hytale's raycast API)"));

        return true;
    }

    /**
     * Removes a crate at a specific location (called from game with actual coordinates).
     */
    public boolean removeCrateAtLocation(UUID senderUuid, String world, int x, int y, int z) {
        CrateLocation location = new CrateLocation(world, x, y, z);
        boolean success = plugin.getCrateManager().removeCrateLocation(location);

        if (success) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateRemoved(location.toDisplayString()));
        } else {
            sendMessage(senderUuid, plugin.getMessageUtil().notACrate());
        }

        return success;
    }

    /**
     * Handles /crate give <player> <key> [amount] - Gives keys to a player.
     */
    private boolean handleGive(UUID senderUuid, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (args.length < 3) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate give <player> <key> [amount]"));
            return true;
        }

        String targetPlayer = args[1];
        String keyId = args[2].toLowerCase();
        int amount = 1;

        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                sendMessage(senderUuid, plugin.getMessageUtil().format("&cInvalid amount: " + args[3]));
                return true;
            }
        }

        // Check if key exists
        var keyOpt = plugin.getKeyManager().getKey(keyId);
        if (keyOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().keyNotFound(keyId));
            return true;
        }

        // In actual implementation, would look up player UUID
        // Player target = ServerAPI.getPlayer(targetPlayer);
        // if (target == null) {
        //     sendMessage(senderUuid, plugin.getMessageUtil().format("&cPlayer not found: " + targetPlayer));
        //     return true;
        // }

        // Give the key (using player name as placeholder for UUID)
        boolean success = plugin.getKeyManager().giveKey(targetPlayer, keyId, amount);

        if (success) {
            sendMessage(senderUuid, plugin.getMessageUtil().keyGiven(keyOpt.get().getDisplayName(), amount, targetPlayer));
        } else {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cFailed to give keys."));
        }

        return true;
    }

    /**
     * Handles /crate reload - Reloads all configurations.
     */
    private boolean handleReload(UUID senderUuid, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        plugin.reload();
        sendMessage(senderUuid, plugin.getMessageUtil().configReloaded());

        return true;
    }

    /**
     * Handles /crate create <name> - Creates a new crate.
     */
    private boolean handleCreate(UUID senderUuid, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (args.length < 2) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate create <name>"));
            return true;
        }

        String crateId = args[1].toLowerCase().replaceAll("[^a-z0-9_]", "_");

        // Check if crate already exists
        if (plugin.getCrateManager().getCrate(crateId).isPresent()) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cA crate with that name already exists!"));
            return true;
        }

        // In actual implementation, would open admin setup GUI
        sendMessage(senderUuid, plugin.getMessageUtil().format("&aCreating crate: &e" + crateId));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Edit the config file at: &ecrates/" + crateId + ".json"));

        return true;
    }

    /**
     * Handles /crate delete <name> - Deletes a crate.
     */
    private boolean handleDelete(UUID senderUuid, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (args.length < 2) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate delete <name>"));
            return true;
        }

        String crateId = args[1].toLowerCase();

        if (plugin.getCrateManager().getCrate(crateId).isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return true;
        }

        boolean success = plugin.getCrateManager().deleteCrate(crateId);

        if (success) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&aDeleted crate: &e" + crateId));
        } else {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cFailed to delete crate."));
        }

        return true;
    }

    /**
     * Handles /crate info <name> - Shows detailed info about a crate.
     */
    private boolean handleInfo(UUID senderUuid, String[] args, boolean hasPermission) {
        if (!hasPermission) {
            sendMessage(senderUuid, plugin.getMessageUtil().noPermission());
            return true;
        }

        if (args.length < 2) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&cUsage: /crate info <name>"));
            return true;
        }

        String crateId = args[1].toLowerCase();
        var crateOpt = plugin.getCrateManager().getCrate(crateId);

        if (crateOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return true;
        }

        Crate crate = crateOpt.get();

        sendMessage(senderUuid, plugin.getMessageUtil().format("&6=== Crate Info ==="));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Name: " + crate.getDisplayName()));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7ID: &e" + crate.getId()));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Key: &b" + crate.getKeyId()));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Rewards: &e" + crate.getRewardCount()));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&7Locations: &e" + crate.getLocations().size()));

        if (crate.hasLegendaryRewards()) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&6✦ Contains LEGENDARY rewards!"));
        }

        return true;
    }

    /**
     * Sends help information to the sender.
     */
    private void sendHelp(UUID senderUuid, boolean isAdmin) {
        sendMessage(senderUuid, plugin.getMessageUtil().format("&6=== HytaeCrates Commands ==="));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate list &7- List all crates"));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate preview <name> &7- Preview crate rewards"));
        sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate info <name> &7- Show crate details"));

        if (isAdmin) {
            sendMessage(senderUuid, plugin.getMessageUtil().format("&6--- Admin Commands ---"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate set <name> &7- Set block as crate"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate remove &7- Remove crate from block"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate give <player> <key> [amount] &7- Give keys"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate create <name> &7- Create new crate"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate delete <name> &7- Delete a crate"));
            sendMessage(senderUuid, plugin.getMessageUtil().format("&e/crate reload &7- Reload configs"));
        }
    }

    /**
     * Provides tab completion for the command.
     */
    public List<String> tabComplete(String[] args, boolean hasAdminPermission) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            completions.add("list");
            completions.add("preview");
            completions.add("info");
            completions.add("help");

            if (hasAdminPermission) {
                completions.add("set");
                completions.add("remove");
                completions.add("give");
                completions.add("create");
                completions.add("delete");
                completions.add("reload");
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            // Second argument - crate names for most commands
            if (subCommand.equals("preview") || subCommand.equals("set") ||
                    subCommand.equals("info") || subCommand.equals("delete")) {
                completions.addAll(plugin.getCrateManager().getCrateIds());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            // Third argument - key names for give command
            if (subCommand.equals("give")) {
                completions.addAll(plugin.getKeyManager().getAllKeys().keySet());
            }
        }

        // Filter based on what user has typed
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix))
                .toList();
    }

    /**
     * Sends a message to a player or console.
     */
    private void sendMessage(UUID playerUuid, String message) {
        // In actual implementation:
        // if (playerUuid != null) {
        //     Player player = ServerAPI.getPlayer(playerUuid);
        //     player.sendMessage(message);
        // } else {
        //     ServerAPI.getConsole().sendMessage(message);
        // }

        plugin.getLogger().info("[Command] " + message);
    }
}

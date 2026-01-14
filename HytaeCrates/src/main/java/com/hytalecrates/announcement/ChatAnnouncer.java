package com.hytalecrates.announcement;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.reward.Rarity;
import com.hytalecrates.reward.Reward;
import com.hytalecrates.util.MessageUtil;

/**
 * Handles global chat announcements for crate wins.
 */
public class ChatAnnouncer {

    private final CratesPlugin plugin;

    public ChatAnnouncer(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Announces a crate win to the server.
     *
     * @param playerName The name of the player who won
     * @param crate The crate that was opened
     * @param reward The reward that was won
     */
    public void announceWin(String playerName, Crate crate, Reward reward) {
        if (!plugin.getConfigManager().getMainConfig().getAnnouncements().isEnabled()) {
            return;
        }

        // Only announce for rare+ rewards
        if (!reward.getRarity().shouldAnnounce()) {
            return;
        }

        String message = formatAnnouncement(playerName, crate, reward);
        broadcast(message);
    }

    /**
     * Formats the announcement message based on rarity.
     */
    private String formatAnnouncement(String playerName, Crate crate, Reward reward) {
        String format;

        if (reward.getRarity() == Rarity.LEGENDARY) {
            format = plugin.getConfigManager().getMainConfig().getAnnouncements().getLegendaryFormat();
            return formatLegendaryAnnouncement(format, playerName, crate, reward);
        } else {
            format = plugin.getConfigManager().getMainConfig().getAnnouncements().getFormat();
            return plugin.getMessageUtil().formatWinAnnouncement(format, playerName, crate, reward);
        }
    }

    /**
     * Formats a legendary announcement with extra flair.
     */
    private String formatLegendaryAnnouncement(String format, String playerName, Crate crate, Reward reward) {
        String message = plugin.getMessageUtil().formatLegendaryAnnouncement(format, playerName, crate, reward);

        // Add extra decorations for legendary
        StringBuilder decorated = new StringBuilder();
        decorated.append("\n");
        decorated.append(MessageUtil.colorize("&6&l✦✦✦ &e&lLEGENDARY WIN &6&l✦✦✦"));
        decorated.append("\n");
        decorated.append(message);
        decorated.append("\n");
        decorated.append(MessageUtil.colorize("&6&l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦"));
        decorated.append("\n");

        return decorated.toString();
    }

    /**
     * Broadcasts a message to all online players.
     */
    private void broadcast(String message) {
        // In actual implementation:
        // for (Player player : ServerAPI.getOnlinePlayers()) {
        //     player.sendMessage(message);
        // }

        plugin.getLogger().info("[BROADCAST] " + MessageUtil.stripColors(message));
    }

    /**
     * Sends a private win notification to the player.
     */
    public void sendPrivateWinMessage(String playerUuid, Crate crate, Reward reward) {
        String message = MessageUtil.colorize(
                plugin.getConfigManager().getMainConfig().getPrefix() +
                        "&aYou won " + reward.getRarity().getColorCode() +
                        reward.getItem().getDisplayName() + " &afrom " +
                        crate.getDisplayName() + "&a!"
        );

        // In actual implementation:
        // Player player = ServerAPI.getPlayer(playerUuid);
        // player.sendMessage(message);

        plugin.getLogger().info("[Private] " + message);
    }

    /**
     * Announces a crate setup by an admin.
     */
    public void announceCrateSetup(String adminName, String crateName, String location) {
        String message = MessageUtil.colorize(
                plugin.getConfigManager().getMainConfig().getPrefix() +
                        "&e" + adminName + " &7set up a new " + crateName + " &7at " + location
        );

        // Only broadcast to players with admin permission
        plugin.getLogger().info("[Admin] " + message);
    }

    /**
     * Creates a hover-able item display for rich chat.
     * This is a placeholder for Hytale's chat component system.
     */
    public String createItemHoverText(Reward reward) {
        StringBuilder hover = new StringBuilder();
        hover.append(reward.getRarity().getColorCode()).append(reward.getItem().getDisplayName());
        hover.append("\n");
        hover.append("&7Rarity: ").append(reward.getRarity().getColorCode()).append(reward.getRarity().name());

        if (reward.getAmount() > 1) {
            hover.append("\n");
            hover.append("&7Amount: &e").append(reward.getAmount());
        }

        if (reward.getItem().hasLore()) {
            hover.append("\n");
            for (String line : reward.getItem().getLore()) {
                hover.append("\n").append(line);
            }
        }

        return MessageUtil.colorize(hover.toString());
    }
}

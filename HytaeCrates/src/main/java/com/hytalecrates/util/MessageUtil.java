package com.hytalecrates.util;

import com.hytalecrates.crate.Crate;
import com.hytalecrates.reward.Rarity;
import com.hytalecrates.reward.Reward;

/**
 * Utility class for message formatting and color code translation.
 */
public class MessageUtil {

    private final String prefix;

    public MessageUtil(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    /**
     * Translates color codes (& to §) in a string.
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        // Replace & color codes with § (Minecraft/Hytale format)
        return message.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    /**
     * Strips color codes from a string.
     */
    public static String stripColors(String message) {
        if (message == null) {
            return "";
        }
        return message.replaceAll("[&§][0-9a-fk-orA-FK-OR]", "");
    }

    /**
     * Formats a message with the plugin prefix.
     */
    public String format(String message) {
        return colorize(prefix + message);
    }

    /**
     * Formats a win announcement message.
     */
    public String formatWinAnnouncement(String format, String playerName, Crate crate, Reward reward) {
        String message = format
                .replace("{player}", playerName)
                .replace("{crate}", crate.getDisplayName())
                .replace("{item}", reward.getItem().getDisplayName())
                .replace("{rarity}", reward.getRarity().name())
                .replace("{rarity_color}", reward.getRarity().getColorCode());

        return colorize(message);
    }

    /**
     * Formats a legendary win announcement.
     */
    public String formatLegendaryAnnouncement(String format, String playerName, Crate crate, Reward reward) {
        return formatWinAnnouncement(format, playerName, crate, reward);
    }

    /**
     * Gets the color code for a rarity.
     */
    public static String getRarityColor(Rarity rarity) {
        return rarity.getColorCode();
    }

    /**
     * Formats a chance percentage for display.
     */
    public static String formatChance(double chance) {
        if (chance >= 1) {
            return String.format("%.1f%%", chance);
        } else if (chance >= 0.1) {
            return String.format("%.2f%%", chance);
        } else {
            return String.format("%.3f%%", chance);
        }
    }

    /**
     * Creates a progress bar string.
     */
    public static String createProgressBar(double percentage, int length, String filledChar, String emptyChar) {
        int filled = (int) Math.round((percentage / 100.0) * length);
        int empty = length - filled;

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            bar.append(filledChar);
        }
        for (int i = 0; i < empty; i++) {
            bar.append(emptyChar);
        }

        return bar.toString();
    }

    /**
     * Formats an item amount for display.
     */
    public static String formatAmount(int amount) {
        if (amount <= 1) {
            return "";
        }
        return " x" + amount;
    }

    /**
     * Creates a centered message for chat.
     */
    public static String centerMessage(String message, int lineLength) {
        String stripped = stripColors(message);
        int padding = (lineLength - stripped.length()) / 2;
        if (padding <= 0) {
            return message;
        }
        return " ".repeat(padding) + message;
    }

    // Common messages
    public String noPermission() {
        return format("&cYou don't have permission to do that!");
    }

    public String crateNotFound(String crateId) {
        return format("&cCrate not found: &e" + crateId);
    }

    public String keyNotFound(String keyId) {
        return format("&cKey not found: &e" + keyId);
    }

    public String notACrate() {
        return format("&cThis block is not a crate!");
    }

    public String wrongKey() {
        return format("&cYou need the correct key to open this crate!");
    }

    public String noKey() {
        return format("&cYou need a key to open this crate!");
    }

    public String crateSet(String crateName, String location) {
        return format("&aCrate &e" + crateName + " &aset at &e" + location);
    }

    public String crateRemoved(String location) {
        return format("&aCrate removed from &e" + location);
    }

    public String keyGiven(String keyName, int amount, String playerName) {
        return format("&aGave &e" + amount + "x " + keyName + " &ato &e" + playerName);
    }

    public String keyReceived(String keyName, int amount) {
        return format("&aYou received &e" + amount + "x " + keyName);
    }

    public String configReloaded() {
        return format("&aConfiguration reloaded successfully!");
    }

    public String onCooldown(int seconds) {
        return format("&cPlease wait &e" + seconds + " &cseconds before opening another crate!");
    }
}

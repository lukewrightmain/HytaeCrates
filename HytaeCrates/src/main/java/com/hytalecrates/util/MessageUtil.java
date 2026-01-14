package com.hytalecrates.util;

import com.hytalecrates.crate.Crate;
import com.hytalecrates.reward.Rarity;
import com.hytalecrates.reward.Reward;
import com.hypixel.hytale.server.core.Message;

/**
 * Utility class for message formatting and color code translation.
 */
public class MessageUtil {

    private final String prefix;

    public MessageUtil(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    /**
     * Converts legacy color codes in a string to a plain string.
     *
     * Hytale chat does NOT render Minecraft-style § codes (and often not & codes either),
     * so for plain string contexts (GUI titles, logs, etc) we strip legacy codes.
     */
    public static String colorize(String message) {
        return stripColors(message);
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
     * Converts legacy '&' color/format codes into a real Hytale {@link Message} using the
     * server's formatting API ({@link Message#color(String)}, {@link Message#bold(boolean)}, etc).
     *
     * Supports: 0-9, a-f colors; l (bold); o (italic); r (reset).
     * Unknown codes are stripped.
     */
    public static Message legacyToMessage(String input) {
        if (input == null || input.isEmpty()) {
            return Message.empty();
        }

        StringBuilder buf = new StringBuilder();
        Message out = Message.empty();

        Style style = new Style();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));

                // Flush current text before applying new style
                if (buf.length() > 0) {
                    out.insert(applyStyle(Message.raw(buf.toString()), style));
                    buf.setLength(0);
                }

                // Apply style changes
                switch (code) {
                    case 'l' -> style.bold = true;
                    case 'o' -> style.italic = true;
                    case 'r' -> style = new Style();
                    default -> {
                        String mapped = mapLegacyColor(code);
                        if (mapped != null) {
                            style.color = mapped;
                        }
                        // else: unknown formatting code - ignore/strip
                    }
                }

                i++; // Skip code char
                continue;
            }
            buf.append(c);
        }

        if (buf.length() > 0) {
            out.insert(applyStyle(Message.raw(buf.toString()), style));
        }
        return out;
    }

    private static Message applyStyle(Message msg, Style style) {
        if (style.color != null) {
            msg.color(style.color);
        }
        msg.bold(style.bold);
        msg.italic(style.italic);
        msg.monospace(style.monospace);
        return msg;
    }

    private static String mapLegacyColor(char code) {
        // Use hex strings; MessageUtil in server also supports hex-to-style.
        return switch (code) {
            case '0' -> "#000000";
            case '1' -> "#0000AA";
            case '2' -> "#00AA00";
            case '3' -> "#00AAAA";
            case '4' -> "#AA0000";
            case '5' -> "#AA00AA";
            case '6' -> "#FFAA00";
            case '7' -> "#AAAAAA";
            case '8' -> "#555555";
            case '9' -> "#5555FF";
            case 'a' -> "#55FF55";
            case 'b' -> "#55FFFF";
            case 'c' -> "#FF5555";
            case 'd' -> "#FF55FF";
            case 'e' -> "#FFFF55";
            case 'f' -> "#FFFFFF";
            default -> null;
        };
    }

    private static final class Style {
        String color = null;
        boolean bold = false;
        boolean italic = false;
        boolean monospace = false;
    }

    /**
     * Formats a message with the plugin prefix.
     */
    public String format(String message) {
        // Keep returning a plain string for logs/other string contexts.
        return colorize(prefix + message);
    }

    /**
     * Formats a legacy-colored message with the plugin prefix into a Hytale {@link Message}.
     */
    public Message formatMessage(String message) {
        return legacyToMessage(prefix + (message != null ? message : ""));
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

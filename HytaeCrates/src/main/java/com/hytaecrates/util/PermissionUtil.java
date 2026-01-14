package com.hytaecrates.util;

import java.util.UUID;

/**
 * Utility class for permission checks.
 */
public class PermissionUtil {

    // Permission nodes
    public static final String PERM_USE = "crates.use";
    public static final String PERM_ADMIN = "crates.admin";
    public static final String PERM_PREVIEW = "crates.preview";
    public static final String PERM_BYPASS_COOLDOWN = "crates.bypass.cooldown";

    /**
     * Checks if a player has a specific permission.
     * This is a placeholder that would integrate with Hytale's permission system.
     *
     * @param playerUuid The player's UUID
     * @param permission The permission node to check
     * @return true if the player has the permission
     */
    public static boolean hasPermission(UUID playerUuid, String permission) {
        // In actual implementation:
        // Player player = ServerAPI.getPlayer(playerUuid);
        // return player.hasPermission(permission);

        // For development/testing, return true
        return true;
    }

    /**
     * Checks if a player has the basic use permission.
     */
    public static boolean canUse(UUID playerUuid) {
        return hasPermission(playerUuid, PERM_USE);
    }

    /**
     * Checks if a player has admin permission.
     */
    public static boolean isAdmin(UUID playerUuid) {
        return hasPermission(playerUuid, PERM_ADMIN);
    }

    /**
     * Checks if a player can bypass cooldowns.
     */
    public static boolean canBypassCooldown(UUID playerUuid) {
        return hasPermission(playerUuid, PERM_BYPASS_COOLDOWN) || isAdmin(playerUuid);
    }

    /**
     * Checks if a player is an operator (for console commands, etc).
     */
    public static boolean isOperator(UUID playerUuid) {
        // In actual implementation:
        // Player player = ServerAPI.getPlayer(playerUuid);
        // return player.isOp();

        return false;
    }

    /**
     * Gets permission description for help messages.
     */
    public static String getPermissionDescription(String permission) {
        return switch (permission) {
            case PERM_USE -> "Allows using crates and previewing rewards";
            case PERM_ADMIN -> "Allows managing crates, giving keys, and reloading config";
            case PERM_PREVIEW -> "Allows previewing crate contents";
            case PERM_BYPASS_COOLDOWN -> "Bypasses the crate opening cooldown";
            default -> "Unknown permission";
        };
    }
}

package com.hytaecrates.util;

import com.hytaecrates.config.CrateConfig;
import com.hytaecrates.config.ItemConfig;
import com.hytaecrates.config.RewardConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating configurations and inputs.
 */
public class ValidationUtil {

    /**
     * Validates a crate configuration.
     *
     * @param config The crate config to validate
     * @return List of validation errors (empty if valid)
     */
    public static List<String> validateCrateConfig(CrateConfig config) {
        List<String> errors = new ArrayList<>();

        if (config == null) {
            errors.add("Crate config is null");
            return errors;
        }

        // Validate ID
        if (isNullOrEmpty(config.getId())) {
            errors.add("Crate ID is required");
        } else if (!config.getId().matches("^[a-z0-9_]+$")) {
            errors.add("Crate ID must only contain lowercase letters, numbers, and underscores");
        }

        // Validate display name
        if (isNullOrEmpty(config.getDisplayName())) {
            errors.add("Display name is required");
        }

        // Validate key ID
        if (isNullOrEmpty(config.getKeyId())) {
            errors.add("Key ID is required");
        }

        // Validate key item
        if (config.getKeyItem() == null) {
            errors.add("Key item configuration is required");
        } else {
            errors.addAll(validateItemConfig(config.getKeyItem(), "Key item"));
        }

        // Validate rewards
        if (config.getRewards() == null || config.getRewards().isEmpty()) {
            errors.add("At least one reward is required");
        } else {
            for (int i = 0; i < config.getRewards().size(); i++) {
                RewardConfig reward = config.getRewards().get(i);
                errors.addAll(validateRewardConfig(reward, "Reward #" + (i + 1)));
            }
        }

        return errors;
    }

    /**
     * Validates an item configuration.
     */
    public static List<String> validateItemConfig(ItemConfig config, String prefix) {
        List<String> errors = new ArrayList<>();

        if (config == null) {
            errors.add(prefix + ": Item config is null");
            return errors;
        }

        if (isNullOrEmpty(config.getMaterial())) {
            errors.add(prefix + ": Material is required");
        }

        if (config.getAmount() < 1) {
            errors.add(prefix + ": Amount must be at least 1");
        }

        if (config.getAmount() > 64) {
            errors.add(prefix + ": Amount cannot exceed 64");
        }

        return errors;
    }

    /**
     * Validates a reward configuration.
     */
    public static List<String> validateRewardConfig(RewardConfig config, String prefix) {
        List<String> errors = new ArrayList<>();

        if (config == null) {
            errors.add(prefix + ": Reward config is null");
            return errors;
        }

        // Validate item
        if (config.getItem() == null) {
            errors.add(prefix + ": Item is required");
        } else {
            errors.addAll(validateItemConfig(config.getItem(), prefix));
        }

        // Validate rarity
        if (isNullOrEmpty(config.getRarity())) {
            errors.add(prefix + ": Rarity is required");
        } else if (!isValidRarity(config.getRarity())) {
            errors.add(prefix + ": Invalid rarity '" + config.getRarity() + "'. Valid values: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
        }

        // Validate weight
        if (config.getWeight() < 1) {
            errors.add(prefix + ": Weight must be at least 1");
        }

        return errors;
    }

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a rarity string is valid.
     */
    public static boolean isValidRarity(String rarity) {
        if (rarity == null) return false;
        return switch (rarity.toUpperCase()) {
            case "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY" -> true;
            default -> false;
        };
    }

    /**
     * Sanitizes a crate ID (removes invalid characters).
     */
    public static String sanitizeCrateId(String id) {
        if (id == null) return "";
        return id.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    /**
     * Validates and clamps a weight value.
     */
    public static int clampWeight(int weight) {
        return Math.max(1, Math.min(1000, weight));
    }

    /**
     * Validates and clamps an amount value.
     */
    public static int clampAmount(int amount) {
        return Math.max(1, Math.min(64, amount));
    }

    /**
     * Checks if a material name is potentially valid.
     * This is a basic check - actual validation would use Hytale's registry.
     */
    public static boolean isPotentiallyValidMaterial(String material) {
        if (isNullOrEmpty(material)) return false;
        // Basic format check: uppercase with underscores
        return material.matches("^[A-Z][A-Z0-9_]*$");
    }
}

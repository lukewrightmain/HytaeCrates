package com.hytaecrates.reward;

/**
 * Represents the rarity levels for crate rewards.
 * Each rarity has an associated color code and default weight.
 */
public enum Rarity {
    COMMON("&7", "Gray", 50),
    UNCOMMON("&a", "Green", 25),
    RARE("&9", "Blue", 15),
    EPIC("&5", "Purple", 7),
    LEGENDARY("&6", "Gold", 3);

    private final String colorCode;
    private final String colorName;
    private final int defaultWeight;

    Rarity(String colorCode, String colorName, int defaultWeight) {
        this.colorCode = colorCode;
        this.colorName = colorName;
        this.defaultWeight = defaultWeight;
    }

    /**
     * Gets the color code for this rarity (e.g., "&6" for gold).
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Gets the color name for this rarity.
     */
    public String getColorName() {
        return colorName;
    }

    /**
     * Gets the default weight for this rarity.
     */
    public int getDefaultWeight() {
        return defaultWeight;
    }

    /**
     * Parses a rarity from string, case-insensitive.
     * Returns COMMON if not found.
     */
    public static Rarity fromString(String name) {
        if (name == null || name.isEmpty()) {
            return COMMON;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }

    /**
     * Checks if this rarity should trigger special effects.
     */
    public boolean isSpecial() {
        return this == EPIC || this == LEGENDARY;
    }

    /**
     * Checks if this rarity should trigger global announcement.
     */
    public boolean shouldAnnounce() {
        return this == RARE || this == EPIC || this == LEGENDARY;
    }
}

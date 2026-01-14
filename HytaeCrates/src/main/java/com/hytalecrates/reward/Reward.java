package com.hytalecrates.reward;

import com.hytalecrates.config.ItemConfig;

/**
 * Represents a reward that can be won from a crate.
 */
public class Reward {

    private final ItemConfig item;
    private final Rarity rarity;
    private final int weight;
    private final double chance;

    public Reward(ItemConfig item, Rarity rarity, int weight, double chance) {
        this.item = item;
        this.rarity = rarity;
        this.weight = weight;
        this.chance = chance;
    }

    /**
     * Gets the item configuration for this reward.
     */
    public ItemConfig getItem() {
        return item;
    }

    /**
     * Gets the rarity of this reward.
     */
    public Rarity getRarity() {
        return rarity;
    }

    /**
     * Gets the weight for weighted random selection.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Gets the display chance percentage.
     */
    public double getChance() {
        return chance;
    }

    /**
     * Gets the display name with rarity color applied.
     */
    public String getColoredDisplayName() {
        return rarity.getColorCode() + item.getDisplayName();
    }

    /**
     * Gets the material name for display purposes.
     */
    public String getMaterialName() {
        return item.getMaterial();
    }

    /**
     * Gets the amount of items in this reward.
     */
    public int getAmount() {
        return item.getAmount();
    }

    @Override
    public String toString() {
        return "Reward{" +
                "item=" + item.getDisplayName() +
                ", rarity=" + rarity +
                ", weight=" + weight +
                ", chance=" + chance + "%" +
                '}';
    }
}

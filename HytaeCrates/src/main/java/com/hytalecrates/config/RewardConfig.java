package com.hytalecrates.config;

/**
 * Configuration model for a reward entry in a crate.
 */
public class RewardConfig {

    private ItemConfig item;
    private String rarity;
    private int weight;
    private double chance;

    public RewardConfig() {
        this.item = new ItemConfig();
        this.rarity = "COMMON";
        this.weight = 50;
        this.chance = 50.0;
    }

    public RewardConfig(ItemConfig item, String rarity, int weight, double chance) {
        this.item = item;
        this.rarity = rarity;
        this.weight = weight;
        this.chance = chance;
    }

    public ItemConfig getItem() {
        return item;
    }

    public void setItem(ItemConfig item) {
        this.item = item;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }
}

package com.hytaecrates.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration model for an item (used in rewards and keys).
 */
public class ItemConfig {

    private String material;
    private int amount;
    private String displayName;
    private boolean enchanted;
    private List<String> lore;
    private Map<String, Integer> enchantments;

    public ItemConfig() {
        this.material = "STONE";
        this.amount = 1;
        this.displayName = "";
        this.enchanted = false;
        this.lore = new ArrayList<>();
        this.enchantments = new HashMap<>();
    }

    public ItemConfig(String material, int amount, String displayName) {
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.enchanted = false;
        this.lore = new ArrayList<>();
        this.enchantments = new HashMap<>();
    }

    // Getters and Setters
    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(1, amount);
    }

    public String getDisplayName() {
        return displayName != null && !displayName.isEmpty() ? displayName : material;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public void setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
    }

    public List<String> getLore() {
        return lore != null ? lore : new ArrayList<>();
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Map<String, Integer> getEnchantments() {
        return enchantments != null ? enchantments : new HashMap<>();
    }

    public void setEnchantments(Map<String, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    public boolean hasEnchantments() {
        return enchantments != null && !enchantments.isEmpty();
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    @Override
    public String toString() {
        return "ItemConfig{" +
                "material='" + material + '\'' +
                ", amount=" + amount +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}

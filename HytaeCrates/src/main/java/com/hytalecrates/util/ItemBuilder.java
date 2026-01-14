package com.hytalecrates.util;

import com.hytalecrates.config.ItemConfig;
import com.hytalecrates.key.CrateKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating in-game items.
 * This is a representation that would be converted to actual Hytale ItemStack.
 */
public class ItemBuilder {

    private String material;
    private int amount;
    private String displayName;
    private List<String> lore;
    private Map<String, Integer> enchantments;
    private boolean enchantedGlow;
    private Map<String, Object> nbtData;

    public ItemBuilder(String material) {
        this.material = material;
        this.amount = 1;
        this.displayName = null;
        this.lore = new ArrayList<>();
        this.enchantments = new HashMap<>();
        this.enchantedGlow = false;
        this.nbtData = new HashMap<>();
    }

    /**
     * Creates an ItemBuilder from an ItemConfig.
     */
    public static ItemBuilder fromConfig(ItemConfig config) {
        ItemBuilder builder = new ItemBuilder(config.getMaterial())
                .amount(config.getAmount())
                .displayName(config.getDisplayName())
                .lore(config.getLore())
                .enchantedGlow(config.isEnchanted());

        if (config.hasEnchantments()) {
            for (Map.Entry<String, Integer> entry : config.getEnchantments().entrySet()) {
                builder.enchantment(entry.getKey(), entry.getValue());
            }
        }

        return builder;
    }

    /**
     * Creates an ItemBuilder for a crate key.
     */
    public static ItemBuilder createKey(CrateKey key, Map<String, Object> keyNbt) {
        ItemBuilder builder = fromConfig(key.getItemConfig());
        builder.nbtData.putAll(keyNbt);
        return builder;
    }

    public ItemBuilder material(String material) {
        this.material = material;
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
        return this;
    }

    public ItemBuilder displayName(String displayName) {
        this.displayName = MessageUtil.colorize(displayName);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (lore != null) {
            this.lore = lore.stream()
                    .map(MessageUtil::colorize)
                    .toList();
        }
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        this.lore.add(MessageUtil.colorize(line));
        return this;
    }

    public ItemBuilder enchantment(String enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder enchantedGlow(boolean glow) {
        this.enchantedGlow = glow;
        return this;
    }

    public ItemBuilder nbt(String key, Object value) {
        this.nbtData.put(key, value);
        return this;
    }

    public ItemBuilder nbtData(Map<String, Object> data) {
        this.nbtData.putAll(data);
        return this;
    }

    /**
     * Builds the item representation.
     * In actual implementation, this would return a Hytale ItemStack.
     */
    public ItemRepresentation build() {
        return new ItemRepresentation(
                material,
                amount,
                displayName,
                new ArrayList<>(lore),
                new HashMap<>(enchantments),
                enchantedGlow,
                new HashMap<>(nbtData)
        );
    }

    /**
     * Represents a built item (would be ItemStack in Hytale API).
     */
    public static class ItemRepresentation {
        private final String material;
        private final int amount;
        private final String displayName;
        private final List<String> lore;
        private final Map<String, Integer> enchantments;
        private final boolean enchantedGlow;
        private final Map<String, Object> nbtData;

        public ItemRepresentation(String material, int amount, String displayName,
                                  List<String> lore, Map<String, Integer> enchantments,
                                  boolean enchantedGlow, Map<String, Object> nbtData) {
            this.material = material;
            this.amount = amount;
            this.displayName = displayName;
            this.lore = lore;
            this.enchantments = enchantments;
            this.enchantedGlow = enchantedGlow;
            this.nbtData = nbtData;
        }

        public String getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getLore() {
            return lore;
        }

        public Map<String, Integer> getEnchantments() {
            return enchantments;
        }

        public boolean hasEnchantedGlow() {
            return enchantedGlow;
        }

        public Map<String, Object> getNbtData() {
            return nbtData;
        }

        /**
         * Checks if this item has a specific NBT tag.
         */
        public boolean hasNbtTag(String key) {
            return nbtData.containsKey(key);
        }

        /**
         * Gets an NBT value.
         */
        public Object getNbtValue(String key) {
            return nbtData.get(key);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "material='" + material + '\'' +
                    ", amount=" + amount +
                    ", displayName='" + displayName + '\'' +
                    '}';
        }
    }
}

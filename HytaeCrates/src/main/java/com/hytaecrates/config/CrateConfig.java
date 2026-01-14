package com.hytaecrates.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration model for a crate definition loaded from JSON.
 */
public class CrateConfig {

    private String id;
    private String displayName;
    private String blockType;
    private String keyId;
    private ItemConfig keyItem;
    private List<RewardConfig> rewards;
    private HologramConfig hologram;

    public CrateConfig() {
        this.id = "";
        this.displayName = "";
        this.blockType = "CHEST";
        this.keyId = "";
        this.keyItem = new ItemConfig();
        this.rewards = new ArrayList<>();
        this.hologram = new HologramConfig();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public ItemConfig getKeyItem() {
        return keyItem;
    }

    public void setKeyItem(ItemConfig keyItem) {
        this.keyItem = keyItem;
    }

    public List<RewardConfig> getRewards() {
        return rewards != null ? rewards : new ArrayList<>();
    }

    public void setRewards(List<RewardConfig> rewards) {
        this.rewards = rewards;
    }

    public HologramConfig getHologram() {
        return hologram;
    }

    public void setHologram(HologramConfig hologram) {
        this.hologram = hologram;
    }

    /**
     * Validates that this crate config has all required fields.
     */
    public boolean isValid() {
        return id != null && !id.isEmpty()
                && displayName != null && !displayName.isEmpty()
                && keyId != null && !keyId.isEmpty()
                && rewards != null && !rewards.isEmpty();
    }

    /**
     * Inner class for hologram configuration.
     */
    public static class HologramConfig {
        private boolean enabled;
        private List<String> lines;

        public HologramConfig() {
            this.enabled = false;
            this.lines = new ArrayList<>();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getLines() {
            return lines != null ? lines : new ArrayList<>();
        }

        public void setLines(List<String> lines) {
            this.lines = lines;
        }
    }
}

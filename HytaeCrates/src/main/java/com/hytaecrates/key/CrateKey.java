package com.hytaecrates.key;

import com.hytaecrates.config.ItemConfig;

import java.util.List;

/**
 * Represents a key item that can open a specific crate.
 */
public class CrateKey {

    private final String keyId;
    private final String crateId;
    private final ItemConfig itemConfig;

    // NBT tag key used to identify crate keys
    public static final String NBT_KEY_TAG = "HytaeCratesKey";
    public static final String NBT_CRATE_TAG = "HytaeCratesCrate";

    public CrateKey(String keyId, String crateId, ItemConfig itemConfig) {
        this.keyId = keyId;
        this.crateId = crateId;
        this.itemConfig = itemConfig;
    }

    /**
     * Gets the unique identifier for this key.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the crate ID this key opens.
     */
    public String getCrateId() {
        return crateId;
    }

    /**
     * Gets the item configuration for this key.
     */
    public ItemConfig getItemConfig() {
        return itemConfig;
    }

    /**
     * Gets the material type for the key item.
     */
    public String getMaterial() {
        return itemConfig.getMaterial();
    }

    /**
     * Gets the display name of the key.
     */
    public String getDisplayName() {
        return itemConfig.getDisplayName();
    }

    /**
     * Gets the lore lines for the key.
     */
    public List<String> getLore() {
        return itemConfig.getLore();
    }

    /**
     * Returns whether this key should have an enchanted glow.
     */
    public boolean isEnchanted() {
        return itemConfig.isEnchanted();
    }

    @Override
    public String toString() {
        return "CrateKey{" +
                "keyId='" + keyId + '\'' +
                ", crateId='" + crateId + '\'' +
                ", material='" + getMaterial() + '\'' +
                '}';
    }
}

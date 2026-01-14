package com.hytaecrates.key;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.config.CrateConfig;
import com.hytaecrates.crate.Crate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages crate keys - creation, validation, and distribution.
 */
public class KeyManager {

    private final CratesPlugin plugin;
    private final Map<String, CrateKey> keys;

    public KeyManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.keys = new HashMap<>();
    }

    /**
     * Registers a key for a crate.
     */
    public void registerKey(Crate crate) {
        CrateConfig config = crate.getConfig();
        CrateKey key = new CrateKey(
                config.getKeyId(),
                crate.getId(),
                config.getKeyItem()
        );
        keys.put(key.getKeyId(), key);
        plugin.getLogger().info("Registered key: " + key.getKeyId() + " for crate: " + crate.getId());
    }

    /**
     * Unregisters a key.
     */
    public void unregisterKey(String keyId) {
        keys.remove(keyId);
    }

    /**
     * Gets a key by its ID.
     */
    public Optional<CrateKey> getKey(String keyId) {
        return Optional.ofNullable(keys.get(keyId));
    }

    /**
     * Gets the key for a specific crate.
     */
    public Optional<CrateKey> getKeyForCrate(String crateId) {
        return keys.values().stream()
                .filter(key -> key.getCrateId().equals(crateId))
                .findFirst();
    }

    /**
     * Checks if an item is a valid crate key.
     * This method checks for the NBT tag on the item.
     *
     * @param itemNbtData The NBT data of the item (key-value map)
     * @return The CrateKey if valid, empty otherwise
     */
    public Optional<CrateKey> validateKeyItem(Map<String, Object> itemNbtData) {
        if (itemNbtData == null || itemNbtData.isEmpty()) {
            return Optional.empty();
        }

        // Check for our custom NBT tag
        Object keyTag = itemNbtData.get(CrateKey.NBT_KEY_TAG);
        if (keyTag == null) {
            return Optional.empty();
        }

        String keyId = keyTag.toString();
        return getKey(keyId);
    }

    /**
     * Checks if an item is a key for a specific crate.
     */
    public boolean isKeyForCrate(Map<String, Object> itemNbtData, String crateId) {
        Optional<CrateKey> key = validateKeyItem(itemNbtData);
        return key.isPresent() && key.get().getCrateId().equals(crateId);
    }

    /**
     * Creates NBT data for a key item.
     */
    public Map<String, Object> createKeyNbtData(CrateKey key) {
        Map<String, Object> nbt = new HashMap<>();
        nbt.put(CrateKey.NBT_KEY_TAG, key.getKeyId());
        nbt.put(CrateKey.NBT_CRATE_TAG, key.getCrateId());
        return nbt;
    }

    /**
     * Gets all registered keys.
     */
    public Map<String, CrateKey> getAllKeys() {
        return new HashMap<>(keys);
    }

    /**
     * Clears all registered keys.
     */
    public void clearKeys() {
        keys.clear();
    }

    /**
     * Gives a key item to a player.
     * This is a placeholder that would integrate with Hytale's inventory API.
     *
     * @param playerUuid The player's UUID
     * @param keyId The key ID to give
     * @param amount The amount of keys to give
     * @return true if successful
     */
    public boolean giveKey(String playerUuid, String keyId, int amount) {
        Optional<CrateKey> keyOpt = getKey(keyId);
        if (keyOpt.isEmpty()) {
            plugin.getLogger().warning("Attempted to give unknown key: " + keyId);
            return false;
        }

        CrateKey key = keyOpt.get();

        // In actual implementation, this would:
        // 1. Create an ItemStack with the key's material, name, lore
        // 2. Apply enchantment glow if configured
        // 3. Set NBT data with our custom tags
        // 4. Add to player's inventory
        //
        // Example pseudo-code:
        // ItemStack item = new ItemStack(key.getMaterial(), amount);
        // item.setDisplayName(key.getDisplayName());
        // item.setLore(key.getLore());
        // if (key.isEnchanted()) {
        //     item.addEnchantmentGlow();
        // }
        // item.setNbtData(createKeyNbtData(key));
        // player.getInventory().addItem(item);

        plugin.getLogger().info("Gave " + amount + "x " + key.getDisplayName() + " to player " + playerUuid);
        return true;
    }

    /**
     * Consumes (removes) a key from a player's hand.
     *
     * @param playerUuid The player's UUID
     * @return true if successful
     */
    public boolean consumeKey(String playerUuid) {
        // In actual implementation, this would:
        // 1. Get the item in player's main hand
        // 2. Reduce its amount by 1
        // 3. If amount becomes 0, remove the item
        //
        // Example pseudo-code:
        // ItemStack item = player.getInventory().getItemInMainHand();
        // item.setAmount(item.getAmount() - 1);

        plugin.getLogger().info("Consumed key from player " + playerUuid);
        return true;
    }
}

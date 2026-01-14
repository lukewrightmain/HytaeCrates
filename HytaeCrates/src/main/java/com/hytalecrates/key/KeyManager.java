package com.hytalecrates.key;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.config.CrateConfig;
import com.hytalecrates.crate.Crate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

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
        plugin.getLogger().at(Level.INFO).log("Registered key: %s for crate: %s", key.getKeyId(), crate.getId());
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
     *
     * @param store The entity store for the target player
     * @param playerEntityRef The player's entity ref within that store
     * @param keyId The key ID to give
     * @param amount The amount of keys to give
     * @return true if successful
     */
    public boolean giveKey(Store<EntityStore> store, Ref<EntityStore> playerEntityRef, String keyId, int amount) {
        Optional<CrateKey> keyOpt = getKey(keyId);
        if (keyOpt.isEmpty()) {
            plugin.getLogger().at(Level.WARNING).log("Attempted to give unknown key: %s", keyId);
            return false;
        }

        CrateKey key = keyOpt.get();

        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) {
            plugin.getLogger().at(Level.WARNING).log("Failed to resolve Player component for ref when giving key %s", keyId);
            return false;
        }

        int safeAmount = Math.max(1, Math.min(64, amount));

        // Build metadata with our custom tags so we can validate keys later.
        BsonDocument metadata = new BsonDocument()
                .append(CrateKey.NBT_KEY_TAG, new BsonString(key.getKeyId()))
                .append(CrateKey.NBT_CRATE_TAG, new BsonString(key.getCrateId()));

        // NOTE: ItemStack expects an itemId string that matches an item asset id.
        // For now we use the configured 'material' directly.
        ItemStack itemStack = new ItemStack(key.getMaterial(), safeAmount, metadata);
        ItemStackTransaction tx = player.getInventory()
                .getCombinedHotbarFirst()
                .addItemStack(itemStack);

        ItemStack remainder = tx.getRemainder();
        boolean success = remainder == null || remainder.isEmpty();

        if (success) {
            plugin.getLogger().at(Level.INFO).log("Gave %dx %s to player %s", safeAmount, key.getDisplayName(), player.getUuid());
        } else {
            plugin.getLogger().at(Level.INFO).log("Insufficient inventory space: remainder=%s", remainder);
        }

        return success;
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

        plugin.getLogger().at(Level.INFO).log("Consumed key from player %s", playerUuid);
        return true;
    }
}

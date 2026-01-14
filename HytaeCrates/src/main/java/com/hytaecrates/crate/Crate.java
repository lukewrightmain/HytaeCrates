package com.hytaecrates.crate;

import com.hytaecrates.config.CrateConfig;
import com.hytaecrates.config.RewardConfig;
import com.hytaecrates.reward.Rarity;
import com.hytaecrates.reward.Reward;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a crate with its configuration and rewards.
 */
public class Crate {

    private final String id;
    private final String displayName;
    private final String blockType;
    private final String keyId;
    private final CrateConfig config;
    private final List<Reward> rewards;
    private final List<CrateLocation> locations;

    public Crate(CrateConfig config) {
        this.id = config.getId();
        this.displayName = config.getDisplayName();
        this.blockType = config.getBlockType();
        this.keyId = config.getKeyId();
        this.config = config;
        this.rewards = new ArrayList<>();
        this.locations = new ArrayList<>();

        // Convert reward configs to Reward objects
        for (RewardConfig rewardConfig : config.getRewards()) {
            Reward reward = new Reward(
                    rewardConfig.getItem(),
                    Rarity.fromString(rewardConfig.getRarity()),
                    rewardConfig.getWeight(),
                    rewardConfig.getChance()
            );
            rewards.add(reward);
        }
    }

    /**
     * Gets the unique identifier for this crate.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name with color codes.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the display name without color codes.
     */
    public String getStrippedDisplayName() {
        return displayName.replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * Gets the block type used for this crate.
     */
    public String getBlockType() {
        return blockType;
    }

    /**
     * Gets the key ID required to open this crate.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the original configuration.
     */
    public CrateConfig getConfig() {
        return config;
    }

    /**
     * Gets all possible rewards from this crate.
     */
    public List<Reward> getRewards() {
        return rewards;
    }

    /**
     * Gets the total weight of all rewards.
     */
    public int getTotalWeight() {
        return rewards.stream().mapToInt(Reward::getWeight).sum();
    }

    /**
     * Gets all locations where this crate is placed.
     */
    public List<CrateLocation> getLocations() {
        return locations;
    }

    /**
     * Adds a location to this crate.
     */
    public void addLocation(CrateLocation location) {
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }

    /**
     * Removes a location from this crate.
     */
    public boolean removeLocation(CrateLocation location) {
        return locations.remove(location);
    }

    /**
     * Checks if this crate exists at the given location.
     */
    public boolean isAtLocation(String world, int x, int y, int z) {
        return locations.stream().anyMatch(loc -> loc.matches(world, x, y, z));
    }

    /**
     * Gets rewards filtered by rarity.
     */
    public List<Reward> getRewardsByRarity(Rarity rarity) {
        return rewards.stream()
                .filter(r -> r.getRarity() == rarity)
                .toList();
    }

    /**
     * Gets the number of rewards in this crate.
     */
    public int getRewardCount() {
        return rewards.size();
    }

    /**
     * Checks if this crate has any legendary rewards.
     */
    public boolean hasLegendaryRewards() {
        return rewards.stream().anyMatch(r -> r.getRarity() == Rarity.LEGENDARY);
    }

    @Override
    public String toString() {
        return "Crate{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", rewards=" + rewards.size() +
                ", locations=" + locations.size() +
                '}';
    }
}

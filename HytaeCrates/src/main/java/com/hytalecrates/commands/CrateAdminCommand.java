package com.hytalecrates.commands;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.config.CrateConfig;
import com.hytalecrates.config.ItemConfig;
import com.hytalecrates.config.RewardConfig;
import com.hytalecrates.crate.Crate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Additional admin commands for crate management.
 * These are more advanced operations typically done via config files.
 */
public class CrateAdminCommand {

    private final CratesPlugin plugin;

    public CrateAdminCommand(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new crate with a basic template configuration.
     *
     * @param crateId The unique ID for the new crate
     * @param displayName The display name for the crate
     * @return true if creation was successful
     */
    public boolean createCrateTemplate(String crateId, String displayName) {
        // Check if crate already exists
        if (plugin.getCrateManager().getCrate(crateId).isPresent()) {
            return false;
        }

        // Create basic crate config
        CrateConfig config = new CrateConfig();
        config.setId(crateId);
        config.setDisplayName(displayName);
        config.setBlockType("CHEST");
        config.setKeyId(crateId + "_key");

        // Create key item config
        ItemConfig keyItem = new ItemConfig();
        keyItem.setMaterial("STICK");
        keyItem.setDisplayName("&b" + displayName.replaceAll("&[0-9a-fk-or]", "") + " Key");
        keyItem.setEnchanted(true);
        keyItem.setLore(List.of(
                "&7Right-click on a " + displayName,
                "&7to claim your reward!"
        ));
        config.setKeyItem(keyItem);

        // Add some default rewards
        List<RewardConfig> rewards = new ArrayList<>();

        // Common reward
        RewardConfig commonReward = new RewardConfig();
        commonReward.setItem(new ItemConfig("IRON_INGOT", 8, "&7Iron Ingots"));
        commonReward.setRarity("COMMON");
        commonReward.setWeight(50);
        commonReward.setChance(50.0);
        rewards.add(commonReward);

        // Uncommon reward
        RewardConfig uncommonReward = new RewardConfig();
        uncommonReward.setItem(new ItemConfig("GOLD_INGOT", 4, "&6Gold Ingots"));
        uncommonReward.setRarity("UNCOMMON");
        uncommonReward.setWeight(25);
        uncommonReward.setChance(25.0);
        rewards.add(uncommonReward);

        // Rare reward
        RewardConfig rareReward = new RewardConfig();
        rareReward.setItem(new ItemConfig("DIAMOND", 2, "&bDiamonds"));
        rareReward.setRarity("RARE");
        rareReward.setWeight(15);
        rareReward.setChance(15.0);
        rewards.add(rareReward);

        // Epic reward
        RewardConfig epicReward = new RewardConfig();
        epicReward.setItem(new ItemConfig("EMERALD", 4, "&aEmeralds"));
        epicReward.setRarity("EPIC");
        epicReward.setWeight(7);
        epicReward.setChance(7.0);
        rewards.add(epicReward);

        // Legendary reward
        RewardConfig legendaryReward = new RewardConfig();
        legendaryReward.setItem(new ItemConfig("NETHERITE_INGOT", 1, "&4&lNetherite Ingot"));
        legendaryReward.setRarity("LEGENDARY");
        legendaryReward.setWeight(3);
        legendaryReward.setChance(3.0);
        rewards.add(legendaryReward);

        config.setRewards(rewards);

        // Set up hologram
        CrateConfig.HologramConfig hologram = new CrateConfig.HologramConfig();
        hologram.setEnabled(true);
        hologram.setLines(List.of(
                displayName,
                "&7Right-click with a key",
                "&7to open!"
        ));
        config.setHologram(hologram);

        // Save and register the crate
        return plugin.getCrateManager().createCrate(config);
    }

    /**
     * Adds a reward to an existing crate.
     *
     * @param crateId The crate ID
     * @param material The item material
     * @param amount The item amount
     * @param displayName The display name
     * @param rarity The rarity level
     * @param weight The selection weight
     * @return true if successful
     */
    public boolean addReward(String crateId, String material, int amount, String displayName,
                             String rarity, int weight) {

        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            return false;
        }

        Crate crate = crateOpt.get();
        CrateConfig config = crate.getConfig();

        // Create new reward
        RewardConfig reward = new RewardConfig();
        reward.setItem(new ItemConfig(material, amount, displayName));
        reward.setRarity(rarity.toUpperCase());
        reward.setWeight(weight);
        reward.setChance(calculateChance(weight, config));

        // Add to config
        List<RewardConfig> rewards = new ArrayList<>(config.getRewards());
        rewards.add(reward);
        config.setRewards(rewards);

        // Save and reload
        plugin.getConfigManager().saveCrateConfig(config);
        plugin.getCrateManager().reload();

        return true;
    }

    /**
     * Removes a reward from a crate by index.
     *
     * @param crateId The crate ID
     * @param rewardIndex The index of the reward to remove
     * @return true if successful
     */
    public boolean removeReward(String crateId, int rewardIndex) {
        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            return false;
        }

        Crate crate = crateOpt.get();
        CrateConfig config = crate.getConfig();

        List<RewardConfig> rewards = new ArrayList<>(config.getRewards());
        if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
            return false;
        }

        rewards.remove(rewardIndex);
        config.setRewards(rewards);

        // Save and reload
        plugin.getConfigManager().saveCrateConfig(config);
        plugin.getCrateManager().reload();

        return true;
    }

    /**
     * Updates the display name of a crate.
     *
     * @param crateId The crate ID
     * @param newDisplayName The new display name
     * @return true if successful
     */
    public boolean updateCrateDisplayName(String crateId, String newDisplayName) {
        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            return false;
        }

        CrateConfig config = crateOpt.get().getConfig();
        config.setDisplayName(newDisplayName);

        plugin.getConfigManager().saveCrateConfig(config);
        plugin.getCrateManager().reload();

        return true;
    }

    /**
     * Updates the key item for a crate.
     *
     * @param crateId The crate ID
     * @param material The key material
     * @param displayName The key display name
     * @return true if successful
     */
    public boolean updateKeyItem(String crateId, String material, String displayName) {
        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            return false;
        }

        CrateConfig config = crateOpt.get().getConfig();
        ItemConfig keyItem = config.getKeyItem();
        keyItem.setMaterial(material);
        keyItem.setDisplayName(displayName);

        plugin.getConfigManager().saveCrateConfig(config);
        plugin.getCrateManager().reload();

        return true;
    }

    /**
     * Runs a simulation of crate openings and displays results.
     *
     * @param senderUuid The sender's UUID
     * @param crateId The crate ID
     * @param iterations Number of simulations
     */
    public void runSimulation(UUID senderUuid, String crateId, int iterations) {
        var crateOpt = plugin.getCrateManager().getCrate(crateId);
        if (crateOpt.isEmpty()) {
            sendMessage(senderUuid, plugin.getMessageUtil().crateNotFound(crateId));
            return;
        }

        Crate crate = crateOpt.get();
        var results = plugin.getRewardManager().simulateOpens(crate, iterations);

        sendMessage(senderUuid, plugin.getMessageUtil().format("&6=== Simulation Results (" + iterations + " opens) ==="));

        results.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    double percentage = (entry.getValue() / (double) iterations) * 100;
                    sendMessage(senderUuid, plugin.getMessageUtil().format(
                            String.format("&7%s: &e%d &8(%.2f%%)", entry.getKey(), entry.getValue(), percentage)
                    ));
                });
    }

    /**
     * Calculates chance percentage based on weight and existing rewards.
     */
    private double calculateChance(int weight, CrateConfig config) {
        int totalWeight = config.getRewards().stream()
                .mapToInt(RewardConfig::getWeight)
                .sum() + weight;

        return (weight / (double) totalWeight) * 100;
    }

    /**
     * Sends a message to a player.
     */
    private void sendMessage(UUID playerUuid, String message) {
        plugin.getLogger().at(Level.INFO).log("[Admin] %s", message);
    }
}

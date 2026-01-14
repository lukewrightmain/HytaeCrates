package com.hytalecrates.reward;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

/**
 * Manages reward selection using weighted random algorithm.
 */
public class RewardManager {

    private final CratesPlugin plugin;
    private final Random random;

    public RewardManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Selects a random reward from a crate using weighted random selection.
     *
     * @param crate The crate to select from
     * @return The selected reward
     */
    public Reward selectReward(Crate crate) {
        List<Reward> rewards = crate.getRewards();
        if (rewards.isEmpty()) {
            plugin.getLogger().at(Level.WARNING).log("Attempted to select reward from empty crate: %s", crate.getId());
            return null;
        }

        int totalWeight = crate.getTotalWeight();
        if (totalWeight <= 0) {
            // Fallback to equal probability if weights are invalid
            return rewards.get(random.nextInt(rewards.size()));
        }

        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Reward reward : rewards) {
            currentWeight += reward.getWeight();
            if (randomValue < currentWeight) {
                return reward;
            }
        }

        // Fallback to last reward (shouldn't happen with valid weights)
        return rewards.get(rewards.size() - 1);
    }

    /**
     * Pre-rolls rewards for the animation display.
     * Returns a list of rewards to show during the spin animation.
     *
     * @param crate The crate to roll from
     * @param count The number of rewards to pre-roll
     * @return List of pre-rolled rewards
     */
    public List<Reward> preRollRewards(Crate crate, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> selectReward(crate))
                .filter(r -> r != null)
                .toList();
    }

    /**
     * Calculates the actual chance percentage for a reward.
     *
     * @param reward The reward to calculate for
     * @param crate The crate containing the reward
     * @return The chance as a percentage (0-100)
     */
    public double calculateChance(Reward reward, Crate crate) {
        int totalWeight = crate.getTotalWeight();
        if (totalWeight <= 0) {
            return 0;
        }
        return (reward.getWeight() / (double) totalWeight) * 100;
    }

    /**
     * Gives a reward to a player.
     * This is a placeholder that would integrate with Hytale's inventory API.
     *
     * @param playerUuid The player's UUID
     * @param reward The reward to give
     * @return true if successful
     */
    public boolean giveReward(String playerUuid, Reward reward) {
        if (reward == null) {
            return false;
        }

        // In actual implementation, this would:
        // 1. Create an ItemStack from the reward's item config
        // 2. Apply display name, lore, enchantments
        // 3. Add to player's inventory (or drop if full)
        //
        // Example pseudo-code:
        // ItemStack item = createItemFromConfig(reward.getItem());
        // if (!player.getInventory().addItem(item)) {
        //     player.getWorld().dropItem(player.getLocation(), item);
        // }

        plugin.getLogger().at(Level.INFO).log("Gave reward %s (%s) to player %s",
                reward.getItem().getDisplayName(), reward.getRarity(), playerUuid);
        return true;
    }

    /**
     * Simulates opening a crate multiple times for testing/statistics.
     *
     * @param crate The crate to simulate
     * @param iterations Number of simulations
     * @return Map of reward display names to occurrence counts
     */
    public Map<String, Integer> simulateOpens(Crate crate, int iterations) {
        Map<String, Integer> results = new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            Reward reward = selectReward(crate);
            if (reward != null) {
                String name = reward.getItem().getDisplayName();
                results.put(name, results.getOrDefault(name, 0) + 1);
            }
        }

        return results;
    }
}

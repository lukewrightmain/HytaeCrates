package com.hytalecrates.reward;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.util.ItemIdUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;

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
     *
     * @param player The player entity
     * @param reward The reward to give
     * @return true if successful
     */
    public boolean giveReward(Player player, Reward reward) {
        if (reward == null) {
            return false;
        }

        if (player == null) {
            return false;
        }

        String itemId = ItemIdUtil.resolveItemId(reward.getItem().getMaterial());
        int quantity = Math.max(1, Math.min(64, reward.getItem().getAmount()));
        ItemStack stack = new ItemStack(itemId, quantity);

        ItemStackTransaction tx = player.getInventory()
                .getCombinedHotbarFirst()
                .addItemStack(stack);

        ItemStack remainder = tx.getRemainder();
        boolean success = remainder == null || remainder.isEmpty();

        plugin.getLogger().at(Level.INFO).log("Reward grant result=%s itemId=%s qty=%d player=%s",
                success, itemId, quantity, player.getUuid());

        return success;
    }

    /**
     * Backwards-compatible placeholder signature used by older GUI scaffolding.
     * The real server runtime should call {@link #giveReward(Player, Reward)} instead.
     */
    public boolean giveReward(String playerUuid, Reward reward) {
        plugin.getLogger().at(Level.WARNING).log("giveReward(String, Reward) called for %s - this path is deprecated", playerUuid);
        return false;
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

package com.hytalecrates.gui;

import com.hytalecrates.CratesPlugin;
import com.hytalecrates.crate.Crate;
import com.hytalecrates.reward.Reward;
import com.hytalecrates.util.ItemBuilder;
import com.hytalecrates.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * GUI that displays all possible rewards from a crate.
 */
public class CratePreviewGUI {

    private final CratesPlugin plugin;
    private final Crate crate;

    // GUI layout constants
    private static final int ROWS = 6;
    private static final int SLOTS = ROWS * 9;
    private static final int[] BORDER_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    private static final int[] REWARD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final List<ItemBuilder.ItemRepresentation> items;
    private final String title;

    public CratePreviewGUI(CratesPlugin plugin, Crate crate) {
        this.plugin = plugin;
        this.crate = crate;
        this.items = new ArrayList<>();
        this.title = MessageUtil.colorize(crate.getDisplayName() + " &8- Preview");

        buildGui();
    }

    /**
     * Builds the GUI contents.
     */
    private void buildGui() {
        // Initialize with empty slots
        for (int i = 0; i < SLOTS; i++) {
            items.add(null);
        }

        // Add border items (glass panes)
        ItemBuilder.ItemRepresentation borderItem = new ItemBuilder("GRAY_STAINED_GLASS_PANE")
                .displayName(" ")
                .build();

        for (int slot : BORDER_SLOTS) {
            items.set(slot, borderItem);
        }

        // Add reward items
        List<Reward> rewards = crate.getRewards();
        int rewardIndex = 0;

        for (int slot : REWARD_SLOTS) {
            if (rewardIndex >= rewards.size()) {
                break;
            }

            Reward reward = rewards.get(rewardIndex);
            ItemBuilder.ItemRepresentation rewardItem = createRewardItem(reward);
            items.set(slot, rewardItem);
            rewardIndex++;
        }

        // Add info item in center of border
        ItemBuilder.ItemRepresentation infoItem = new ItemBuilder("BOOK")
                .displayName("&e" + MessageUtil.stripColors(crate.getDisplayName()))
                .addLoreLine("")
                .addLoreLine("&7Total Rewards: &e" + rewards.size())
                .addLoreLine("&7Use a &b" + crate.getConfig().getKeyItem().getDisplayName())
                .addLoreLine("&7to open this crate!")
                .addLoreLine("")
                .addLoreLine("&8Click anywhere to close")
                .build();

        items.set(4, infoItem);
    }

    /**
     * Creates an item representation for a reward.
     */
    private ItemBuilder.ItemRepresentation createRewardItem(Reward reward) {
        double chance = plugin.getRewardManager().calculateChance(reward, crate);

        ItemBuilder builder = ItemBuilder.fromConfig(reward.getItem())
                .displayName(reward.getRarity().getColorCode() + reward.getItem().getDisplayName());

        // Add lore with rarity and chance
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7Rarity: " + reward.getRarity().getColorCode() + reward.getRarity().name());
        lore.add("&7Chance: &e" + MessageUtil.formatChance(chance));

        if (reward.getAmount() > 1) {
            lore.add("&7Amount: &e" + reward.getAmount());
        }

        // Add original lore if present
        if (reward.getItem().hasLore()) {
            lore.add("");
            lore.addAll(reward.getItem().getLore());
        }

        return builder.lore(lore).build();
    }

    /**
     * Opens the GUI for a player.
     * This is a placeholder that would integrate with Hytale's inventory API.
     */
    public void open(UUID playerUuid) {
        // In actual implementation:
        // Inventory inv = ServerAPI.createInventory(ROWS, title);
        // for (int i = 0; i < items.size(); i++) {
        //     if (items.get(i) != null) {
        //         inv.setItem(i, items.get(i).toItemStack());
        //     }
        // }
        // player.openInventory(inv);

        plugin.getLogger().at(Level.INFO).log("Opening preview GUI: %s", title);
        plugin.getLogger().at(Level.INFO).log("Displaying %d rewards", crate.getRewards().size());
    }

    /**
     * Gets the GUI title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the items in the GUI.
     */
    public List<ItemBuilder.ItemRepresentation> getItems() {
        return items;
    }
}

package com.hytaecrates.gui;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.animation.ParticleEffect;
import com.hytaecrates.animation.SoundEffect;
import com.hytaecrates.animation.SpinAnimation;
import com.hytaecrates.crate.Crate;
import com.hytaecrates.reward.Reward;
import com.hytaecrates.util.ItemBuilder;
import com.hytaecrates.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI that displays the slot machine spinning animation.
 */
public class CrateSpinGUI {

    private final CratesPlugin plugin;
    private final Crate crate;
    private final UUID playerUuid;
    private final String playerName;

    private SpinAnimation animation;
    private ParticleEffect particleEffect;
    private SoundEffect soundEffect;

    // GUI layout constants
    private static final int ROWS = 3;
    private static final int SLOTS = ROWS * 9;

    // Spinning slots (horizontal row in middle)
    private static final int[] SPIN_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int CENTER_SLOT = 13; // The winning slot
    private static final int POINTER_SLOT_TOP = 4;
    private static final int POINTER_SLOT_BOTTOM = 22;

    private final List<ItemBuilder.ItemRepresentation> items;
    private final String title;

    public CrateSpinGUI(CratesPlugin plugin, Crate crate, UUID playerUuid, String playerName) {
        this.plugin = plugin;
        this.crate = crate;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.items = new ArrayList<>();
        this.title = MessageUtil.colorize(crate.getDisplayName() + " &8- Opening...");

        this.particleEffect = new ParticleEffect(plugin);
        this.soundEffect = new SoundEffect(plugin);

        buildGui();
    }

    /**
     * Builds the initial GUI contents.
     */
    private void buildGui() {
        // Initialize with empty slots
        for (int i = 0; i < SLOTS; i++) {
            items.add(null);
        }

        // Add glass pane borders (top and bottom rows)
        ItemBuilder.ItemRepresentation borderItem = new ItemBuilder("BLACK_STAINED_GLASS_PANE")
                .displayName(" ")
                .build();

        for (int i = 0; i < 9; i++) {
            items.set(i, borderItem);          // Top row
            items.set(18 + i, borderItem);     // Bottom row
        }

        // Add pointer arrows
        ItemBuilder.ItemRepresentation pointerItem = new ItemBuilder("ARROW")
                .displayName("&e▼ WINNER ▼")
                .build();
        items.set(POINTER_SLOT_TOP, pointerItem);

        ItemBuilder.ItemRepresentation pointerBottomItem = new ItemBuilder("ARROW")
                .displayName("&e▲ WINNER ▲")
                .build();
        items.set(POINTER_SLOT_BOTTOM, pointerBottomItem);

        // Fill spin slots with initial rewards
        List<Reward> rewards = crate.getRewards();
        for (int i = 0; i < SPIN_SLOTS.length; i++) {
            Reward reward = rewards.get(i % rewards.size());
            items.set(SPIN_SLOTS[i], createSpinItem(reward));
        }
    }

    /**
     * Creates an item representation for a spinning reward.
     */
    private ItemBuilder.ItemRepresentation createSpinItem(Reward reward) {
        return ItemBuilder.fromConfig(reward.getItem())
                .displayName(reward.getRarity().getColorCode() + reward.getItem().getDisplayName())
                .addLoreLine("")
                .addLoreLine("&7Rarity: " + reward.getRarity().getColorCode() + reward.getRarity().name())
                .build();
    }

    /**
     * Opens the GUI for the player.
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

        plugin.getLogger().info("Opening spin GUI: " + title);

        // Play crate opening sound
        soundEffect.playCrateOpenSound(playerUuid);
    }

    /**
     * Starts the spinning animation.
     */
    public void startAnimation() {
        animation = new SpinAnimation(plugin, crate);

        // Set up tick callback
        animation.onTick(this::onAnimationTick);

        // Set up completion callback
        animation.onComplete(this::onAnimationComplete);

        // Start the animation
        animation.start();

        plugin.getLogger().info("Started spin animation for player " + playerUuid);
    }

    /**
     * Called on each animation tick - updates the GUI display.
     */
    private void onAnimationTick(Reward currentReward) {
        // Shift items left and add new item on the right
        shiftItems(currentReward);

        // Play tick sound with varying pitch
        soundEffect.playSlowdownTick(playerUuid, animation.getCurrentStep(), animation.getTotalSteps());

        // Update the inventory display
        updateDisplay();
    }

    /**
     * Shifts the spinning items left and adds a new item on the right.
     */
    private void shiftItems(Reward newReward) {
        // Shift all items left
        for (int i = 0; i < SPIN_SLOTS.length - 1; i++) {
            items.set(SPIN_SLOTS[i], items.get(SPIN_SLOTS[i + 1]));
        }

        // Add new item on the right
        items.set(SPIN_SLOTS[SPIN_SLOTS.length - 1], createSpinItem(newReward));
    }

    /**
     * Updates the inventory display.
     */
    private void updateDisplay() {
        // In actual implementation:
        // Inventory inv = player.getOpenInventory();
        // for (int i = 0; i < SPIN_SLOTS.length; i++) {
        //     inv.setItem(SPIN_SLOTS[i], items.get(SPIN_SLOTS[i]).toItemStack());
        // }

        plugin.getLogger().fine("Updated spin GUI display");
    }

    /**
     * Called when the animation completes.
     */
    private void onAnimationComplete(Reward wonReward) {
        plugin.getLogger().info("Animation complete! Won: " + wonReward.getItem().getDisplayName());

        // Play win sound based on rarity
        soundEffect.playWinSound(playerUuid, wonReward.getRarity());

        // Play particle effects
        // Note: Would need crate location for particles
        // particleEffect.playWinEffect(wonReward.getRarity(), crateLocation);

        // Highlight the winning item
        highlightWinner(wonReward);

        // Give the reward to the player
        plugin.getRewardManager().giveReward(playerUuid.toString(), wonReward);

        // Announce the win
        announceWin(wonReward);

        // Schedule GUI close
        scheduleClose();
    }

    /**
     * Highlights the winning item in the GUI.
     */
    private void highlightWinner(Reward wonReward) {
        // Update center slot with highlighted version
        ItemBuilder.ItemRepresentation winnerItem = ItemBuilder.fromConfig(wonReward.getItem())
                .displayName("&a&l✦ " + wonReward.getRarity().getColorCode() + wonReward.getItem().getDisplayName() + " &a&l✦")
                .addLoreLine("")
                .addLoreLine("&a&lYOU WON!")
                .addLoreLine("")
                .addLoreLine("&7Rarity: " + wonReward.getRarity().getColorCode() + wonReward.getRarity().name())
                .enchantedGlow(true)
                .build();

        items.set(CENTER_SLOT, winnerItem);

        // Update pointers to celebration mode
        ItemBuilder.ItemRepresentation celebrationItem = new ItemBuilder("NETHER_STAR")
                .displayName("&6&l★ WINNER! ★")
                .enchantedGlow(true)
                .build();

        items.set(POINTER_SLOT_TOP, celebrationItem);
        items.set(POINTER_SLOT_BOTTOM, celebrationItem);

        updateDisplay();
    }

    /**
     * Announces the win to the server.
     */
    private void announceWin(Reward wonReward) {
        if (!plugin.getConfigManager().getMainConfig().getAnnouncements().isEnabled()) {
            return;
        }

        String format;
        if (wonReward.getRarity() == com.hytaecrates.reward.Rarity.LEGENDARY) {
            format = plugin.getConfigManager().getMainConfig().getAnnouncements().getLegendaryFormat();
        } else if (wonReward.getRarity().shouldAnnounce()) {
            format = plugin.getConfigManager().getMainConfig().getAnnouncements().getFormat();
        } else {
            return; // Don't announce common/uncommon
        }

        String message = plugin.getMessageUtil().formatWinAnnouncement(format, playerName, crate, wonReward);

        // Broadcast to all players
        // In actual implementation:
        // ServerAPI.broadcast(message);

        plugin.getLogger().info("[BROADCAST] " + message);
    }

    /**
     * Schedules the GUI to close after a delay.
     */
    private void scheduleClose() {
        // In actual implementation, would use Hytale's scheduler
        // ServerAPI.getScheduler().runTaskLater(() -> {
        //     plugin.getGuiManager().closeGui(playerUuid);
        // }, 60); // 3 seconds

        plugin.getLogger().info("Scheduled GUI close for player " + playerUuid);
    }

    /**
     * Gets the GUI title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the current items in the GUI.
     */
    public List<ItemBuilder.ItemRepresentation> getItems() {
        return items;
    }

    /**
     * Checks if the animation is still running.
     */
    public boolean isAnimationRunning() {
        return animation != null && animation.isRunning();
    }

    /**
     * Stops the animation if running.
     */
    public void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }
}

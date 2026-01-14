package com.hytaecrates.animation;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.reward.Rarity;

import java.util.UUID;

/**
 * Handles sound effects for crate animations.
 */
public class SoundEffect {

    private final CratesPlugin plugin;

    public SoundEffect(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays a sound for a player.
     *
     * @param playerUuid The player to play the sound for
     * @param sound The sound identifier
     * @param volume Volume of the sound (0.0 - 1.0)
     * @param pitch Pitch of the sound (0.5 - 2.0)
     */
    public void playSound(UUID playerUuid, String sound, float volume, float pitch) {
        // In actual implementation:
        // Player player = ServerAPI.getPlayer(playerUuid);
        // player.playSound(sound, volume, pitch);

        plugin.getLogger().fine("Playing sound: " + sound + " for player " + playerUuid);
    }

    /**
     * Plays the spin/scroll sound during animation.
     */
    public void playSpinSound(UUID playerUuid) {
        String sound = plugin.getConfigManager().getMainConfig().getAnimation().getSpinSound();
        playSound(playerUuid, sound, 0.5f, 1.0f);
    }

    /**
     * Plays the tick sound during animation (for each item scroll).
     */
    public void playTickSound(UUID playerUuid, float pitch) {
        String sound = plugin.getConfigManager().getMainConfig().getAnimation().getTickSound();
        playSound(playerUuid, sound, 0.3f, pitch);
    }

    /**
     * Plays the win sound when the animation completes.
     */
    public void playWinSound(UUID playerUuid, Rarity rarity) {
        String sound;
        float volume;
        float pitch;

        switch (rarity) {
            case LEGENDARY:
                sound = plugin.getConfigManager().getMainConfig().getAnimation().getLegendarySound();
                volume = 1.0f;
                pitch = 1.0f;
                // Also play to all nearby players
                playGlobalWinSound(sound);
                break;
            case EPIC:
                sound = plugin.getConfigManager().getMainConfig().getAnimation().getWinSound();
                volume = 0.9f;
                pitch = 1.2f;
                break;
            case RARE:
                sound = plugin.getConfigManager().getMainConfig().getAnimation().getWinSound();
                volume = 0.8f;
                pitch = 1.0f;
                break;
            default:
                sound = plugin.getConfigManager().getMainConfig().getAnimation().getWinSound();
                volume = 0.7f;
                pitch = 0.8f;
                break;
        }

        playSound(playerUuid, sound, volume, pitch);
    }

    /**
     * Plays a global win sound for legendary rewards.
     */
    private void playGlobalWinSound(String sound) {
        // In actual implementation, would play to all online players
        // for (Player player : ServerAPI.getOnlinePlayers()) {
        //     player.playSound(sound, 0.5f, 1.0f);
        // }

        plugin.getLogger().info("Playing global win sound: " + sound);
    }

    /**
     * Plays a sound effect with increasing pitch (for spin slowdown).
     */
    public void playSlowdownTick(UUID playerUuid, int step, int totalSteps) {
        // Pitch increases as animation slows down
        float progress = (float) step / totalSteps;
        float pitch = 0.5f + (progress * 1.5f); // 0.5 to 2.0

        playTickSound(playerUuid, Math.min(pitch, 2.0f));
    }

    /**
     * Plays the crate opening sound.
     */
    public void playCrateOpenSound(UUID playerUuid) {
        // Play chest opening sound
        playSound(playerUuid, "block.chest.open", 0.8f, 1.0f);
    }

    /**
     * Plays an error/invalid key sound.
     */
    public void playErrorSound(UUID playerUuid) {
        playSound(playerUuid, "entity.villager.no", 0.5f, 1.0f);
    }

    /**
     * Plays a success sound (for admin actions, etc).
     */
    public void playSuccessSound(UUID playerUuid) {
        playSound(playerUuid, "entity.experience_orb.pickup", 0.7f, 1.2f);
    }
}

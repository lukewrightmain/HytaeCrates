package com.hytaecrates.animation;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.crate.CrateLocation;
import com.hytaecrates.reward.Rarity;

/**
 * Handles particle effects for crate animations.
 */
public class ParticleEffect {

    private final CratesPlugin plugin;

    public ParticleEffect(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays a particle effect at a location.
     *
     * @param particleType The type of particle
     * @param location The crate location
     * @param count Number of particles
     * @param spread How spread out the particles are
     */
    public void playEffect(String particleType, CrateLocation location, int count, double spread) {
        if (!plugin.getConfigManager().getMainConfig().getParticles().isEnabled()) {
            return;
        }

        // In actual implementation, would spawn particles at the location
        // World world = ServerAPI.getWorld(location.getWorldName());
        // world.spawnParticles(particleType, location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5, count, spread, spread, spread);

        plugin.getLogger().fine("Playing particle effect: " + particleType + " at " + location.toDisplayString());
    }

    /**
     * Plays a particle effect for a specific rarity.
     */
    public void playRarityEffect(Rarity rarity, CrateLocation location) {
        String particleType = getParticleForRarity(rarity);
        int count = getParticleCount(rarity);
        double spread = getParticleSpread(rarity);

        playEffect(particleType, location, count, spread);
    }

    /**
     * Plays the win reveal effect based on rarity.
     */
    public void playWinEffect(Rarity rarity, CrateLocation location) {
        switch (rarity) {
            case LEGENDARY:
                playLegendaryEffect(location);
                break;
            case EPIC:
                playEpicEffect(location);
                break;
            case RARE:
                playRareEffect(location);
                break;
            default:
                playCommonEffect(location);
                break;
        }
    }

    /**
     * Plays a legendary win effect (fireworks explosion).
     */
    private void playLegendaryEffect(CrateLocation location) {
        String particle = plugin.getConfigManager().getMainConfig().getParticles().getLegendaryParticle();

        // Multiple bursts of particles
        for (int i = 0; i < 5; i++) {
            playEffect(particle, location, 50, 1.5);
        }

        // Additional spiral effect
        playSpiralEffect(location, particle, 3.0, 20);

        plugin.getLogger().info("Playing LEGENDARY effect at " + location.toDisplayString());
    }

    /**
     * Plays an epic win effect.
     */
    private void playEpicEffect(CrateLocation location) {
        String particle = plugin.getConfigManager().getMainConfig().getParticles().getEpicParticle();

        // Multiple bursts
        for (int i = 0; i < 3; i++) {
            playEffect(particle, location, 30, 1.0);
        }

        plugin.getLogger().info("Playing EPIC effect at " + location.toDisplayString());
    }

    /**
     * Plays a rare win effect.
     */
    private void playRareEffect(CrateLocation location) {
        String particle = plugin.getConfigManager().getMainConfig().getParticles().getRareParticle();
        playEffect(particle, location, 20, 0.8);

        plugin.getLogger().info("Playing RARE effect at " + location.toDisplayString());
    }

    /**
     * Plays a common/uncommon win effect.
     */
    private void playCommonEffect(CrateLocation location) {
        String particle = plugin.getConfigManager().getMainConfig().getParticles().getCommonParticle();
        playEffect(particle, location, 10, 0.5);

        plugin.getLogger().info("Playing COMMON effect at " + location.toDisplayString());
    }

    /**
     * Plays a spiral particle effect.
     */
    private void playSpiralEffect(CrateLocation location, String particleType, double radius, int points) {
        // In actual implementation, would spawn particles in a spiral pattern
        // for (int i = 0; i < points; i++) {
        //     double angle = (2 * Math.PI * i) / points;
        //     double x = location.getX() + 0.5 + radius * Math.cos(angle);
        //     double z = location.getZ() + 0.5 + radius * Math.sin(angle);
        //     double y = location.getY() + 1 + (i * 0.1);
        //     world.spawnParticles(particleType, x, y, z, 1, 0, 0, 0);
        // }

        plugin.getLogger().fine("Playing spiral effect at " + location.toDisplayString());
    }

    /**
     * Plays a tick effect during animation.
     */
    public void playTickEffect(CrateLocation location) {
        String particle = plugin.getConfigManager().getMainConfig().getParticles().getCommonParticle();
        playEffect(particle, location, 3, 0.2);
    }

    /**
     * Gets the particle type for a rarity.
     */
    private String getParticleForRarity(Rarity rarity) {
        return plugin.getConfigManager().getMainConfig().getParticles().getParticleForRarity(rarity.name());
    }

    /**
     * Gets the particle count for a rarity.
     */
    private int getParticleCount(Rarity rarity) {
        return switch (rarity) {
            case LEGENDARY -> 50;
            case EPIC -> 30;
            case RARE -> 20;
            case UNCOMMON -> 15;
            default -> 10;
        };
    }

    /**
     * Gets the particle spread for a rarity.
     */
    private double getParticleSpread(Rarity rarity) {
        return switch (rarity) {
            case LEGENDARY -> 1.5;
            case EPIC -> 1.2;
            case RARE -> 1.0;
            case UNCOMMON -> 0.8;
            default -> 0.5;
        };
    }
}

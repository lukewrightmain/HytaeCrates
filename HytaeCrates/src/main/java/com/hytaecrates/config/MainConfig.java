package com.hytaecrates.config;

/**
 * Configuration model for the main config.json file.
 */
public class MainConfig {

    private String prefix;
    private AnimationConfig animation;
    private AnnouncementConfig announcements;
    private ParticleConfig particles;
    private SettingsConfig settings;

    public MainConfig() {
        this.prefix = "&6[Crates] &r";
        this.animation = new AnimationConfig();
        this.announcements = new AnnouncementConfig();
        this.particles = new ParticleConfig();
        this.settings = new SettingsConfig();
    }

    // Getters and Setters
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public AnimationConfig getAnimation() {
        return animation;
    }

    public void setAnimation(AnimationConfig animation) {
        this.animation = animation;
    }

    public AnnouncementConfig getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(AnnouncementConfig announcements) {
        this.announcements = announcements;
    }

    public ParticleConfig getParticles() {
        return particles;
    }

    public void setParticles(ParticleConfig particles) {
        this.particles = particles;
    }

    public SettingsConfig getSettings() {
        return settings;
    }

    public void setSettings(SettingsConfig settings) {
        this.settings = settings;
    }

    /**
     * Animation configuration.
     */
    public static class AnimationConfig {
        private int spinDuration;
        private String tickSound;
        private String winSound;
        private String spinSound;
        private String legendarySound;

        public AnimationConfig() {
            this.spinDuration = 4000;
            this.tickSound = "ui.button.click";
            this.winSound = "entity.player.levelup";
            this.spinSound = "block.note_block.pling";
            this.legendarySound = "ui.toast.challenge_complete";
        }

        public int getSpinDuration() {
            return spinDuration;
        }

        public void setSpinDuration(int spinDuration) {
            this.spinDuration = spinDuration;
        }

        public String getTickSound() {
            return tickSound;
        }

        public void setTickSound(String tickSound) {
            this.tickSound = tickSound;
        }

        public String getWinSound() {
            return winSound;
        }

        public void setWinSound(String winSound) {
            this.winSound = winSound;
        }

        public String getSpinSound() {
            return spinSound;
        }

        public void setSpinSound(String spinSound) {
            this.spinSound = spinSound;
        }

        public String getLegendarySound() {
            return legendarySound;
        }

        public void setLegendarySound(String legendarySound) {
            this.legendarySound = legendarySound;
        }
    }

    /**
     * Announcement configuration.
     */
    public static class AnnouncementConfig {
        private boolean enabled;
        private String format;
        private String legendaryFormat;

        public AnnouncementConfig() {
            this.enabled = true;
            this.format = "&e{player} &7opened a &b{crate} &7and won {rarity_color}{item}&7!";
            this.legendaryFormat = "&6&l★ &e{player} &7won &6&l{item} &7from &b{crate}&7! &6&l★";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getLegendaryFormat() {
            return legendaryFormat;
        }

        public void setLegendaryFormat(String legendaryFormat) {
            this.legendaryFormat = legendaryFormat;
        }
    }

    /**
     * Particle configuration.
     */
    public static class ParticleConfig {
        private boolean enabled;
        private String commonParticle;
        private String uncommonParticle;
        private String rareParticle;
        private String epicParticle;
        private String legendaryParticle;

        public ParticleConfig() {
            this.enabled = true;
            this.commonParticle = "VILLAGER_HAPPY";
            this.uncommonParticle = "VILLAGER_HAPPY";
            this.rareParticle = "ENCHANTMENT_TABLE";
            this.epicParticle = "PORTAL";
            this.legendaryParticle = "FIREWORK";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCommonParticle() {
            return commonParticle;
        }

        public void setCommonParticle(String commonParticle) {
            this.commonParticle = commonParticle;
        }

        public String getUncommonParticle() {
            return uncommonParticle;
        }

        public void setUncommonParticle(String uncommonParticle) {
            this.uncommonParticle = uncommonParticle;
        }

        public String getRareParticle() {
            return rareParticle;
        }

        public void setRareParticle(String rareParticle) {
            this.rareParticle = rareParticle;
        }

        public String getEpicParticle() {
            return epicParticle;
        }

        public void setEpicParticle(String epicParticle) {
            this.epicParticle = epicParticle;
        }

        public String getLegendaryParticle() {
            return legendaryParticle;
        }

        public void setLegendaryParticle(String legendaryParticle) {
            this.legendaryParticle = legendaryParticle;
        }

        /**
         * Gets the particle type for a specific rarity.
         */
        public String getParticleForRarity(String rarity) {
            return switch (rarity.toUpperCase()) {
                case "UNCOMMON" -> uncommonParticle;
                case "RARE" -> rareParticle;
                case "EPIC" -> epicParticle;
                case "LEGENDARY" -> legendaryParticle;
                default -> commonParticle;
            };
        }
    }

    /**
     * General settings configuration.
     */
    public static class SettingsConfig {
        private boolean requireKeyInHand;
        private boolean consumeKeyOnUse;
        private boolean preventCrateBreak;
        private int cooldownSeconds;

        public SettingsConfig() {
            this.requireKeyInHand = true;
            this.consumeKeyOnUse = true;
            this.preventCrateBreak = true;
            this.cooldownSeconds = 0;
        }

        public boolean isRequireKeyInHand() {
            return requireKeyInHand;
        }

        public void setRequireKeyInHand(boolean requireKeyInHand) {
            this.requireKeyInHand = requireKeyInHand;
        }

        public boolean isConsumeKeyOnUse() {
            return consumeKeyOnUse;
        }

        public void setConsumeKeyOnUse(boolean consumeKeyOnUse) {
            this.consumeKeyOnUse = consumeKeyOnUse;
        }

        public boolean isPreventCrateBreak() {
            return preventCrateBreak;
        }

        public void setPreventCrateBreak(boolean preventCrateBreak) {
            this.preventCrateBreak = preventCrateBreak;
        }

        public int getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(int cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }
    }
}

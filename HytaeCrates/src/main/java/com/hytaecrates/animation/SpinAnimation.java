package com.hytaecrates.animation;

import com.hytaecrates.CratesPlugin;
import com.hytaecrates.crate.Crate;
import com.hytaecrates.reward.Reward;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles the casino-style spinning animation logic.
 */
public class SpinAnimation {

    private final CratesPlugin plugin;
    private final Crate crate;
    private final Reward finalReward;
    private final List<Reward> displayRewards;

    // Animation parameters
    private final int totalDuration;
    private final int totalSteps;
    private int currentStep;
    private boolean running;
    private boolean completed;

    // Callbacks
    private Consumer<Reward> onTick;
    private Consumer<Reward> onComplete;

    // Animation timing (in milliseconds)
    private static final int INITIAL_DELAY = 50;   // Fast start
    private static final int FINAL_DELAY = 400;    // Slow end

    public SpinAnimation(CratesPlugin plugin, Crate crate) {
        this.plugin = plugin;
        this.crate = crate;
        this.totalDuration = plugin.getConfigManager().getMainConfig().getAnimation().getSpinDuration();
        this.totalSteps = calculateTotalSteps();
        this.currentStep = 0;
        this.running = false;
        this.completed = false;

        // Pre-roll rewards for the animation
        this.displayRewards = new ArrayList<>(plugin.getRewardManager().preRollRewards(crate, totalSteps));

        // Select the final reward (last item in the spin)
        this.finalReward = plugin.getRewardManager().selectReward(crate);
        // Set the final reward as the last item
        if (!displayRewards.isEmpty()) {
            displayRewards.set(displayRewards.size() - 1, finalReward);
        } else {
            displayRewards.add(finalReward);
        }
    }

    /**
     * Calculates the total number of steps based on duration and easing.
     */
    private int calculateTotalSteps() {
        // Approximate number of steps for a smooth animation
        // More steps at the start (fast), fewer at the end (slow)
        return 30 + (totalDuration / 100);
    }

    /**
     * Starts the animation.
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        currentStep = 0;

        // Start the animation loop
        runAnimationStep();
    }

    /**
     * Runs a single animation step.
     */
    private void runAnimationStep() {
        if (!running || currentStep >= totalSteps) {
            complete();
            return;
        }

        // Get the current reward to display
        Reward currentReward = displayRewards.get(currentStep % displayRewards.size());

        // Notify tick callback
        if (onTick != null) {
            onTick.accept(currentReward);
        }

        currentStep++;

        // Calculate delay for next step (easing)
        int delay = calculateDelay();

        // Schedule next step
        // In actual implementation, this would use Hytale's scheduler
        // ServerAPI.getScheduler().runTaskLater(() -> runAnimationStep(), delay);

        plugin.getLogger().fine("Animation step " + currentStep + "/" + totalSteps + " - Delay: " + delay + "ms");
    }

    /**
     * Calculates the delay for the current step using easing.
     */
    private int calculateDelay() {
        // Use ease-out cubic for smooth slowdown
        double progress = (double) currentStep / totalSteps;
        double eased = easeOutCubic(progress);

        // Interpolate between initial and final delay
        return (int) (INITIAL_DELAY + (FINAL_DELAY - INITIAL_DELAY) * eased);
    }

    /**
     * Ease-out cubic function for smooth deceleration.
     */
    private double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    /**
     * Completes the animation.
     */
    private void complete() {
        if (completed) {
            return;
        }

        running = false;
        completed = true;

        if (onComplete != null) {
            onComplete.accept(finalReward);
        }

        plugin.getLogger().info("Animation completed - Final reward: " + finalReward.getItem().getDisplayName());
    }

    /**
     * Stops the animation immediately.
     */
    public void stop() {
        running = false;
    }

    /**
     * Sets the callback for each animation tick.
     */
    public void onTick(Consumer<Reward> callback) {
        this.onTick = callback;
    }

    /**
     * Sets the callback for animation completion.
     */
    public void onComplete(Consumer<Reward> callback) {
        this.onComplete = callback;
    }

    /**
     * Gets the final reward that will be won.
     */
    public Reward getFinalReward() {
        return finalReward;
    }

    /**
     * Checks if the animation is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Checks if the animation has completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Gets the current progress as a percentage (0-100).
     */
    public double getProgress() {
        return ((double) currentStep / totalSteps) * 100;
    }

    /**
     * Gets the display rewards list.
     */
    public List<Reward> getDisplayRewards() {
        return displayRewards;
    }

    /**
     * Gets the current step number.
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Gets the total number of steps.
     */
    public int getTotalSteps() {
        return totalSteps;
    }
}

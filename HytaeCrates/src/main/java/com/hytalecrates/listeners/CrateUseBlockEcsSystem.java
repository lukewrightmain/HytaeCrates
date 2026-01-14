package com.hytalecrates.listeners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytalecrates.CratesPlugin;

import java.util.Set;
import java.util.logging.Level;

/**
 * ECS system hook for block-use interactions (the "Press F to open" flow).
 *
 * <p>Important: {@link UseBlockEvent.Pre} is an ECS event, NOT a regular {@code IEvent},
 * so it must be handled via the ECS {@link WorldEventSystem} pipeline, not {@code EventRegistry}.</p>
 */
public final class CrateUseBlockEcsSystem extends WorldEventSystem<EntityStore, UseBlockEvent.Pre> {

    private final CrateInteractListener listener;

    public CrateUseBlockEcsSystem(CrateInteractListener listener) {
        super(UseBlockEvent.Pre.class);
        this.listener = listener;
    }

    @Override
    public Set<com.hypixel.hytale.component.dependency.Dependency<EntityStore>> getDependencies() {
        // Run as early as possible so we can cancel the default container-open logic.
        return RootDependency.firstSet();
    }

    @Override
    public void handle(Store<EntityStore> store, CommandBuffer<EntityStore> buffer, UseBlockEvent.Pre event) {
        // DIAGNOSTIC: Log every time this ECS system is invoked
        try {
            var target = event.getTargetBlock();
            var blockType = event.getBlockType();
            CratesPlugin.getInstance().getLogger().at(Level.INFO).log(
                    "[DIAG] UseBlockEvent.Pre ECS FIRED: block=%s pos=%s interactionType=%s",
                    blockType != null ? blockType.getId() : "null",
                    target != null ? (target.x + "," + target.y + "," + target.z) : "null",
                    event.getInteractionType()
            );
        } catch (Throwable t) {
            CratesPlugin.getInstance().getLogger().at(Level.WARNING).log("[DIAG] UseBlockEvent.Pre ECS FIRED but error logging: %s", t.getMessage());
        }

        // Delegate to our shared crate logic.
        listener.onUseBlock(event);
    }
}



package com.hytalecrates.util;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import java.util.Locale;
import java.util.Map;

/**
 * Utility to resolve user-friendly item identifiers (often Minecraft-style material names)
 * into actual Hytale item asset ids.
 *
 * <p>In configs we commonly use values like {@code STICK} or {@code DIAMOND_SWORD}.
 * Hytale item ids are typically lowercase and may be namespaced (e.g. {@code hytale:stick}).</p>
 */
public final class ItemIdUtil {

    private ItemIdUtil() {}

    public static String resolveItemId(String configured) {
        if (configured == null) {
            return null;
        }

        String raw = configured.trim();
        if (raw.isEmpty()) {
            return raw;
        }

        String lower = raw.toLowerCase(Locale.ROOT);

        // If the asset map is available (server runtime), prefer known ids.
        try {
            // Asset map type is a DefaultAssetMap<String, Item> (map-like API).
            var assetMap = Item.getAssetMap();
            var map = assetMap != null ? assetMap.getAssetMap() : null;
            if (map != null && !map.isEmpty()) {
                if (map.containsKey(raw)) {
                    return raw;
                }
                if (map.containsKey(lower)) {
                    return lower;
                }

                String namespaced = "hytale:" + lower;
                if (map.containsKey(namespaced)) {
                    return namespaced;
                }

                String best = bestMatch(map, raw);
                if (best != null) {
                    return best;
                }
            }
        } catch (Throwable ignored) {
            // In tests or non-server runtimes, assets may not be initialized. Fall back below.
        }

        return lower;
    }

    /**
     * Picks the most likely asset id given a user-friendly id like "STICK" or "GOLD_INGOT".
     * This is heuristic-based and prefers non-debug, non-prototype items.
     */
    private static String bestMatch(Map<String, Item> map, String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        String[] tokens = lower.split("[\\s_]+");

        int bestScore = Integer.MIN_VALUE;
        String bestKey = null;

        for (String key : map.keySet()) {
            if (key == null) continue;
            String k = key.toLowerCase(Locale.ROOT);

            int matched = 0;
            for (String t : tokens) {
                if (t.isEmpty()) continue;
                if (k.contains(t)) matched++;
            }
            if (matched == 0) continue;

            int score = matched * 10;
            if (matched == tokens.length) score += 50;

            // Prefer close endings like "..._stick" or "...:stick"
            if (k.endsWith("_" + lower) || k.endsWith(":" + lower) || k.endsWith("/" + lower)) score += 40;

            // Prefer typical "real" items over debug/prototype/test.
            if (k.contains("ingredient")) score += 30;
            if (k.contains("weapon")) score += 10;
            if (k.contains("armor")) score += 10;
            if (k.contains("rock_gem")) score += 20;

            if (k.contains("debug")) score -= 200;
            if (k.contains("prototype")) score -= 100;
            if (k.contains("test")) score -= 100;
            if (k.contains("_npc") || k.contains("npc")) score -= 30;

            // If the key contains every token as a full segment (split by _), bonus.
            String[] segments = k.split("[_:/]+");
            int segHits = 0;
            for (String t : tokens) {
                if (t.isEmpty()) continue;
                for (String s : segments) {
                    if (s.equals(t)) {
                        segHits++;
                        break;
                    }
                }
            }
            score += segHits * 10;

            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }

        return bestKey;
    }
}



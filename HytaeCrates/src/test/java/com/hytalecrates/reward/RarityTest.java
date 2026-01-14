package com.hytalecrates.reward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Rarity enum.
 */
class RarityTest {

    @Test
    void testRarityFromString() {
        assertEquals(Rarity.COMMON, Rarity.fromString("common"));
        assertEquals(Rarity.COMMON, Rarity.fromString("COMMON"));
        assertEquals(Rarity.LEGENDARY, Rarity.fromString("legendary"));
        assertEquals(Rarity.EPIC, Rarity.fromString("EPIC"));
    }

    @Test
    void testRarityFromInvalidString() {
        assertEquals(Rarity.COMMON, Rarity.fromString("invalid"));
        assertEquals(Rarity.COMMON, Rarity.fromString(""));
        assertEquals(Rarity.COMMON, Rarity.fromString(null));
    }

    @Test
    void testRarityColorCodes() {
        assertEquals("&7", Rarity.COMMON.getColorCode());
        assertEquals("&a", Rarity.UNCOMMON.getColorCode());
        assertEquals("&9", Rarity.RARE.getColorCode());
        assertEquals("&5", Rarity.EPIC.getColorCode());
        assertEquals("&6", Rarity.LEGENDARY.getColorCode());
    }

    @Test
    void testRarityDefaultWeights() {
        assertEquals(50, Rarity.COMMON.getDefaultWeight());
        assertEquals(25, Rarity.UNCOMMON.getDefaultWeight());
        assertEquals(15, Rarity.RARE.getDefaultWeight());
        assertEquals(7, Rarity.EPIC.getDefaultWeight());
        assertEquals(3, Rarity.LEGENDARY.getDefaultWeight());
    }

    @Test
    void testIsSpecial() {
        assertFalse(Rarity.COMMON.isSpecial());
        assertFalse(Rarity.UNCOMMON.isSpecial());
        assertFalse(Rarity.RARE.isSpecial());
        assertTrue(Rarity.EPIC.isSpecial());
        assertTrue(Rarity.LEGENDARY.isSpecial());
    }

    @Test
    void testShouldAnnounce() {
        assertFalse(Rarity.COMMON.shouldAnnounce());
        assertFalse(Rarity.UNCOMMON.shouldAnnounce());
        assertTrue(Rarity.RARE.shouldAnnounce());
        assertTrue(Rarity.EPIC.shouldAnnounce());
        assertTrue(Rarity.LEGENDARY.shouldAnnounce());
    }
}

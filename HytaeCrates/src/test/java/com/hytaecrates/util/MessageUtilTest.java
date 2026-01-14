package com.hytaecrates.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MessageUtil class.
 */
class MessageUtilTest {

    @Test
    void testColorize() {
        assertEquals("§aGreen", MessageUtil.colorize("&aGreen"));
        assertEquals("§6§lBold Gold", MessageUtil.colorize("&6&lBold Gold"));
        assertEquals("Plain text", MessageUtil.colorize("Plain text"));
    }

    @Test
    void testStripColors() {
        assertEquals("Green", MessageUtil.stripColors("&aGreen"));
        assertEquals("Bold Gold", MessageUtil.stripColors("&6&lBold Gold"));
        assertEquals("Test", MessageUtil.stripColors("§6Test"));
    }

    @Test
    void testFormatChance() {
        assertTrue(MessageUtil.formatChance(50.0).contains("50"));
        assertTrue(MessageUtil.formatChance(3.0).contains("3"));
        assertTrue(MessageUtil.formatChance(0.5).contains("0.5"));
    }

    @Test
    void testFormatAmount() {
        assertEquals("", MessageUtil.formatAmount(1));
        assertEquals(" x5", MessageUtil.formatAmount(5));
        assertEquals(" x64", MessageUtil.formatAmount(64));
    }

    @Test
    void testCreateProgressBar() {
        String bar = MessageUtil.createProgressBar(50, 10, "█", "░");
        assertEquals("█████░░░░░", bar);
        
        String fullBar = MessageUtil.createProgressBar(100, 5, "#", "-");
        assertEquals("#####", fullBar);
        
        String emptyBar = MessageUtil.createProgressBar(0, 5, "#", "-");
        assertEquals("-----", emptyBar);
    }
}

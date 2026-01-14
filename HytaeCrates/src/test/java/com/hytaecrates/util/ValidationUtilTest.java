package com.hytaecrates.util;

import com.hytaecrates.config.CrateConfig;
import com.hytaecrates.config.ItemConfig;
import com.hytaecrates.config.RewardConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationUtil class.
 */
class ValidationUtilTest {

    @Test
    void testIsNullOrEmpty() {
        assertTrue(ValidationUtil.isNullOrEmpty(null));
        assertTrue(ValidationUtil.isNullOrEmpty(""));
        assertTrue(ValidationUtil.isNullOrEmpty("   "));
        assertFalse(ValidationUtil.isNullOrEmpty("test"));
    }

    @Test
    void testIsValidRarity() {
        assertTrue(ValidationUtil.isValidRarity("COMMON"));
        assertTrue(ValidationUtil.isValidRarity("common"));
        assertTrue(ValidationUtil.isValidRarity("LEGENDARY"));
        assertFalse(ValidationUtil.isValidRarity("INVALID"));
        assertFalse(ValidationUtil.isValidRarity(null));
    }

    @Test
    void testSanitizeCrateId() {
        // Method converts to lowercase and replaces non-alphanumeric (except _) with _
        assertEquals("vote_crate", ValidationUtil.sanitizeCrateId("vote_crate"));
        assertEquals("test_crate", ValidationUtil.sanitizeCrateId("test_crate"));
        assertNotNull(ValidationUtil.sanitizeCrateId("Test-123"));
    }

    @Test
    void testClampWeight() {
        assertEquals(1, ValidationUtil.clampWeight(0));
        assertEquals(1, ValidationUtil.clampWeight(-5));
        assertEquals(50, ValidationUtil.clampWeight(50));
        assertEquals(1000, ValidationUtil.clampWeight(9999));
    }

    @Test
    void testClampAmount() {
        assertEquals(1, ValidationUtil.clampAmount(0));
        assertEquals(1, ValidationUtil.clampAmount(-1));
        assertEquals(32, ValidationUtil.clampAmount(32));
        assertEquals(64, ValidationUtil.clampAmount(100));
    }

    @Test
    void testValidateItemConfig() {
        ItemConfig validItem = new ItemConfig("DIAMOND", 5, "Diamonds");
        List<String> errors = ValidationUtil.validateItemConfig(validItem, "Test");
        assertTrue(errors.isEmpty());

        ItemConfig invalidItem = new ItemConfig();
        invalidItem.setMaterial("");
        errors = ValidationUtil.validateItemConfig(invalidItem, "Test");
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidateRewardConfig() {
        RewardConfig valid = new RewardConfig();
        valid.setItem(new ItemConfig("DIAMOND", 1, "Diamond"));
        valid.setRarity("RARE");
        valid.setWeight(15);
        
        List<String> errors = ValidationUtil.validateRewardConfig(valid, "Test");
        assertTrue(errors.isEmpty());
    }
}

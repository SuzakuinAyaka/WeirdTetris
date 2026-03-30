package com.example.els.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class GameConstantsTest {

    @Test
    public void normalizeCustomSpeedLevel_shouldFallbackToDefaultWhenInvalid() {
        assertEquals(GameConstants.CUSTOM_SPEED_DEFAULT, GameConstants.normalizeCustomSpeedLevel(-1));
        assertEquals(GameConstants.CUSTOM_SPEED_DEFAULT, GameConstants.normalizeCustomSpeedLevel(99));
    }

    @Test
    public void normalizeCustomSpeedLevel_shouldKeepSupportedValues() {
        assertEquals(GameConstants.CUSTOM_SPEED_DEFAULT,
                GameConstants.normalizeCustomSpeedLevel(GameConstants.CUSTOM_SPEED_DEFAULT));
        assertEquals(GameConstants.CUSTOM_SPEED_FAST,
                GameConstants.normalizeCustomSpeedLevel(GameConstants.CUSTOM_SPEED_FAST));
        assertEquals(GameConstants.CUSTOM_SPEED_VERY_FAST,
                GameConstants.normalizeCustomSpeedLevel(GameConstants.CUSTOM_SPEED_VERY_FAST));
    }

    @Test
    public void itemPrices_shouldBeConfiguredForKnownItems() {
        for (String itemId : GameConstants.ITEM_IDS) {
            assertTrue(GameConstants.getItemPrice(itemId) > 0);
        }
        assertEquals(0, GameConstants.getItemPrice("unknown_item"));
    }

    @Test
    public void itemIds_shouldContainUniqueEntries() {
        Set<String> uniqueIds = new HashSet<>(GameConstants.ITEM_IDS);
        assertEquals(GameConstants.ITEM_IDS.size(), uniqueIds.size());
    }
}

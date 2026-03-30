package com.example.els.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TetrominoBagGeneratorTest {

    @Test
    public void nextClassicType_shouldAlwaysReturnClassicType() {
        Tetromino.BagGenerator generator = new Tetromino.BagGenerator();
        for (int i = 0; i < 70; i++) {
            int type = generator.nextClassicType();
            assertTrue(type >= Tetromino.TYPE_I && type <= Tetromino.TYPE_Z);
        }
    }

    @Test
    public void firstBag_shouldContainAllSevenUniqueTypes() {
        Tetromino.BagGenerator generator = new Tetromino.BagGenerator();
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 7; i++) {
            seen.add(generator.nextClassicType());
        }
        assertEquals(7, seen.size());
    }
}

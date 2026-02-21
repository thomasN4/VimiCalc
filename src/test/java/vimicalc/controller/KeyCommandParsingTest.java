package vimicalc.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyCommandParsingTest {

    private final KeyCommand kc = new KeyCommand(null);

    @Test
    void multiplierThenFunction() {
        // "5j" → function index 1, multiplier 5
        int[] result = kc.parseFIndexAndMult(0, "5j");
        assertEquals(1, result[0]); // index of 'j'
        assertEquals(5, result[1]); // multiplier
    }

    @Test
    void noMultiplier() {
        // "j" → function index 0, multiplier 1
        int[] result = kc.parseFIndexAndMult(0, "j");
        assertEquals(0, result[0]);
        assertEquals(1, result[1]);
    }

    @Test
    void multiDigitMultiplier() {
        // "12h" → function index 2, multiplier 12
        int[] result = kc.parseFIndexAndMult(0, "12h");
        assertEquals(2, result[0]);
        assertEquals(12, result[1]);
    }

    @Test
    void startingFromOffset() {
        // In "d5j", parsing from index 1 → function index 2, multiplier 5
        int[] result = kc.parseFIndexAndMult(1, "d5j");
        assertEquals(2, result[0]);
        assertEquals(5, result[1]);
    }

    @Test
    void singleDigitMultiplier() {
        // "3l" → function index 1, multiplier 3
        int[] result = kc.parseFIndexAndMult(0, "3l");
        assertEquals(1, result[0]);
        assertEquals(3, result[1]);
    }
}

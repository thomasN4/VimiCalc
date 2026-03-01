package vimicalc.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyCommandParsingTest {

    private final KeyCommand kc = new KeyCommand(null, null);

    // --- Basic multiplier parsing ---

    @Test
    void noMultiplier() {
        var result = kc.parsePrefix(0, "j");
        assertEquals(0, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void singleDigitMultiplier() {
        var result = kc.parsePrefix(0, "3l");
        assertEquals(1, result.funcIndex());
        assertEquals(3, result.multiplier());
    }

    @Test
    void multiplierThenFunction() {
        var result = kc.parsePrefix(0, "5j");
        assertEquals(1, result.funcIndex());
        assertEquals(5, result.multiplier());
    }

    @Test
    void multiDigitMultiplier() {
        var result = kc.parsePrefix(0, "12h");
        assertEquals(2, result.funcIndex());
        assertEquals(12, result.multiplier());
    }

    @Test
    void largeMultiplier() {
        var result = kc.parsePrefix(0, "999k");
        assertEquals(3, result.funcIndex());
        assertEquals(999, result.multiplier());
    }

    // --- Offset parsing (for range-op sub-expressions) ---

    @Test
    void startingFromOffset() {
        // In "d5j", parsing from index 1 gives the "5j" part
        var result = kc.parsePrefix(1, "d5j");
        assertEquals(2, result.funcIndex());
        assertEquals(5, result.multiplier());
    }

    @Test
    void offsetNoMultiplier() {
        // In "dj", parsing from index 1 gives just "j" with multiplier 1
        var result = kc.parsePrefix(1, "dj");
        assertEquals(1, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void offsetMultiDigit() {
        // In "p10l", parsing from index 1 gives funcIndex=3, multiplier=10
        var result = kc.parsePrefix(1, "p10l");
        assertEquals(3, result.funcIndex());
        assertEquals(10, result.multiplier());
    }

    // --- Double-letter commands ---

    @Test
    void doubleLetter_dd() {
        // "dd" — first char is non-digit, so funcIndex=0, multiplier=1
        var result = kc.parsePrefix(0, "dd");
        assertEquals(0, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void doubleLetter_yy() {
        var result = kc.parsePrefix(0, "yy");
        assertEquals(0, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void doubleLetter_pp() {
        var result = kc.parsePrefix(0, "pp");
        assertEquals(0, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    // --- Edge cases ---

    @Test
    void startIndexAtEnd() {
        // Parsing from the last char — it's non-digit, so funcIndex=startIndex, multiplier=1
        var result = kc.parsePrefix(2, "d5j");
        assertEquals(2, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void startIndexBeyondEnd() {
        // startIndex past the string — should return startIndex with multiplier 1
        var result = kc.parsePrefix(3, "d5j");
        assertEquals(3, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    @Test
    void zeroMultiplier() {
        // "0j" — 0 is a valid parse result (the old code also allows this)
        var result = kc.parsePrefix(0, "0j");
        assertEquals(1, result.funcIndex());
        assertEquals(0, result.multiplier());
    }

    @Test
    void singleChar() {
        var result = kc.parsePrefix(0, "h");
        assertEquals(0, result.funcIndex());
        assertEquals(1, result.multiplier());
    }

    // --- Mfuncs set ---

    @Test
    void mfuncsContainsAllDirections() {
        assertTrue(KeyCommand.Mfuncs.contains('h'));
        assertTrue(KeyCommand.Mfuncs.contains('j'));
        assertTrue(KeyCommand.Mfuncs.contains('k'));
        assertTrue(KeyCommand.Mfuncs.contains('l'));
    }

    @Test
    void mfuncsRejectsNonDirections() {
        assertFalse(KeyCommand.Mfuncs.contains('d'));
        assertFalse(KeyCommand.Mfuncs.contains('y'));
        assertFalse(KeyCommand.Mfuncs.contains('p'));
        assertFalse(KeyCommand.Mfuncs.contains('x'));
    }
}

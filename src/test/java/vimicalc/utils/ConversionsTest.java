package vimicalc.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.utils.Conversions.*;

class ConversionsTest {

    @Nested
    class ToAlphaTests {
        @Test
        void singleLetters() {
            assertEquals("A", toAlpha(0));
            assertEquals("B", toAlpha(1));
            assertEquals("Z", toAlpha(25));
        }

        @Test
        void doubleLetters() {
            assertEquals("AA", toAlpha(26));
            assertEquals("AB", toAlpha(27));
            assertEquals("AZ", toAlpha(51));
            assertEquals("BA", toAlpha(52));
        }

        @Test
        void tripleLetters() {
            assertEquals("AAA", toAlpha(702));
        }
    }

    @Nested
    class FromAlphaTests {
        @Test
        void singleLetters() {
            assertEquals(1, fromAlpha("A"));
            assertEquals(2, fromAlpha("B"));
            assertEquals(26, fromAlpha("Z"));
        }

        @Test
        void doubleLetters() {
            assertEquals(27, fromAlpha("AA"));
            assertEquals(28, fromAlpha("AB"));
            assertEquals(52, fromAlpha("AZ"));
            assertEquals(53, fromAlpha("BA"));
        }

        @Test
        void tripleLetters() {
            assertEquals(703, fromAlpha("AAA"));
        }
    }

    @Nested
    class RoundTripTests {
        @Test
        void toAlphaThenFromAlpha() {
            // toAlpha is 0-based, fromAlpha returns 1-based
            for (int i = 0; i < 100; i++) {
                assertEquals(i + 1, fromAlpha(toAlpha(i)));
            }
        }
    }

    @Nested
    class IsNumberTests {
        @Test
        void integers() {
            assertTrue(isNumber("42"));
            assertTrue(isNumber("0"));
            assertTrue(isNumber("-7"));
        }

        @Test
        void decimals() {
            assertTrue(isNumber("3.14"));
            assertTrue(isNumber("-0.5"));
        }

        @Test
        void nonNumbers() {
            assertFalse(isNumber("abc"));
            assertFalse(isNumber(""));
            assertFalse(isNumber("12abc"));
            assertFalse(isNumber("A1"));
        }
    }

    @Nested
    class CoordsStrToIntsTests {
        @Test
        void simpleReference() {
            assertArrayEquals(new int[]{2, 3}, coordsStrToInts("B3"));
        }

        @Test
        void firstCell() {
            assertArrayEquals(new int[]{1, 1}, coordsStrToInts("A1"));
        }

        @Test
        void doubleLetterColumn() {
            assertArrayEquals(new int[]{27, 1}, coordsStrToInts("AA1"));
        }

        @Test
        void multiDigitRow() {
            assertArrayEquals(new int[]{1, 123}, coordsStrToInts("A123"));
        }

        @Test
        void lowercaseIsAccepted() {
            assertArrayEquals(new int[]{2, 3}, coordsStrToInts("b3"));
        }

        @Test
        void invalidReturnsZeros() {
            assertArrayEquals(new int[]{0, 0}, coordsStrToInts(""));
        }
    }

    @Nested
    class CoordsIntsToStrTests {
        @Test
        void simpleReference() {
            assertEquals("A3", coordsIntsToStr(1, 3));
        }

        @Test
        void doubleLetterColumn() {
            assertEquals("AA1", coordsIntsToStr(27, 1));
        }
    }

    @Nested
    class RelToAbsCoordsTests {
        @Test
        void moveDown() {
            // "2j" from (1,1) = 2 rows down = A3
            assertEquals("A3", relToAbsCoords("2j", 1, 1));
        }

        @Test
        void moveRight() {
            // "3l" from (1,1) = 3 columns right = D1
            assertEquals("D1", relToAbsCoords("3l", 1, 1));
        }

        @Test
        void moveLeft() {
            // "h" from (3,1) = 1 column left = B1
            assertEquals("B1", relToAbsCoords("h", 3, 1));
        }

        @Test
        void moveUp() {
            // "k" from (1,3) = 1 row up = A2
            assertEquals("A2", relToAbsCoords("k", 1, 3));
        }

        @Test
        void chainedMovement() {
            // "2j3l" from (1,1) = 2 down, 3 right = D3
            assertEquals("D3", relToAbsCoords("2j3l", 1, 1));
        }

        @Test
        void singleMovementNoMultiplier() {
            // "j" from (1,1) = 1 down = A2
            assertEquals("A2", relToAbsCoords("j", 1, 1));
        }
    }
}

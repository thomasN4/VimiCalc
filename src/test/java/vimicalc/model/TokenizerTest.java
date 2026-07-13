package vimicalc.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Nested
    class BasicTokenization {
        @Test
        void tokenizesNumbersAsLiterals() {
            Token[] result = Tokenizer.tokenize("42");
            assertEquals(1, result.length);
            assertTrue(result[0].isLiteral());
            assertFalse(result[0].isSymbol());
            assertEquals(42.0, result[0].getVal());
        }

        @Test
        void tokenizesIdentifiersAsSymbols() {
            Token[] result = Tokenizer.tokenize("sin");
            assertEquals(1, result.length);
            assertTrue(result[0].isSymbol());
            assertEquals("sin", result[0].getSymbol());
        }

        @Test
        void stripsParentheses() {
            Token[] result = Tokenizer.tokenize("(A1:C1)");
            assertEquals(1, result.length);
            assertEquals("A1:C1", result[0].getSymbol());
        }

        @Test
        void multipleTokens() {
            Token[] result = Tokenizer.tokenize("3 4 +");
            assertEquals(3, result.length);
            assertTrue(result[0].isLiteral());
            assertEquals(3.0, result[0].getVal());
            assertTrue(result[1].isLiteral());
            assertEquals(4.0, result[1].getVal());
            assertTrue(result[2].isSymbol());
            assertEquals("+", result[2].getSymbol());
        }
    }

    /**
     * Known quirks of the current tokenizer, pinned as-is.
     * Fixing them is a separate sub-issue of #58.
     */
    @Nested
    class KnownQuirks {
        @Test
        void emptyInputThrowsArrayIndexOutOfBoundsException() {
            // Backing array is sized from pre-padding length (0), then a space is
            // appended and produces one token write past the array bound.
            assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> Tokenizer.tokenize(""));
        }

        @Test
        void consecutiveSpacesProduceEmptyStringSymbolTokens() {
            Token[] result = Tokenizer.tokenize("3  4");
            // "3" + empty (from double space) + "4"
            assertEquals(3, result.length);
            assertTrue(result[0].isLiteral());
            assertEquals(3.0, result[0].getVal());
            assertTrue(result[1].isSymbol());
            assertEquals("", result[1].getSymbol());
            assertTrue(result[2].isLiteral());
            assertEquals(4.0, result[2].getVal());
        }
    }
}

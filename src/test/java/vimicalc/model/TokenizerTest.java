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

    @Nested
    class EdgeCases {
        @Test
        void emptyInputReturnsEmptyArray() {
            Token[] result = Tokenizer.tokenize("");
            assertEquals(0, result.length);
        }

        @Test
        void consecutiveSpacesDoNotProduceTokens() {
            Token[] result = Tokenizer.tokenize("3  4");
            assertEquals(2, result.length);
            assertTrue(result[0].isLiteral());
            assertEquals(3.0, result[0].getVal());
            assertTrue(result[1].isLiteral());
            assertEquals(4.0, result[1].getVal());
        }

        @Test
        void leadingAndTrailingSpacesIgnored() {
            Token[] result = Tokenizer.tokenize("  3 4  ");
            assertEquals(2, result.length);
            assertEquals(3.0, result[0].getVal());
            assertEquals(4.0, result[1].getVal());
        }

        @Test
        void whitespaceOnlyReturnsEmptyArray() {
            Token[] result = Tokenizer.tokenize("   ");
            assertEquals(0, result.length);
        }

        @Test
        void doubleSpacesInExpression() {
            Token[] result = Tokenizer.tokenize("3  5 +");
            assertEquals(3, result.length);
            assertEquals(3.0, result[0].getVal());
            assertEquals(5.0, result[1].getVal());
            assertEquals("+", result[2].getSymbol());
        }
    }
}

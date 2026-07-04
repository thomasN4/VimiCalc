package vimicalc.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Nested
    class ConstructorTests {
        @Test
        void emptyCell() {
            Cell c = new Cell(1, 1);
            assertEquals(1, c.xCoord());
            assertEquals(1, c.yCoord());
            assertNull(c.txt());
            assertNull(c.value());
            assertNull(c.formula());
            assertTrue(c.isEmpty());
        }

        @Test
        void textCell() {
            Cell c = new Cell(2, 3, "hello");
            assertEquals("hello", c.txt());
            assertNull(c.value());
            assertFalse(c.isEmpty());
        }

        @Test
        void numericTextCell() {
            Cell c = new Cell(1, 1, "42");
            assertEquals("42", c.txt());
            assertEquals(42.0, c.value());
        }

        @Test
        void decimalTextCell() {
            Cell c = new Cell(1, 1, "3.14");
            assertNotNull(c.value());
            assertEquals(3.14, c.value(), 1e-10);
        }

        @Test
        void numericValueCell() {
            Cell c = new Cell(1, 1, 42.0);
            assertEquals(42.0, c.value());
            assertEquals("42", c.txt());
        }

        @Test
        void numericValueCellWithDecimal() {
            Cell c = new Cell(1, 1, 3.14);
            assertEquals(3.14, c.value());
            // txt should be formatted with DecimalFormat("0.0")
            assertNotNull(c.txt());
        }

        @Test
        void formulaCell() {
            Formula f = new Formula("3 4 +", 1, 1);
            Cell c = new Cell(1, 1, 7.0, f);
            assertEquals(7.0, c.value());
            assertEquals("7", c.txt());
            assertNotNull(c.formula());
            assertEquals("3 4 +", c.formula().getTxt());
        }

        @Test
        void formulaCellWithLiteralNumberDiscardsFormula() {
            Formula f = new Formula("42", 1, 1);
            Cell c = new Cell(1, 1, 42.0, f);
            assertNull(c.formula());
            assertEquals(42.0, c.value());
        }
    }

    @Nested
    class CopyTests {
        @Test
        void copyPreservesText() {
            Cell original = new Cell(2, 3, "hello");
            Cell copy = original.copy();
            assertEquals("hello", copy.txt());
            assertEquals(2, copy.xCoord());
            assertEquals(3, copy.yCoord());
        }

        @Test
        void copyPreservesNumericValue() {
            Cell original = new Cell(1, 1, 42.0);
            Cell copy = original.copy();
            assertEquals(42.0, copy.value());
        }

        @Test
        void copyDeepCopiesFormula() {
            Formula f = new Formula("3 4 +", 1, 1);
            Cell original = new Cell(1, 1, 7.0, f);
            Cell copy = original.copy();
            assertNotNull(copy.formula());
            assertNotSame(original.formula(), copy.formula());
            assertEquals("3 4 +", copy.formula().getTxt());
        }

        @Test
        void copyPreservesMergeState() {
            Cell start = new Cell(1, 1, "merged");
            Cell end = new Cell(3, 3);
            start.setMergeStart(true);
            start.mergeWith(end);

            Cell copy = start.copy();
            assertTrue(copy.isMergeStart());
            assertSame(end, copy.getMergeDelimiter()); // shallow copy of merge ref
        }

        @Test
        void copyOfEmptyCell() {
            Cell original = new Cell(5, 5);
            Cell copy = original.copy();
            assertNull(copy.txt());
            assertNull(copy.value());
            assertTrue(copy.isEmpty());
        }
    }

    @Nested
    class IsEmptyTests {
        @Test
        void emptyCell() {
            assertTrue(new Cell(1, 1).isEmpty());
        }

        @Test
        void cellWithText() {
            assertFalse(new Cell(1, 1, "text").isEmpty());
        }

        @Test
        void cellWithMerge() {
            Cell c = new Cell(1, 1);
            c.mergeWith(new Cell(2, 2));
            assertFalse(c.isEmpty());
        }
    }

    @Nested
    class CorrectTxtTests {
        @Test
        void integerString() {
            Cell c = new Cell(1, 1);
            c.correctTxt("42");
            assertEquals(42.0, c.value());
        }

        @Test
        void decimalString() {
            Cell c = new Cell(1, 1);
            c.correctTxt("3.14");
            assertEquals(3.14, c.value(), 1e-10);
        }

        @Test
        void nonNumericString() {
            Cell c = new Cell(1, 1);
            c.correctTxt("hello");
            assertNull(c.value());
        }
    }

    @Nested
    class SetFormulaResultTests {
        @Test
        void integerResult() {
            Cell c = new Cell(1, 1);
            Formula f = new Formula("3 4 +", 1, 1);
            c.setFormulaResult(7.0, f);
            assertEquals("7", c.txt());
            assertEquals(7.0, c.value());
            assertSame(f, c.formula());
        }

        @Test
        void decimalResult() {
            Cell c = new Cell(1, 1);
            Formula f = new Formula("1 3 /", 1, 1);
            c.setFormulaResult(0.333, f);
            assertNotNull(c.txt());
            assertEquals(0.333, c.value(), 1e-10);
        }
    }

    @Nested
    class EqualsAndHashCodeTests {
        @Test
        void sameCoordsAndText() {
            Cell a = new Cell(1, 2, "hello");
            Cell b = new Cell(1, 2, "hello");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void differentCoords() {
            Cell a = new Cell(1, 1, "hello");
            Cell b = new Cell(2, 1, "hello");
            assertNotEquals(a, b);
        }

        @Test
        void differentText() {
            Cell a = new Cell(1, 1, "hello");
            Cell b = new Cell(1, 1, "world");
            assertNotEquals(a, b);
        }
    }

    @Nested
    class MergeTests {
        @Test
        void defaultMergeState() {
            Cell c = new Cell(1, 1);
            assertFalse(c.isMergeStart());
            assertNull(c.getMergeDelimiter());
        }

        @Test
        void setMergeStart() {
            Cell c = new Cell(1, 1);
            c.setMergeStart(true);
            assertTrue(c.isMergeStart());
        }

        @Test
        void mergeWith() {
            Cell start = new Cell(1, 1);
            Cell end = new Cell(3, 3);
            start.mergeWith(end);
            assertSame(end, start.getMergeDelimiter());
        }

        @Test
        void unmerge() {
            Cell c = new Cell(1, 1);
            c.mergeWith(new Cell(3, 3));
            c.mergeWith(null);
            assertNull(c.getMergeDelimiter());
        }
    }
}

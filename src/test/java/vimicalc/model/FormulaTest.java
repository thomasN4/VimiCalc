package vimicalc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vimicalc.view.Positions;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FormulaTest {

    private Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Sheet();
        Positions positions = new Positions(
            0, 0, 2000, 2000, 100, 30,
            new HashMap<>(), new HashMap<>()
        );
        positions.generate(0, 0);
        sheet.setPositions(positions);
    }

    private double eval(String rpn) throws Exception {
        Formula f = new Formula(rpn, 1, 1);
        return f.interpret(sheet);
    }

    // ── Lexer ──

    @Nested
    class LexerTests {
        @Test
        void tokenizesNumbers() {
            Formula f = new Formula("", 1, 1);
            Lexeme[] result = f.lexer("42");
            assertEquals(1, result.length);
            assertFalse(result[0].isFunction());
            assertEquals(42.0, result[0].getVal());
        }

        @Test
        void tokenizesIdentifiers() {
            Formula f = new Formula("", 1, 1);
            Lexeme[] result = f.lexer("sin");
            assertEquals(1, result.length);
            assertTrue(result[0].isFunction());
            assertEquals("sin", result[0].getFunc());
        }

        @Test
        void stripsParentheses() {
            Formula f = new Formula("", 1, 1);
            Lexeme[] result = f.lexer("(A1:C1)");
            assertEquals(1, result.length);
            assertEquals("A1:C1", result[0].getFunc());
        }

        @Test
        void multipleTokens() {
            Formula f = new Formula("", 1, 1);
            Lexeme[] result = f.lexer("3 4 +");
            assertEquals(3, result.length);
            assertFalse(result[0].isFunction());
            assertEquals(3.0, result[0].getVal());
            assertFalse(result[1].isFunction());
            assertEquals(4.0, result[1].getVal());
            assertTrue(result[2].isFunction());
            assertEquals("+", result[2].getFunc());
        }
    }

    // ── Basic arithmetic ──

    @Nested
    class ArithmeticTests {
        @Test
        void addition() throws Exception {
            assertEquals(7.0, eval("3 4 +"));
        }

        @Test
        void subtraction() throws Exception {
            assertEquals(1.0, eval("5 4 -"));
        }

        @Test
        void multiplication() throws Exception {
            assertEquals(12.0, eval("3 4 *"));
        }

        @Test
        void division() throws Exception {
            assertEquals(2.5, eval("5 2 /"));
        }

        @Test
        void modulo() throws Exception {
            assertEquals(1.0, eval("7 3 mod"));
        }

        @Test
        void exponentiation() throws Exception {
            assertEquals(8.0, eval("2 3 ^"));
        }

        @Test
        void chainedOperations() throws Exception {
            // (2 + 3) * 4 = 20
            assertEquals(20.0, eval("2 3 + 4 *"));
        }
    }

    // ── Trig functions ──

    @Nested
    class TrigTests {
        @Test
        void sinZero() throws Exception {
            assertEquals(0.0, eval("0 sin"), 1e-10);
        }

        @Test
        void cosZero() throws Exception {
            assertEquals(1.0, eval("0 cos"), 1e-10);
        }

        @Test
        void tanZero() throws Exception {
            assertEquals(0.0, eval("0 tan"), 1e-10);
        }

        @Test
        void asinZero() throws Exception {
            assertEquals(0.0, eval("0 asin"), 1e-10);
        }

        @Test
        void acosOne() throws Exception {
            assertEquals(0.0, eval("1 acos"), 1e-10);
        }

        @Test
        void atanZero() throws Exception {
            assertEquals(0.0, eval("0 atan"), 1e-10);
        }
    }

    // ── Log/exp functions ──

    @Nested
    class LogExpTests {
        @Test
        void lnE() throws Exception {
            assertEquals(1.0, eval(Math.E + " ln"), 1e-10);
        }

        @Test
        void log10Of100() throws Exception {
            assertEquals(2.0, eval("100 log10"), 1e-10);
        }

        @Test
        void logBase() throws Exception {
            // logBase(8, 2) = ln(8)/ln(2) = 3
            assertEquals(3.0, eval("8 2 logBase"), 1e-10);
        }

        @Test
        void expOne() throws Exception {
            assertEquals(Math.E, eval("1 exp"), 1e-10);
        }
    }

    // ── Constants ──

    @Test
    void piConstant() throws Exception {
        assertEquals(Math.PI, eval("PI"), 1e-10);
    }

    // ── Negative numbers ──

    @Test
    void negativeNumber() throws Exception {
        assertEquals(-5.0, eval("-5"));
    }

    @Test
    void negativeInExpression() throws Exception {
        assertEquals(-2.0, eval("-5 3 +"));
    }

    // ── Cell references ──

    @Test
    void cellReference() throws Exception {
        sheet.simplyAddCell(new Cell(2, 1, 42.0)); // B1 = 42
        Formula f = new Formula("B1", 1, 1);
        assertEquals(42.0, f.interpret(sheet));
    }

    // ── Range operations ──

    @Nested
    class RangeTests {
        @BeforeEach
        void populateCells() {
            // A1=1, B1=2, C1=3
            sheet.simplyAddCell(new Cell(1, 1, 1.0));
            sheet.simplyAddCell(new Cell(2, 1, 2.0));
            sheet.simplyAddCell(new Cell(3, 1, 3.0));
        }

        @Test
        void sum() throws Exception {
            Formula f = new Formula("(A1:C1) sum", 4, 1);
            assertEquals(6.0, f.interpret(sheet));
        }

        @Test
        void product() throws Exception {
            Formula f = new Formula("(A1:C1) prod", 4, 1);
            assertEquals(6.0, f.interpret(sheet));
        }

        @Test
        void quotient() throws Exception {
            // 1 / 2 / 3 = 1/6
            Formula f = new Formula("(A1:C1) quot", 4, 1);
            assertEquals(1.0 / 6.0, f.interpret(sheet), 1e-10);
        }
    }

    // ── Matrix operations ──

    @Nested
    class MatrixTests {
        @Test
        void determinant2x2() throws Exception {
            // | 1  2 |
            // | 3  4 | = 1*4 - 2*3 = -2
            sheet.simplyAddCell(new Cell(1, 1, 1.0));
            sheet.simplyAddCell(new Cell(2, 1, 2.0));
            sheet.simplyAddCell(new Cell(1, 2, 3.0));
            sheet.simplyAddCell(new Cell(2, 2, 4.0));

            Formula f = new Formula("(A1:B2) det", 3, 1);
            assertEquals(-2.0, f.interpret(sheet), 1e-10);
        }

        @Test
        void determinant3x3() throws Exception {
            // | 6  1  1 |
            // | 4 -2  5 | = 6(-2*2 - 5*3) - 1(4*2 - 5*1) + 1(4*3 - (-2)*1)
            // | 2  8  7 |   (wait let me just compute)
            // Actually let me use a simple known matrix:
            // | 1  0  0 |
            // | 0  1  0 | = 1 (identity)
            // | 0  0  1 |
            sheet.simplyAddCell(new Cell(1, 1, 1.0));
            sheet.simplyAddCell(new Cell(2, 1, 0.0));
            sheet.simplyAddCell(new Cell(3, 1, 0.0));
            sheet.simplyAddCell(new Cell(1, 2, 0.0));
            sheet.simplyAddCell(new Cell(2, 2, 1.0));
            sheet.simplyAddCell(new Cell(3, 2, 0.0));
            sheet.simplyAddCell(new Cell(1, 3, 0.0));
            sheet.simplyAddCell(new Cell(2, 3, 0.0));
            sheet.simplyAddCell(new Cell(3, 3, 1.0));

            Formula f = new Formula("(A1:C3) det", 4, 1);
            assertEquals(1.0, f.interpret(sheet), 1e-10);
        }

        @Test
        void for1Pos() {
            Formula f = new Formula("", 1, 1);
            double[] row = {1, 2, 3};
            double[] col = {4, 5, 6};
            // 1*4 + 2*5 + 3*6 = 32
            assertEquals(32.0, f.for1Pos(row, col));
        }

        @Test
        void matMul() throws Exception {
            // Matrix A (A1:B2): | 1  2 |    Matrix B (A3:B4): | 5  6 |
            //                   | 3  4 |                      | 7  8 |
            // Result: | 1*5+2*7  1*6+2*8 | = | 19  22 |
            //         | 3*5+4*7  3*6+4*8 |   | 43  50 |
            sheet.simplyAddCell(new Cell(1, 1, 1.0));
            sheet.simplyAddCell(new Cell(2, 1, 2.0));
            sheet.simplyAddCell(new Cell(1, 2, 3.0));
            sheet.simplyAddCell(new Cell(2, 2, 4.0));
            sheet.simplyAddCell(new Cell(1, 3, 5.0));
            sheet.simplyAddCell(new Cell(2, 3, 6.0));
            sheet.simplyAddCell(new Cell(1, 4, 7.0));
            sheet.simplyAddCell(new Cell(2, 4, 8.0));

            Formula f = new Formula("(A1:B2) (A3:B4) matMul", 3, 5);
            double topLeft = f.interpret(sheet);
            assertEquals(19.0, topLeft, 1e-10);
        }

        @Test
        void transpose() throws Exception {
            // | 1  2  3 |  transposed to  | 1  4 |
            // | 4  5  6 |                 | 2  5 |
            //                             | 3  6 |
            sheet.simplyAddCell(new Cell(1, 1, 1.0));
            sheet.simplyAddCell(new Cell(2, 1, 2.0));
            sheet.simplyAddCell(new Cell(3, 1, 3.0));
            sheet.simplyAddCell(new Cell(1, 2, 4.0));
            sheet.simplyAddCell(new Cell(2, 2, 5.0));
            sheet.simplyAddCell(new Cell(3, 2, 6.0));

            Formula f = new Formula("(A1:C2) tpose", 4, 3);
            double topLeft = f.interpret(sheet);
            assertEquals(1.0, topLeft, 1e-10);
        }
    }

    // ── Error cases ──

    @Test
    void notEnoughArgsForOperator() {
        assertThrows(Exception.class, () -> eval("+"));
    }
}

package vimicalc.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.pow;
import static vimicalc.controller.KeyCommand.Mfuncs;
import static vimicalc.utils.Conversions.*;

/**
 * Evaluates Reverse Polish Notation (RPN) formulas against the spreadsheet.
 *
 * <p>Supports arithmetic operators ({@code +}, {@code -}, {@code *}, {@code /},
 * {@code mod}, {@code ^}), trigonometric and logarithmic functions
 * ({@code sin}, {@code cos}, {@code tan}, {@code asin}, {@code acos}, {@code atan},
 * {@code ln}, {@code log10}, {@code logBase}, {@code exp}, {@code PI}, etc.),
 * cell references (both absolute like "B3" and relative like "2j3l"),
 * cell range operations ({@code sum}, {@code prod}, {@code quot}), and matrix
 * operations ({@code det}, {@code tpose}, {@code matMul}).</p>
 *
 * <p>Evaluation is a single left-to-right pass over a value stack: value tokens
 * are pushed, and each operator or function pops its operands and pushes the
 * result. Cell references are resolved as they are encountered; ranges ride the
 * stack as {@link Ref}s until an aggregate or matrix function consumes them.</p>
 *
 * <p>During evaluation, dependencies are registered in the {@link Sheet}'s
 * dependency graph so that changes propagate correctly.</p>
 */
public class Formula {
    /** Column index of the cell this formula is associated with. */
    private final int xC;
    /** Row index of the cell this formula is associated with. */
    private final int yC;
    /** The raw RPN formula text. */
    private String txt;

    /**
     * Creates a formula from the given RPN expression text.
     *
     * @param txt the RPN formula text
     * @param xC  the column index of the owning cell
     * @param yC  the row index of the owning cell
     */
    public Formula(String txt, int xC, int yC) {
        this.txt = txt;
        this.xC = xC;
        this.yC = yC;
    }

    /**
     * Returns the raw formula text.
     *
     * @return the formula text
     */
    public String getTxt() {
        return txt;
    }

    /**
     * Sets the raw formula text.
     *
     * @param txt the new formula text
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * Tokenises the formula text and evaluates it, returning the numeric result.
     *
     * @param sheet the sheet context (for cell lookups and dependency tracking)
     * @return the computed value
     * @throws Exception if evaluation fails (e.g. missing args, circular dependency)
     */
    public double interpret(Sheet sheet) throws Exception {
        return interpret(Tokenizer.tokenize(txt), sheet)[0].getVal();
    }

    // ─────────────────────────────── Operand model ───────────────────────────────

    /**
     * A value on the evaluation stack: a concrete {@link Num number}, an
     * {@link Empty} reference to a blank cell (which stands in for an operator's
     * identity element), or an unresolved range {@link Ref} (e.g. {@code "A1:C1"})
     * consumed by aggregate/matrix functions.
     */
    private sealed interface Operand permits Num, Empty, Ref {}
    private record Num(double value) implements Operand {}
    private record Empty() implements Operand {}
    private record Ref(String coords) implements Operand {}

    /**
     * Resolves an operand to a scalar. Concrete numbers yield their value;
     * everything else (a blank cell, or a range used where a number was expected)
     * collapses to the supplied identity element.
     */
    private static double scalar(Operand o, double identity) {
        return (o instanceof Num n) ? n.value() : identity;
    }

    // ────────────────────────────── Operator tables ──────────────────────────────

    /** Unary math functions; their operand is read as a raw scalar (identity 0). */
    private static final Map<String, DoubleUnaryOperator> UNARY = Map.ofEntries(
        Map.entry("sin",   Math::sin),
        Map.entry("cos",   Math::cos),
        Map.entry("tan",   Math::tan),
        Map.entry("asin",  Math::asin),
        Map.entry("acos",  Math::acos),
        Map.entry("atan",  Math::atan),
        Map.entry("ln",    Math::log),
        Map.entry("log10", Math::log10),
        Map.entry("exp",   x -> pow(Math.E, x))
    );

    /**
     * A binary operator together with the identity element a blank operand stands
     * in for (0 for additive-style ops, 1 for multiplicative-style ops).
     */
    private record BinaryOp(DoubleBinaryOperator fn, double identity) {}

    private static final Map<String, BinaryOp> BINARY = Map.ofEntries(
        Map.entry("+",       new BinaryOp(Double::sum,      0)),
        Map.entry("-",       new BinaryOp((a, b) -> a - b,  0)),
        Map.entry("*",       new BinaryOp((a, b) -> a * b,  1)),
        Map.entry("/",       new BinaryOp((a, b) -> a / b,  1)),
        Map.entry("mod",     new BinaryOp((a, b) -> a % b,  1)),
        // ^ substitutes 0 (not 1) for a blank operand, matching historical behaviour.
        Map.entry("^",       new BinaryOp(Math::pow,        0)),
        Map.entry("logBase", new BinaryOp((a, b) -> Math.log(a) / Math.log(b), 0))
    );

    // ──────────────────────────────── Evaluation ─────────────────────────────────

    /**
     * Evaluates an RPN expression represented as a token array.
     *
     * @param args  the tokenised RPN expression
     * @param sheet the sheet context for cell lookups and dependency tracking
     * @return a single-element token array holding the result
     * @throws Exception if evaluation fails (not enough args, invalid refs, cycle, ...)
     */
    public Token[] interpret(Token[] args, Sheet sheet) throws Exception {
        boolean partOfExpression = args.length > 1;
        Deque<Operand> stack = new ArrayDeque<>();

        for (Token token : args) {
            if (!token.isSymbol()) stack.push(new Num(token.getVal()));
            else evalFunction(token.getSymbol(), stack, sheet, partOfExpression);
        }

        if (stack.size() != 1)
            throw new Exception("Malformed formula: " + stack.size() + " value(s) left on the stack.");
        return new Token[]{new Token(scalar(stack.pop(), 0))};
    }

    /** Applies a single function/operator token to the working stack. */
    private void evalFunction(@NotNull String func, Deque<Operand> stack, Sheet sheet,
                              boolean partOfExpression) throws Exception {
        if (func.isEmpty()) return;  // stray token from double/trailing spaces

        // Negated reference (e.g. "-B1", "-PI", "-2j"). Plain negative numbers such
        // as "-5" are recognised as numbers by the tokenizer and never reach here.
        if (func.charAt(0) == '-' && func.length() > 1) {
            Deque<Operand> inner = new ArrayDeque<>();
            evalFunction(func.substring(1), inner, sheet, partOfExpression);
            stack.push(new Num(-scalar(pop(inner), 0)));
            return;
        }

        if (func.equals("PI")) {
            stack.push(new Num(Math.PI));
            return;
        }

        DoubleUnaryOperator unary = UNARY.get(func);
        if (unary != null) {
            stack.push(new Num(unary.applyAsDouble(scalar(pop(stack), 0))));
            return;
        }

        BinaryOp binary = BINARY.get(func);
        if (binary != null) {
            double y = scalar(pop(stack), binary.identity());
            double x = scalar(pop(stack), binary.identity());
            stack.push(new Num(binary.fn().applyAsDouble(x, y)));
            return;
        }

        switch (func) {
            case "sum"  -> stack.push(new Num(sum(createVectorFromArea(popRange(stack), sheet))));
            case "prod" -> stack.push(new Num(product(createVectorFromArea(popRange(stack), sheet))));
            case "quot" -> stack.push(new Num(quotient(createVectorFromArea(popRange(stack), sheet))));
            case "det"  -> stack.push(new Num(determinant(createMatrixFromArea(popRange(stack), sheet))));
            case "tpose" -> stack.push(new Num(transpose(popRange(stack), sheet)));
            case "matMul" -> {
                String second = popRange(stack);  // top of stack is the right-hand matrix
                String first = popRange(stack);
                stack.push(new Num(matMul(first, second, sheet)));
            }
            default -> stack.push(resolveToken(func, sheet, partOfExpression));
        }
    }

    private static Operand pop(@NotNull Deque<Operand> stack) throws Exception {
        if (stack.isEmpty()) throw new Exception("Not enough args.");
        return stack.pop();
    }

    /** Pops an operand expected to be an (unresolved) cell range. */
    private static String popRange(Deque<Operand> stack) throws Exception {
        if (pop(stack) instanceof Ref r) return r.coords();
        throw new Exception("Expected a cell range.");
    }

    /**
     * Resolves a bare token (a cell reference or a range) to an {@link Operand}.
     * Ranges become {@link Ref}s; cell references (absolute or relative) are read
     * from the sheet.
     */
    private Operand resolveToken(@NotNull String func, Sheet sheet, boolean partOfExpression) throws Exception {
        if (func.contains(":") || func.contains("\\") || func.contains(";"))
            return new Ref(func);
        String coords = looksRelative(func) ? relToAbsCoords(func, xC, yC) : func;
        return resolveCell(coords, sheet, partOfExpression);
    }

    /** Whether a cell-reference token is written in Vim-style relative form (e.g. "2j3l"). */
    private static boolean looksRelative(@NotNull String func) {
        return (isNumber("" + func.charAt(0)) || Mfuncs.contains(Character.toLowerCase(func.charAt(0))))
            && Mfuncs.contains(func.charAt(func.length() - 1));
    }

    /**
     * Reads a single cell and registers the dependency on it. A blank cell yields
     * {@link Empty} when it appears inside a larger expression (so it can act as an
     * operator's identity element), or the number {@code 0} when it is the whole
     * formula.
     */
    private Operand resolveCell(String coords, @NotNull Sheet sheet, boolean partOfExpression) throws Exception {
        Cell c = sheet.findCell(coords);
        sheet.addDependent(xC, yC);
        sheet.addDepended(c.xCoord(), c.yCoord(), sheet.findDependency(xC, yC));

        if (c.value() == null) return partOfExpression ? new Empty() : new Num(0);
        return new Num(c.value());
    }

    // ─────────────────────────── Range / aggregate helpers ───────────────────────

    /**
     * Parses a range string (e.g. "A1:C3", or relative "2j:4j2l") into absolute
     * bounds {@code {firstX, firstY, lastX, lastY}}. The two endpoints are split on
     * {@code ':'} or {@code ';'} and converted from relative to absolute form when
     * needed.
     */
    private int[] rangeBounds(@NotNull String range, @NotNull Sheet sheet) {
        int split = 0;
        while (range.charAt(split) != ':' && range.charAt(split) != ';') split++;
        String first = range.substring(0, split);
        String last = range.substring(split + 1);

        if (Mfuncs.contains(first.charAt(first.length() - 1)))
            first = relToAbsCoords(first, xC, yC);
        if (Mfuncs.contains(last.charAt(last.length() - 1)))
            last = relToAbsCoords(last, xC, yC);

        Cell firstCell = sheet.findCell(first);
        Cell lastCell = sheet.findCell(last);
        return new int[]{firstCell.xCoord(), firstCell.yCoord(), lastCell.xCoord(), lastCell.yCoord()};
    }

    /**
     * Extracts a 2D matrix of numeric values from a cell range (e.g. "A1:C3"),
     * registering a dependency for each cell in the range.
     *
     * @param range the range string (e.g. "A1:C3" or relative like "2j:4j2l")
     * @param sheet the sheet to read cell values from
     * @return a 2D array of doubles
     * @throws Exception if a dependency cycle is detected
     */
    protected double[][] createMatrixFromArea(@NotNull String range, @NotNull Sheet sheet) throws Exception {
        sheet.addDependent(xC, yC);
        int[] b = rangeBounds(range, sheet);
        int firstX = b[0], firstY = b[1], lastX = b[2], lastY = b[3];

        double[][] mat = new double[lastY - firstY + 1][lastX - firstX + 1];
        for (int i = 0; i <= lastY - firstY; i++) {
            for (int j = 0; j <= lastX - firstX; j++) {
                int x = firstX + j, y = firstY + i;
                Cell c = sheet.findCell(x, y);
                mat[i][j] = (c.value() == null) ? 0 : c.value();
                sheet.addDepended(x, y, sheet.findDependency(xC, yC));
            }
        }
        return mat;
    }

    /**
     * Extracts a flat array of {@link Token}s from a cell range, reading cells
     * left-to-right, top-to-bottom. Blank cells become identity tokens ("I") so
     * that aggregate functions can skip them. Used by {@code sum}, {@code prod},
     * and {@code quot}.
     *
     * @param range the range string (e.g. "A1:C3")
     * @param sheet the sheet to read cell values from
     * @return an array of value/identity tokens
     * @throws Exception if a dependency cycle is detected
     */
    protected Token[] createVectorFromArea(@NotNull String range, @NotNull Sheet sheet) throws Exception {
        sheet.addDependent(xC, yC);
        int[] b = rangeBounds(range, sheet);
        int firstX = b[0], firstY = b[1], lastX = b[2], lastY = b[3];

        Token[] vector = new Token[(lastX - firstX + 1) * (lastY - firstY + 1)];
        int k = 0;
        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                Cell c = sheet.findCell(x, y);
                vector[k++] = (c.value() == null) ? new Token("I") : new Token(c.value());
                sheet.addDepended(x, y, sheet.findDependency(xC, yC));
            }
        }
        return vector;
    }

    /** Sum of a range's values (blank cells are skipped). */
    private static double sum(@NotNull Token[] nums) {
        double s = 0;
        for (Token n : nums) if (!n.isSymbol()) s += n.getVal();
        return s;
    }

    /** Product of a range's values (blank cells are skipped). */
    private static double product(@NotNull Token[] nums) {
        double p = 1;
        for (Token n : nums) if (!n.isSymbol()) p *= n.getVal();
        return p;
    }

    /** Running quotient of a range's values (blank cells are skipped). */
    private static double quotient(@NotNull Token[] nums) {
        double q = nums[0].isSymbol() ? 1 : nums[0].getVal();
        for (int i = 1; i < nums.length; i++)
            if (!nums[i].isSymbol()) q /= nums[i].getVal();
        return q;
    }

    // ──────────────────────────────── Matrix ops ─────────────────────────────────

    /**
     * Transposes the matrix defined by the given cell range and writes the result
     * directly into the sheet starting at this formula's cell position. This is a
     * side-effecting operation: it modifies sheet cells beyond the formula's own
     * cell. Returns the value at position [0][0] of the result.
     *
     * @param coords the range string defining the source matrix
     * @param sheet  the sheet context
     * @return the value at position [0][0] of the transposed matrix
     * @throws Exception if coordinates are invalid
     */
    private double transpose(String coords, Sheet sheet) throws Exception {
        Matrix ogMat = new Matrix(createMatrixFromArea(coords, sheet));
        for (int i = 0; i < ogMat.getHeight(); i++)
            for (int j = 0; j < ogMat.getWidth(); j++)
                if (i != 0 || j != 0)
                    sheet.addCell(new Cell(xC + i, yC + j, ogMat.getRow(i)[j]));
        return ogMat.getRow(0)[0];
    }

    /** Computes the determinant of the matrix defined by the given cell range. */
    private double determinant(String coords, Sheet sheet) throws Exception {
        return determinant(createMatrixFromArea(coords, sheet));
    }

    /**
     * Recursively computes the determinant of a square matrix using cofactor
     * expansion along the first column.
     *
     * @param imat the square matrix
     * @return the determinant
     * @throws Exception if the matrix is not square
     */
    private double determinant(double[][] imat) throws Exception {
        if (imat.length != imat[0].length)
            throw new Exception("The matrix isn't square.");

        if (imat.length <= 2)
            return imat[0][0] * imat[1][1] - imat[0][1] * imat[1][0];

        Matrix[] omats = new Matrix[imat.length];
        for (int ignoredRow = 0; ignoredRow < imat.length; ignoredRow++) {
            double[][] omat = new double[imat.length - 1][imat.length - 1];
            int om_i = 0;
            for (int im_i = 0; im_i < imat.length; im_i++) {
                if (im_i == ignoredRow) continue;
                System.arraycopy(imat[im_i], 1, omat[om_i], 0, imat.length - 1);
                om_i++;
            }
            omats[ignoredRow] = new Matrix(omat);
        }

        double sum = 0;
        for (int i = 0; i < imat.length; i++)
            sum += pow(-1, i) * determinant(omats[i].getItems()) * imat[i][0];
        return sum;
    }

    /**
     * Performs matrix multiplication of two cell ranges and writes the result
     * matrix into the sheet starting at this formula's cell position. Returns the
     * value at the top-left position of the product.
     *
     * @param coords1 the range string for the left matrix
     * @param coords2 the range string for the right matrix
     * @param sheet   the sheet context
     * @return the value at position [0][0] of the product matrix
     * @throws Exception if the matrices have incompatible dimensions
     */
    public double matMul(String coords1, String coords2, Sheet sheet) throws Exception {
        Matrix mat1 = new Matrix(createMatrixFromArea(coords1, sheet));
        Matrix mat2 = new Matrix(createMatrixFromArea(coords2, sheet));

        if (mat1.getWidth() != mat2.getHeight())
            throw new Exception("There's a mismatch in the number of rows and columns.");

        for (int i = 0; i < mat1.getHeight(); i++)
            for (int j = 0; j < mat2.getWidth(); j++)
                if (i != 0 || j != 0)
                    sheet.addCell(new Cell(xC + j, yC + i, dotProduct(mat1.getRow(i), mat2.getCol(j))));

        return dotProduct(mat1.getRow(0), mat2.getCol(0));
    }

    /**
     * Computes the dot product of a row vector and a column vector.
     *
     * @param row the row vector
     * @param col the column vector
     * @return the dot product
     */
    public double dotProduct(@NotNull double[] row, double[] col) {
        double v = 0;
        for (int i = 0; i < row.length; i++)
            v += row[i] * col[i];
        return v;
    }
}

/**
 * A simple wrapper around a 2D double array, providing convenient access to rows,
 * columns, and dimensions. Used by {@link Formula} for matrix operations
 * (determinant, transpose, multiplication).
 */
class Matrix {
    final double[][] items;
    final int width;
    final int height;

    @Contract(pure = true)
    Matrix(double[][] items) {
        this.items = items;
        width = items[0].length;
        height = items.length;
    }

    double[][] getItems() {
        return items;
    }

    double[] getRow(int i) {
        return items[i];
    }

    double[] getCol(int j) {
        double[] col = new double[items.length];
        for (int i = 0; i < items.length; i++)
            col[i] = items[i][j];
        return col;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }
}

package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Objects;

import static vimicalc.utils.Conversions.isNumber;
import static vimicalc.utils.Conversions.toAlpha;

/**
 * Represents a single cell in the spreadsheet.
 *
 * <p>A cell has grid coordinates ({@code xCoord}, {@code yCoord}), display text,
 * an optional numeric value, and an optional {@link Formula}. Cells can also
 * participate in merges: a merge-start cell holds a reference to the
 * merge-end delimiter, and intermediate cells point back to the start.</p>
 *
 * <p>Implements {@link Serializable} for persistence in {@code .wss} files.</p>
 */
public class Cell implements Serializable {
    /** The one-based column index. */
    private int xCoord;
    /** The row number. */
    private int yCoord;
    /** The display text shown in the cell. */
    private String txt;
    /** The formula that produced this cell's value, or {@code null} for plain text cells. */
    private Formula formula;
    /** The numeric value of the cell, or {@code null} if the cell holds only text. */
    private Double value;
    /** The decimal formatter for display text. */
    private final DecimalFormat format;
    /** For merged cells: points to the opposite corner of the merge range. */
    private Cell mergeDelimiter;
    /** {@code true} if this cell is the top-left corner of a merged range. */
    private boolean mergeStart;

    /**
     * Creates a cell with a computed formula result.
     * If the formula text is just a literal number, the formula reference is discarded.
     *
     * @param xCoord  the one-based column index
     * @param yCoord  the row number
     * @param result  the evaluated formula result
     * @param formula the formula that produced the result
     */
    public Cell(int xCoord, int yCoord, double result, @NotNull Formula formula) {
        this(xCoord, yCoord);
        value = result;
        if (result - (int) result == 0) txt = String.valueOf((int) result);
        else txt = format.format(result);
        if (isNumber(formula.getTxt()))
            this.formula = null;
        else this.formula = formula;
    }

    /**
     * Creates a cell with text content. If the text is parseable as a number,
     * the numeric {@code value} is also set.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     * @param txt    the cell's text content
     */
    public Cell(int xCoord, int yCoord, String txt) {
        this(xCoord, yCoord);
        try {
            value = (double) Integer.parseInt(txt);
            this.txt = txt;
        } catch (Exception ignored) {
            try {
                value = Double.parseDouble(txt);
                this.txt = String.valueOf(value);
                if (this.txt.contains("."))
                    this.txt = format.format(value);
            } catch (Exception ignored1) {
                this.txt = txt;
            }
        }
    }

    /**
     * Creates a cell with a numeric value. Display text is derived from the value,
     * showing integers without decimal points.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     * @param value  the numeric value
     */
    public Cell(int xCoord, int yCoord, double value) {
        this(xCoord, yCoord);
        this.value = value;
        int valInt = (int) value;
        if (valInt - value == 0.0)
            txt = ""+valInt;
        else
            txt = format.format(value);
    }

    /**
     * Creates an empty cell at the given coordinates. Used as a placeholder
     * when no data exists at a position.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     */
    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        format = new DecimalFormat("0.0");
        mergeStart = false;
        mergeDelimiter = null;
    }

    /**
     * Returns the one-based column index.
     *
     * @return the column index
     */
    public int xCoord() {
        return xCoord;
    }

    /**
     * Returns the row number.
     *
     * @return the row number
     */
    public int yCoord() {
        return yCoord;
    }

    /**
     * Returns the display text of this cell, or {@code null} if empty.
     *
     * @return the cell text
     */
    public String txt() {
        return txt;
    }

    /**
     * Returns the formula associated with this cell, or {@code null}.
     *
     * @return the formula
     */
    public Formula formula() {
        return formula;
    }

    /**
     * Returns the numeric value of this cell, or {@code null} if non-numeric.
     *
     * @return the numeric value
     */
    public Double value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Cell) obj;
        return this.xCoord == that.xCoord &&
               this.yCoord == that.yCoord &&
               Objects.equals(this.txt, that.txt) &&
               Objects.equals(this.formula, that.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoord, yCoord, txt, formula);
    }

    @Override
    public String toString() {
        String mergeDelimiterCoords;
        try {
            mergeDelimiterCoords = toAlpha(mergeDelimiter.xCoord) + mergeDelimiter.yCoord;
        } catch (Exception ignored) {
            mergeDelimiterCoords = "null";
        }
        return "Cell{" +
               "xCoord=" + xCoord +
               ", yCoord=" + yCoord +
               ", txt='" + txt + '\'' +
               ", formula=" + ((formula == null) ? "null" : formula.getTxt()) +
               ", value=" + value +
               ", format=" + format +
               ", mergeDelimiter=" + mergeDelimiterCoords +
               ", mergeStart=" + mergeStart +
               '}';
    }

    /**
     * Sets the display text of this cell.
     *
     * @param txt the new text
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * Sets the one-based column index.
     *
     * @param xCoord the new column index
     */
    public void setXCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    /**
     * Sets the row number.
     *
     * @param yCoord the new row number
     */
    public void setYCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    /**
     * Re-parses the given text to update the numeric {@code value} and
     * display text formatting. Called after INSERT mode editing to ensure
     * numeric cells are properly recognized. Only updates the display text
     * ({@code this.txt}) when it is currently {@code null} for integer values,
     * or when a decimal format is needed for floating-point values.
     *
     * @param txt the text to parse
     */
    public void correctTxt(String txt) {
        try {
            value = (double) Integer.parseInt(txt);
            if (this.txt == null) this.txt = txt;
        } catch (Exception ignored) {
            try {
                value = Double.parseDouble(txt);
                if (txt.contains("."))
                    this.txt = format.format(value);
            } catch (Exception ignored1) {
                if (this.txt == null) this.txt = txt;
            }
        }
    }
    /**
     * Sets the formula associated with this cell.
     *
     * @param formula the formula to set
     */
    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    /**
     * Updates this cell with a new formula evaluation result, formatting
     * the display text appropriately (integer vs decimal).
     *
     * @param result  the evaluated numeric result
     * @param formula the formula that produced the result
     */
    public void setFormulaResult(double result, Formula formula) {
        if (result - ((int) result) != 0.0) {
            System.out.println("Formula result isn't an int");
            txt = format.format(result);
        }
        else {
            System.out.println("Formula result is an int");
            txt = "" + (int) result;
        }
        value = result;
        this.formula = formula;
    }

    /**
     * Returns the cell at the opposite corner of this merge range, or {@code null}.
     *
     * @return the merge delimiter cell
     */
    public Cell getMergeDelimiter() {
        return mergeDelimiter;
    }

    /**
     * Sets the merge delimiter, linking this cell to a merged range.
     *
     * @param mergeDelimiter the cell at the opposite corner
     */
    public void mergeWith(Cell mergeDelimiter) {
        this.mergeDelimiter = mergeDelimiter;
    }

    /**
     * Returns {@code true} if this cell is the top-left corner of a merged range.
     *
     * @return whether this is a merge-start cell
     */
    public boolean isMergeStart() {
        return mergeStart;
    }

    /**
     * Sets whether this cell is the top-left corner of a merged range.
     *
     * @param mergeStart the merge-start flag
     */
    public void setMergeStart(boolean mergeStart) {
        this.mergeStart = mergeStart;
    }

    /**
     * Returns {@code true} if this cell has no merge reference and no text content.
     *
     * @return whether the cell is empty
     */
    public boolean isEmpty() {
        // Uses non-short-circuit & intentionally: both conditions are cheap
        // and this avoids a branch prediction miss on a frequently called path.
        return (mergeDelimiter == null) & (txt == null);
    }

    /**
     * Creates a copy of this cell, including its formula (if any)
     * and merge state. The formula is deep-copied, but the
     * {@code mergeDelimiter} is a shallow reference to the same cell object.
     *
     * @return a new {@code Cell} with the same data
     */
    public Cell copy() {
        Cell c;
        if (formula != null) {
            c = new Cell(
                xCoord,
                yCoord,
                value,
                new Formula(formula.getTxt(), xCoord, yCoord)
            );
        }
        else if (isNumber(txt)) {
            c = new Cell(
                xCoord,
                yCoord,
                value
            );
        }
        else {
            c = new Cell(
                xCoord,
                yCoord,
                txt
            );
        }
        c.setMergeStart(mergeStart);
        c.mergeWith(mergeDelimiter);
        return c;
    }
}
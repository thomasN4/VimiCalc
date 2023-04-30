package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Objects;

import static vimicalc.utils.Conversions.isNumber;
import static vimicalc.utils.Conversions.toAlpha;

public class Cell implements Serializable {
    private final int xCoord;
    private final int yCoord;
    private String txt;
    private Formula formula;
    private Double value;
    private final DecimalFormat format;  // final, pour l'instant
    private Cell mergeDelimiter;
    private boolean mergeStart;

    public Cell(int xCoord, int yCoord, double result, @NotNull Formula formula) {
        this(xCoord, yCoord);
        value = result;
        txt = String.valueOf(result);
        if (txt.contains("."))
            txt = format.format(result);
        if (isNumber(formula.getTxt()))
            this.formula = null;
        else this.formula = formula;
    }

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

    public Cell(int xCoord, int yCoord, double value) {
        this(xCoord, yCoord);
        this.value = value;
        int valInt = (int) value;
        if (valInt - value == 0.0)
            txt = ""+valInt;
        else
            txt = format.format(value);
    }

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        format = new DecimalFormat("0.0");
        mergeStart = false;
        mergeDelimiter = null;
    }

    public int xCoord() {
        return xCoord;
    }

    public int yCoord() {
        return yCoord;
    }

    public String txt() {
        return txt;
    }

    public Formula formula() {
        return formula;
    }

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

    public void setTxt(String txt) {
        this.txt = txt;
    }

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
    public void setFormula(Formula formula) {
        this.formula = formula;
    }

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

    public Cell getMergeDelimiter() {
        return mergeDelimiter;
    }

    public void mergeWith(Cell mergeDelimiter) {
        this.mergeDelimiter = mergeDelimiter;
    }

    public boolean isMergeStart() {
        return mergeStart;
    }

    public void setMergeStart(boolean mergeStart) {
        this.mergeStart = mergeStart;
    }

    public boolean isEmpty() {
        return (mergeDelimiter == null) & (txt == null);
    }

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
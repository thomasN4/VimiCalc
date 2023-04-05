package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Objects;

import static vimicalc.utils.Conversions.isNumber;

public class Cell {
    private final int xCoord;
    private final int yCoord;
    private String txt;
    private Formula formula;
    private double value;
    private final DecimalFormat format;  // final, pour l'instant
    private Cell mergedWith;
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
            value = Integer.parseInt(txt);
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
        if (valInt - value == 0)
            txt = String.valueOf(valInt);
        else
            txt = format.format(value);
    }

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        format = new DecimalFormat("0.0");
        mergeStart = false;
        mergedWith = null;
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

    public double value() {
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
        return "Cell[" +
               "xCoord=" + xCoord + ", " +
               "yCoord=" + yCoord + ", " +
               "txt=" + txt + ", " +
               "formula=" + formula + ", " +
               "value=" + value + ']';
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public Cell getMergedWith() {
        return mergedWith;
    }

    public void setMergedWith(Cell mergedWith) {
        this.mergedWith = mergedWith;
    }

    public void unMerge() {
        if (mergeStart) mergeStart = false;
        mergedWith = null;
    }

    public boolean isMergeStart() {
        return mergeStart;
    }

    public void setMergeStart(boolean mergeStart) {
        this.mergeStart = mergeStart;
    }

    public Cell copy() {
        if (formula != null) {
            return new Cell(
                xCoord,
                yCoord,
                value,
                new Formula(formula.getTxt(), xCoord, yCoord)
            );
        }
        else if (isNumber(txt)) {
            return new Cell(
                xCoord,
                yCoord,
                value
            );
        }
        else {
            return new Cell(
                xCoord,
                yCoord,
                txt
            );
        }
    }
}
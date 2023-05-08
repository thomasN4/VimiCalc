package vimicalc.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.view.CellSelector;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Objects;

import static vimicalc.utils.Conversions.isNumber;
import static vimicalc.utils.Conversions.toAlpha;

public class Cell implements Serializable {
    private int xCoord;
    private int yCoord;
    private String txt;
    private Formula formula;
    private Double value;
    private final DecimalFormat format;  // final, pour l'instant
    private Cell mergeDelimiter;
    private boolean mergeStart;
    public Color color;

    public Cell(int xCoord, int yCoord, double result, @NotNull Formula formula, Color color) {
        this(xCoord, yCoord);
        this.color = color;
        value = result;
        txt = String.valueOf(result);
        if (txt.contains("."))
            txt = format.format(result);
        if (isNumber(formula.getTxt()))
            this.formula = null;
        else this.formula = formula;
    }

    public Cell(int xCoord, int yCoord, String txt, Color color) {
        this(xCoord, yCoord);
        try {
            value = (double) Integer.parseInt(txt);
            this.txt = txt;
            this.color = color;
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

    public Cell(int xCoord, int yCoord, double value, Color color) {
        this(xCoord, yCoord);
        this.value = value;
        this.color = color;
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

    public void setXCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setYCoord(int yCoord) {
        this.yCoord = yCoord;
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
                new Formula(formula.getTxt(), xCoord, yCoord),
                color
            );
        }
        else if (isNumber(txt)) {
            c = new Cell(
                xCoord,
                yCoord,
                value,
                color
            );
        }
        else {
            c = new Cell(
                xCoord,
                yCoord,
                txt,
                color
            );
        }
        c.setMergeStart(mergeStart);
        c.mergeWith(mergeDelimiter);
        return c;
    }
    public void setColor(Color color) {
        this.color = color;
    }
}
package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Objects;

import static vimicalc.Main.isNumber;

public class Cell {
    private final int xCoord;
    private final int yCoord;
    private String txt;
    private Formula formula;
    private double value;
    private final DecimalFormat format; // final, pour l'instant, car le compiler va complain otherwise

    public Cell(int xCoord, int yCoord, String result, @NotNull Formula formula) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        format = new DecimalFormat("0.0");
        value = Double.parseDouble(result);
        txt = format.format(value);
        if (isNumber(formula.getTxt()))
            this.formula = null;
        else this.formula = formula;
    }

    public Cell(int xCoord, int yCoord, String txt) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        format = new DecimalFormat("0.0");
        try {
            value = Double.parseDouble(txt);
            this.txt = format.format(value);
        } catch (Exception ignored) {
            value = 0;
            this.txt = txt;
        }
    }

    public Cell(int xCoord, int yCoord, String txt, double value) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.txt = txt;
        this.value = value;
        format = new DecimalFormat("0.0");
    }

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        txt = "";
        value = 0;
        format = new DecimalFormat("0.0");
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

    public void setFormula(@NotNull Formula formula) {
        this.formula = formula;
        if (isNumber(formula.getTxt())) this.formula = null;
    }
}
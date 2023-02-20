package vimicalc.model;

import java.text.DecimalFormat;
import java.util.Objects;

public class Cell {
    private final int xCoord;
    private final int yCoord;
    private String txt;
    private Formula formula;
    private final DecimalFormat format;

    public Cell(int xCoord, int yCoord, String txt, Formula formula) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.txt = txt;
        this.formula = formula;
        format = new DecimalFormat("0.0");
    }

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        txt = "";
        formula = new Formula("0");
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

    public DecimalFormat format() {
        return format;
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
                "formula=" + formula.getTxt() + ']';
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    public void updateFormula(String c) {
        formula.setTxt(formula.getTxt() + c);
    }
}
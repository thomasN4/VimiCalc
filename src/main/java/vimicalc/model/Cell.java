package vimicalc.model;

import java.text.DecimalFormat;
import java.util.Objects;

public class Cell {
    protected int xCoord;
    protected int yCoord;
    protected String txt;
    protected Formula formula;
    private DecimalFormat format;
    private final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("0.00");
    private final DecimalFormat INT_FORMAT = new DecimalFormat("0");

    public Cell(int xCoord, int yCoord, String txt, Formula formula) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.txt = txt;
        this.formula = formula;
        format = DEFAULT_FORMAT;
    }

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        txt = "";
        formula = new Formula("0");
        format = null;
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
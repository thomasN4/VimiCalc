package vimicalc.model;

import java.util.ArrayList;
import java.util.Objects;

public class Cell {
    private int xCoord;
    private int yCoord;
    private String txt;
    private double val;
    private ArrayList<String> formula;

    public Cell(int xCoord, int yCoord, String txt, double val, ArrayList<String> formula) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.txt = txt;
        this.val = val;
        this.formula = formula;
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

    public double val() {
        return val;
    }

    public ArrayList<String> formula() {
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
                Double.doubleToLongBits(this.val) == Double.doubleToLongBits(that.val) &&
                Objects.equals(this.formula, that.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoord, yCoord, txt, val, formula);
    }

    @Override
    public String toString() {
        return "Cell[" +
                "xCoord=" + xCoord + ", " +
                "yCoord=" + yCoord + ", " +
                "txt=" + txt + ", " +
                "val=" + val + ", " +
                "formula=" + formula + ']';
    }

    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public void setFormula(ArrayList<String> formula) {
        this.formula = formula;
    }
};
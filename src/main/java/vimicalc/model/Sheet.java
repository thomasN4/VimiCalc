package vimicalc.model;

import java.util.ArrayList;

import static vimicalc.Main.fromAlpha;

public class Sheet {

    private ArrayList<Cell> cells;

    public Sheet() {
        setCells(new ArrayList<>());
    }

    public Sheet(ArrayList<Cell> fileContent) {
        getCells().addAll(fileContent);
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(int xCoord, int yCoord) {
        getCells().removeIf(c -> xCoord == c.xCoord() && yCoord == c.yCoord());
    }

    public double findCellVal(String coords) {
        Cell c = findCell(coords);
        if (c == null) return 0;
        else return c.val();
    }

    public Cell findCell(String coords) {
        StringBuilder coordX = new StringBuilder();
        StringBuilder coordY = new StringBuilder();
        Cell found = null;
        System.out.println("findCell's coords: "+coords);
        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) > 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }
        for (Cell c : getCells())
            if (c.xCoord() == fromAlpha(coordX.toString()) && c.yCoord() == Integer.parseInt(coordY.toString()))
                found = c;
        return found;
    }

    public void createCell(int xCoord, int yCoord, String insertedTxt) {
        double val = 0;
        try { val = Double.parseDouble(insertedTxt);
        } catch (Exception ignored) {}
        cells.add(new Cell(xCoord, yCoord, insertedTxt, val, null));
    }

    public void createCell(int xCoord, int yCoord, double val, ArrayList<String> formula) {
        cells.add(new Cell(xCoord, yCoord, String.valueOf(val), val, formula));
    }

    public void updateCellVals(ArrayList<Cell> modified) {
        for (Cell m : modified)
            getCells().removeIf(c -> m == c);
        cells.addAll(modified);
    }

    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }
}
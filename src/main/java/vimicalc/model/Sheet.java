package vimicalc.model;

import java.util.ArrayList;

import static vimicalc.Main.fromAlpha;

public class Sheet {

    public ArrayList<Cell> cells;

    public Sheet() {
        cells = new ArrayList<>();
    }
    public Sheet(ArrayList<Cell> fileContent) {
        cells.addAll(fileContent);
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(int xCoord, int yCoord) {
        cells.removeIf(c -> xCoord == c.xCoord() && yCoord == c.yCoord());
    }

    public Cell findCell(String coords) {
        StringBuilder coordX = new StringBuilder();
        StringBuilder coordY = new StringBuilder();
        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) < 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }
        Cell found = new Cell(fromAlpha(coordX.toString()), Integer.parseInt(coordY.toString()), "", 0, null);
        for (Cell c : cells)
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
        Double v = val;
        cells.add(new Cell(xCoord, yCoord, v.toString(), val, formula));
    }
}
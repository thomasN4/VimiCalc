package vimicalc.model;

import java.util.ArrayList;

import static vimicalc.Main.fromAlpha;

public class Sheet {

    public ArrayList<Cell> cells;

    public Sheet() {
        cells = new ArrayList<Cell>();
    }
    public Sheet(ArrayList<Cell> fileContent) {
        cells.addAll(fileContent);
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(int xCoord, int yCoord) {
        for (Cell c : cells)
            if (xCoord == c.xCoord() && yCoord == c.yCoord())
                cells.remove(c);
    }

    public Cell findCell(String coords) {
        String coordX = "";
        String coordY = "";
        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) < 64)
                coordX += coords.charAt(i);
            else coordY += coords.charAt(i);
        }
        Cell found = new Cell(fromAlpha(coordX), Integer.parseInt(coordY), "", 0, null);
        for (Cell c : cells)
            if (c.xCoord() == fromAlpha(coordX) && c.yCoord() == Integer.parseInt(coordY))
                found = c;
        return found;
    }
}
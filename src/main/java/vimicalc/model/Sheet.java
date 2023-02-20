package vimicalc.model;

import java.util.ArrayList;

import static vimicalc.Main.fromAlpha;

public class Sheet {

    private ArrayList<Cell> cells;

    public Sheet() {
        setCells(new ArrayList<>());
    }

//    public Sheet(ArrayList<Cell> fileContent) {
//        cells.addAll(fileContent);
//    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(String coords) {
        cells.remove(findCell(coords));
    }

    public Cell findCell(String coords) {
        StringBuilder coordX = new StringBuilder(),
                coordY = new StringBuilder();

        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) > 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }

        int xCoord, yCoord;
        try {
            xCoord = fromAlpha(coordX.toString());
            yCoord = Integer.parseInt(coordY.toString());
        } catch (Exception ignored) {
            xCoord = 0;
            yCoord = 0;
        }

        Cell found = new EmptyCell(xCoord, yCoord);

        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                found = c;

        System.out.println("Found cell: "+ found);
        return found;
    }

    public void updateCell(String coords, Cell c) {
        deleteCell(coords);
        cells.add(c);
    }

    public void updateCellVals(ArrayList<Cell> modified) {
        // might not work if I didn't do this, but haven't tested yet
        modified.forEach(m -> {
            cells.removeIf(c -> m.xCoord() == c.xCoord() && m.yCoord() == c.yCoord());
            cells.add(new Cell(m.xCoord(), m.yCoord(), m.txt(), m.formula()));
        });
    }

    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }

    // how do I not need this
    public void modifyCellFormula(int xCoord, int yCoord, String txt, Formula formula) {
        cells.removeIf(c -> xCoord == c.xCoord() && yCoord == c.yCoord());
        cells.add(new Cell(xCoord, yCoord, txt, formula));
    }

//    public void modifyCellFormula(Cell m) {
//        for (Cell c : cells)
//            if (m.xCoord() == c.xCoord() && m.yCoord() == c.yCoord())
//                cells.remove(c);
//        cells.add(m);
//    }
}
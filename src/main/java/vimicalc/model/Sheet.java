package vimicalc.model;

import java.util.ArrayList;

public class Sheet {

    public ArrayList<Cell> nonEmptyCells;
    public ArrayList<TextCell> textCells;

    public Sheet() {
        textCells = new ArrayList<TextCell>();
    }
    public Sheet(ArrayList<Cell> fileContent) {
        nonEmptyCells.addAll(fileContent);
    }

    public ArrayList<Cell> getNonEmptyCells() {
        return nonEmptyCells;
    }

    public void deleteCell(int xCoord, int yCoord) {
        for (TextCell tC : textCells)
            if (xCoord == tC.xCoord() && yCoord == tC.yCoord())
                textCells.remove(tC);
    }
}

package vimicalc.model;

import java.util.ArrayList;

public class Sheet {

    public ArrayList<Cell> nonEmptyCells;
    public ArrayList<TextCell> textCells;

    public Sheet() {}
    public Sheet(ArrayList<Cell> fileContent) {
        nonEmptyCells.addAll(fileContent);
    }

    public ArrayList<Cell> getNonEmptyCells() {
        return nonEmptyCells;
    }
}

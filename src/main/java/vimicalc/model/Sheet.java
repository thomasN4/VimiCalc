package vimicalc.model;

import java.util.ArrayList;

public class Sheet {

    private ArrayList<Cell> nonEmptyCells;

    Sheet() {}
    Sheet(ArrayList<Cell> fileContent) {
        nonEmptyCells.addAll(fileContent);
    }

    public ArrayList<Cell> getNonEmptyCells() {
        return nonEmptyCells;
    }
}

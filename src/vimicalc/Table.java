package vimicalc;

import java.util.ArrayList;

public class Table {

    ArrayList<Cell> nonEmptyCells;

    Table(ArrayList<Cell> fileContent) {
        nonEmptyCells.addAll(fileContent);
    }
}

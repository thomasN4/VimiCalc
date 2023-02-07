package vimicalc;

import java.util.ArrayList;

public class Sheet {

    ArrayList<Cell> nonEmptyCells;

    Sheet(ArrayList<Cell> fileContent) {
        nonEmptyCells.addAll(fileContent);
    }
}

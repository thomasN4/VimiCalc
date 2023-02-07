package vimicalc;

public class Table {

    final int MAX_CELLS_ROW = 2^10;
    final int MAX_CELLS_COL = 2^12;
    final int MAX_CELLS = MAX_CELLS_COL * MAX_CELLS_ROW;


    Cell[] nonEmptyCells;

    Table() {
        var nonEmptyCells = new Cell[MAX_CELLS];
    }

    Table(Cell[] fileContent) {
        var nonEmptyCells = new Cell[MAX_CELLS];
        for (int i = 0; fileContent[i] != null; i++) {
            nonEmptyCells[i] = fileContent[i];
        }
    }
}

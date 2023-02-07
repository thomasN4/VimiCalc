package vimicalc;

public class Table {

    final int MAX_CELLS_ROW = 2^10;
    final int MAX_CELLS_COL = 2^12;
    final int MAX_CELLS = MAX_CELLS_COL * MAX_CELLS_ROW;

    ArrayList<Cell> nonEmptyCells = new ArrayList<Cell>;

    Table() {
        var nonEmptyCells = new Cell[MAX_CELLS];
    }

    Table(ArrayList<Cell> fileContent) {
        var nonEmptyCells = new ArrayList<Cell>;
        for (i:
             fileContent) {
            nonEmptyCells.add(i)
        }
    }
}

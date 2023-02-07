package vicalc;

// sort of represents a whole spreadsheet file
public class Table {

    final int MAX_CELLS_ROW = 2^10;
    final int MAX_CELLS_COL = 2^12;
    final int MAX_CELLS = MAX_CELLS_COL * MAX_CELLS_ROW;

    Cell[] nonEmptyCells = new Cell[MAX_CELLS];
}

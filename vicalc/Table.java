package vicalc;

// sort of represents a whole spreadsheet file
public class Table {

    final int MAX_CELLS_ROW = 2^10;
    final int MAX_CELLS_COL = 2^12;

    Cell[][] nonEmptyCells = new Cell[MAX_CELLS_ROW][MAX_CELLS_COL];
}

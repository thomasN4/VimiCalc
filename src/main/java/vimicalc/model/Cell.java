package vimicalc.model;

abstract class Cell {

    protected int xCoord;
    protected int yCoord;

    public Cell(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }
}

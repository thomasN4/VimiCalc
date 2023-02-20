package vimicalc.model;

public class EmptyCell extends Cell {
    public EmptyCell(int xCoord, int yCoord) {
        super(xCoord, yCoord, "", new Formula("0"));
    }
}

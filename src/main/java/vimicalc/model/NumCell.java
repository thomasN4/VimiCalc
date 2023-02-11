package vimicalc.model;

public class NumCell extends Cell {

    private int value;

    public NumCell(int x_coord, int y_coord, int value) {
        super(x_coord, y_coord);
        this.value = value;
    }
}

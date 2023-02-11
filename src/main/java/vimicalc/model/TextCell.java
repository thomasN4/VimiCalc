package vimicalc.model;

public class TextCell extends Cell {

    String text;

    TextCell (int xCoord, int yCoord, String text) {
        super(xCoord, yCoord);
        this.text = text;
    }
}

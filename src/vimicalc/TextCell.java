package vimicalc;

public class TextCell extends Cell {

    String content;

    TextCell (int x_pos, int y_pos, String content) {
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.content = content;
    }
}

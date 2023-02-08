package vimicalc;

import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TextCell extends Cell {

    String text;

    // Uh je vais sort this out plus tard
    TextCell (int x_pos, int y_pos, String text) {
        super();
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.text = text;
        this.content.setText(text);
        content.setTextAlignment(TextAlignment.CENTER);
    }
}

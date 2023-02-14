package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class InfoBar extends Visible {

    private String keyStroke;
    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
    }

    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + Controller.CANVAS_W - 4, y+16);
    }
}

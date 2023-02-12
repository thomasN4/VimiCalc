package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class StatusBar extends Visible {
    private String actualMode;
    public StatusBar(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
        actualMode = "NORMAL";
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
    }
}
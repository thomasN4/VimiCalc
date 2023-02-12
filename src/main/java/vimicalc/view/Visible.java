package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

abstract class Visible {
    protected int x;
    protected int y;
    protected double w;
    protected double h;
    protected Color c;

    public Visible(int x, int y, double w, double h, Color c) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.c = c;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(c);
        gc.fillRect(x, y, w, h);
    }
}

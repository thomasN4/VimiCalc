package vimicalc.view;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

abstract class Visible {
    protected int x;
    protected int y;
    protected double w;
    protected double h;
    protected Rectangle r;
    protected StackPane s;

    public Visible(int x, int y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        r = new Rectangle(x, y, w, h);
        s = new StackPane(r);
    }

    public StackPane getS() {
        return s;
    }
}

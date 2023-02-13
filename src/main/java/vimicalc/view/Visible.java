package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

abstract class Visible {
    protected int x;
    protected int y;
    protected int w;
    protected int h;
    protected Color c;

    public Visible(int x, int y, int w, int h, Color c) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.c = c;
    }

    public void erase(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, w, h);
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(c);
        gc.fillRect(x, y, w, h);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public Color getC() {
        return c;
    }

    public void setC(Color c) {
        this.c = c;
    }
}

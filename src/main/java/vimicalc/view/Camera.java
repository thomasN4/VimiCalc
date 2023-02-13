package vimicalc.view;

import javafx.scene.paint.Color;

public class Camera {
    private int absX;
    private int absY;
    public Picture picture;

    public Camera(int x, int y, int w, int h, Color c, int absX, int absY) {
        picture = new Picture(x, y, w, h, c);
        this.absX = absX;
        this.absY = absY;
    }

    public int getAbsX() {
        return absX;
    }

    public int getAbsY() {
        return absY;
    }

//    public void xScrollTo(GraphicsContext gc, ArrayList<TextCell> textCells, int absX, int absY, int end) {
//        picture.take(gc, textCells, absX, absY);
//    }
//
//    public void yScrollTo(int end) {
//        ;
//    }

    public void updateTable_x(int x_mov) {
        absX += x_mov;
    }

    public void updateTable_y(int y_mov) {
        absY += y_mov;
    }

    public void setAbsX(int absX) {
        this.absX = absX;
    }

    public void setAbsY(int absY) {
        this.absY = absY;
    }
}

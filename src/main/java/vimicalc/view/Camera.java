package vimicalc.view;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class Camera {
    private int absX;
    private int absY;
    public Picture picture;

    public Camera(int x, int y, int w, int h, Color c, int DCW, int DCH,
                  HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets) {
        absX = DCW/2;
        absY = DCH;
        picture = new Picture(x, y, w, h, c, DCW, DCH, absX, absY, xOffsets, yOffsets);
    }

    public int getAbsX() {
        return absX;
    }

    public int getAbsY() {
        return absY;
    }

    public void updateAbsX(int x_mov) {
        absX += x_mov;
    }

    public void updateAbsY(int y_mov) {
        absY += y_mov;
    }

    public void ready() {
        picture.setIsntReady(false);
    }
}

package vimicalc.view;

import javafx.scene.paint.Color;

public class Camera {
    private int table_x;
    private int table_y;
    private Picture picture;

    public Camera(int x, int y, double w, double h, Color c, int table_x, int table_y) {
        picture = new Picture(x, y, w, h, c);
        this.table_x = table_x;
        this.table_y = table_y;
    }

    public Picture getPicture() {
        return picture;
    }
}

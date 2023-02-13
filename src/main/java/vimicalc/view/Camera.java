package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import vimicalc.model.TextCell;

import java.util.ArrayList;

public class Camera {
    private int table_x;
    private int table_y;
    public Picture picture;

    public Camera(int x, int y, int w, int h, Color c, int table_x, int table_y) {
        picture = new Picture(x, y, w, h, c);
        this.table_x = table_x;
        this.table_y = table_y;
    }

    public int getTable_x() {
        return table_x;
    }

    public int getTable_y() {
        return table_y;
    }

    public void updateTable_x(int x_mov) {
        table_x += x_mov;
    }

    public void updateTable_y(int y_mov) {
        table_y += y_mov;
    }

    public void setTable_x(int table_x) {
        this.table_x = table_x;
    }

    public void setTable_y(int table_y) {
        this.table_y = table_y;
    }
}

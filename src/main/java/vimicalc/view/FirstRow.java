package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

import static vimicalc.Main.toAlpha;

public class FirstRow extends Visible {
    public FirstRow(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
    }


    public void draw(GraphicsContext gc, int table_x, int table_y) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_W;
        for (int i = 1-table_x/jump; i < gc.getCanvas().getWidth()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+toAlpha(i), i*jump+48, 16, Controller.DEFAULT_CELL_W);
        }
    }
}
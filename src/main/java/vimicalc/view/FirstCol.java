package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class FirstCol extends Visible{
    public FirstCol(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(GraphicsContext gc, int table_y) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_H;
        for (int i = 1; i < gc.getCanvas().getHeight()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+(i+table_y/jump), 45, (i+1)*jump-5-table_y%jump, Controller.DEFAULT_CELL_W);
        }
    }
}
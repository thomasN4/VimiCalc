package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class FirstCol extends Visible{
    public FirstCol(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(GraphicsContext gc, int table_x, int table_y) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_HEIGHT;
        for (int i = 1+table_y/jump; i < gc.getCanvas().getHeight()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+i, 45, (i+1)*jump, Controller.DEFAULT_CELL_WIDTH);
        }
    }
}
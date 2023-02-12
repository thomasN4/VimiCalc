package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class FirstRow extends Visible {
    public FirstRow(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_WIDTH;
        for (int i = 0; i < gc.getCanvas().getWidth()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+i, (i+1)*jump, 0);
        }
    }
}

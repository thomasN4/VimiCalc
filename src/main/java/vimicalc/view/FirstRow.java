package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

import static vimicalc.Main.toAlpha;

public class FirstRow extends Visible {
    public FirstRow(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(GraphicsContext gc, int absX) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_W;
        for (int i = 1; i <= gc.getCanvas().getWidth()/jump+1; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+toAlpha(i+absX/jump-1), (i+1)*jump-absX%jump-45, 16, jump);
        }
    }
}
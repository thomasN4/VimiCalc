package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class FirstRow extends Visible {
    public FirstRow(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
    }

    // basically le même algorithme qu'on a appris en maths discrètes pour la conversion de bases
    public String toAlpha(int num) {
        int divRes = num / 26;
        int rem = num % 26;
        if (divRes != 0) return (char)(divRes+64) + toAlpha(rem);
        else if (num <= 26) return ""+(char)(num+64);
        else return "";
    }

    public void draw(GraphicsContext gc, int table_x, int table_y) {
        super.draw(gc);
        int jump = Controller.DEFAULT_CELL_WIDTH;
        for (int i = 1-table_x/jump; i < gc.getCanvas().getWidth()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+toAlpha(i), i*jump+48, 16, Controller.DEFAULT_CELL_WIDTH);
        }
    }
}
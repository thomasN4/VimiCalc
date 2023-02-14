package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import static vimicalc.Main.toAlpha;

public class CoordsCell extends Visible {

    private String coords;

    public CoordsCell(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void setCoords(int xCoord, int yCoord) {
        coords = toAlpha(xCoord-1) + yCoord;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(coords, 45, 16, vimicalc.controller.Controller.DEFAULT_CELL_W);
    }
}

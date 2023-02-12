package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class CoordCell extends Visible {

    private String coords;

    public CoordCell(int x, int y, double w, double h, Color c, String coords) {
        super(x, y, w, h, c);
        this.coords = coords;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(coords, 45, 16, vimicalc.controller.Controller.DEFAULT_CELL_WIDTH);
    }
}

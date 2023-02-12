package vimicalc.view;

import javafx.scene.paint.Color;

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
}

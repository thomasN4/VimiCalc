package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;

public class CoordsInfo {
    int x, y;

    private String coords;

    public CoordsInfo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCoords(int xCoord, int yCoord) {
        coords = toAlpha(xCoord-1) + yCoord;
    }

    public void setCoords(int maxXC, int minXC, int maxYC, int minYC) {
        coords = toAlpha(minXC-1) + minYC + ":" + toAlpha(maxXC-1) + maxYC;
    }

    public String getCoords() {
        return coords;
    }

    public void draw(@NotNull GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(coords, x-4, y+7);
    }
}

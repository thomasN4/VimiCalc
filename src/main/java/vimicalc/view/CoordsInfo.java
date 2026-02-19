package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;

/**
 * Displays the current cell coordinates (e.g. "B3") or a selected range
 * (e.g. "A1:C4") in the status area of the spreadsheet.
 *
 * <p>In NORMAL mode it shows a single cell reference; in VISUAL mode it
 * shows the bounding range of the selection.</p>
 */
public class CoordsInfo {
    int x, y, h;

    private String coords;

    /**
     * Creates a coordinate display at the given position.
     *
     * @param x the x pixel position (right edge)
     * @param y the y pixel position
     * @param h the height in pixels
     */
    public CoordsInfo(int x, int y, int h) {
        this.x = x;
        this.y = y;
        this.h = h;
    }

    /**
     * Sets the display to a single cell reference (e.g. "B3").
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     */
    public void setCoords(int xCoord, int yCoord) {
        coords = toAlpha(xCoord-1) + yCoord;
    }

    /**
     * Sets the display to a cell range (e.g. "A1:C4") for VISUAL mode.
     *
     * @param maxXC the maximum column in the selection
     * @param minXC the minimum column in the selection
     * @param maxYC the maximum row in the selection
     * @param minYC the minimum row in the selection
     */
    public void setCoords(int maxXC, int minXC, int maxYC, int minYC) {
        coords = toAlpha(minXC-1) + minYC + ":" + toAlpha(maxXC-1) + maxYC;
    }

    /**
     * Returns the formatted coordinate string.
     *
     * @return the coordinate string
     */
    public String getCoords() {
        return coords;
    }

    /**
     * Draws the coordinate display onto the canvas.
     *
     * @param gc the graphics context
     */
    public void draw(@NotNull GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(coords, x-4, y + (float)h/2);
    }
}

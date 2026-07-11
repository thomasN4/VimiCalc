package vimicalc.view;

import javafx.scene.control.Label;

import static vimicalc.utils.Conversions.toAlpha;

/**
 * Displays the current cell coordinates (e.g. "B3") or a selected range
 * (e.g. "A1:C4") in the status area of the spreadsheet.
 *
 * <p>In NORMAL mode it shows a single cell reference; in VISUAL mode it
 * shows the bounding range of the selection. The string field is the source
 * of truth and is mirrored to a scene-graph {@link Label}.</p>
 */
public class CoordsInfo {
    private final Label coordsLabel;
    private String coords;

    /**
     * Creates a coordinate display bound to the given label.
     *
     * @param coordsLabel the scene-graph label to update
     */
    public CoordsInfo(Label coordsLabel) {
        this.coordsLabel = coordsLabel;
        coords = "";
    }

    /**
     * Sets the display to a single cell reference (e.g. "B3").
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     */
    public void setCoords(int xCoord, int yCoord) {
        coords = toAlpha(xCoord-1) + yCoord;
        coordsLabel.setText(coords);
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
        coordsLabel.setText(coords);
    }

    /**
     * Returns the formatted coordinate string.
     *
     * @return the coordinate string
     */
    public String getCoords() {
        return coords;
    }
}

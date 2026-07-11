package vimicalc.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static vimicalc.view.Defaults.DEFAULT_ZOOM;
import static vimicalc.view.Defaults.MAX_ZOOM;
import static vimicalc.view.Defaults.MIN_ZOOM;

/**
 * Manages the pixel-level layout of the spreadsheet grid.
 *
 * <p>Computes and stores the absolute pixel positions of every column and row
 * boundary ({@code cellAbsXs} and {@code cellAbsYs}), taking into account
 * per-column/row size offsets (from {@code :resCol} and {@code :resRow} commands).
 * Also tracks which columns/rows are currently visible within the viewport.</p>
 *
 * <p>Regenerated every time the camera moves or column/row sizes change.</p>
 */
public class Positions {
    /**
     * The viewport edges passed to the last {@link #generate(int, int)} call.
     * Only used by {@link #regenerate()} to re-run layout at the same spot;
     * never a substitute for the live camera offset (see issue #30).
     */
    private int lastXInnerEdge, lastYInnerEdge;
    private int firstXC, lastXC, firstYC, lastYC, maxXC, maxYC;
    /** Viewport pixel dimensions; mutable via {@link #setViewport(int, int)} for window resizing. */
    private int picW, picH;
    private final int DCW, DCH;
    private HashMap<Integer, Integer> xOffsets;
    private HashMap<Integer, Integer> yOffsets;
    private int[] cellAbsXs, cellAbsYs;
    /**
     * The view zoom factor multiplied into every cell size during
     * {@link #generate(int, int)}. Session-only view state — never persisted.
     */
    private double zoom = DEFAULT_ZOOM;

    /**
     * Creates a new position layout manager.
     *
     * @param picW     the picture area width in pixels
     * @param picH     the picture area height in pixels
     * @param DCW      the default cell width in pixels
     * @param DCH      the default cell height in pixels
     * @param xOffsets per-column pixel offsets from default width
     * @param yOffsets per-row pixel offsets from default height
     */
    public Positions(int picW, int picH, int DCW, int DCH,
                     HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets) {
        this.xOffsets = xOffsets;
        this.yOffsets = yOffsets;
        this.picW = picW;
        this.picH = picH;
        this.DCW = DCW;
        this.DCH = DCH;
        maxXC = 0;
        maxYC = 0;
    }

    /** Scales a pixel size by the current zoom factor. */
    private int scaled(int size) {
        return (int) Math.round(size * zoom);
    }

    private int xOuterEdge(int xInnerEdge, int xC) {
        return xInnerEdge + picW + scaled(2 * DCW +
               ((xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC)) +
               ((xOffsets.get(xC + 1) == null) ? 0 : xOffsets.get(xC + 1)));
    }
    private int yOuterEdge(int yInnerEdge, int yC) {
        return yInnerEdge + picH + scaled(2 * DCH +
               ((yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC)) +
               ((yOffsets.get(yC + 1) == null) ? 0 : yOffsets.get(yC + 1)));
    }

    /**
     * Recalculates all cell boundary positions from the origin, given the
     * current camera viewport edges.
     *
     * <p>Walks column-by-column and row-by-row from position 0, accumulating
     * pixel offsets, and records which columns/rows fall within the visible
     * area bounded by {@code [xInnerEdge, xInnerEdge + picW]} and
     * {@code [yInnerEdge, yInnerEdge + picH]}.</p>
     *
     * @param xInnerEdge the left pixel boundary of the visible area
     * @param yInnerEdge the top pixel boundary of the visible area
     */
    public void generate(int xInnerEdge, int yInnerEdge) {
        System.out.println("Generating metadata...");
        ArrayList<Integer> cellAbsXsLong = new ArrayList<>(), cellAbsYsLong = new ArrayList<>();
        cellAbsXsLong.add(0); cellAbsYsLong.add(0);
        // The seeds stay unscaled by zoom: cellAbsXs[1] must equal GUTTER_W and
        // cellAbsYs[1] must equal HEADER_H so the grid stays flush against the
        // fixed-size chrome at the camera home position (Camera.scrollBy clamps
        // to those values).
        int currAbsX = DCW/2, currAbsY = DCH, xC = 1, yC = 1;
        boolean firstXCFound = false, lastXCFound = false,
                firstYCFound = false, lastYCFound = false;
        int xOffset, yOffset;

        do {
            xOffset = (xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC);
            if (!firstXCFound && currAbsX > xInnerEdge) {
                firstXC = xC - 1;
                firstXCFound = true;
            }
            cellAbsXsLong.add(currAbsX);
            // Scaling (default + offset) as one unit makes :resCol sizes zoom
            // proportionally without mutating the stored offset maps, and
            // per-increment rounding cannot accumulate drift across columns.
            currAbsX += scaled(DCW + xOffset);
            if (currAbsX > xInnerEdge + picW && !lastXCFound) {
                System.out.println(currAbsX + " " + xInnerEdge + " " + picW + " " + DCW);
                lastXC = xC;
                lastXCFound = true;
            }
            xC++;
        } while (!lastXCFound || currAbsX <= xOuterEdge(xInnerEdge, lastXC) || xC <= maxXC + 2);
        cellAbsXsLong.add(currAbsX);
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        do {
            yOffset = (yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC);
            if (!firstYCFound && currAbsY > yInnerEdge) {
                firstYC = yC - 1;
                firstYCFound = true;
            }
            cellAbsYsLong.add(currAbsY);
            currAbsY += scaled(DCH + yOffset);
            if (currAbsY > yInnerEdge + picH && !lastYCFound) {
                System.out.println(currAbsY + " " + yInnerEdge + " " + picH + " " + DCH);
                lastYC = yC;
                lastYCFound = true;
            }
            yC++;
        } while (!lastYCFound || currAbsY <= yOuterEdge(yInnerEdge, lastYC) || yC <= maxYC + 2);
        cellAbsYsLong.add(currAbsY);
        System.out.println("firstYC = " + firstYC);
        System.out.println("lastYC = " + lastYC);

        cellAbsXs = new int[xC];
        for (int i = 0; i < xC; i++)
            cellAbsXs[i] = cellAbsXsLong.get(i);
        cellAbsYs = new int[yC];
        for (int i = 0; i < yC; i++)
            cellAbsYs[i] = cellAbsYsLong.get(i);

        System.out.println("CellAbsXs: {");
        System.out.println("\t" + Arrays.toString(cellAbsXs));
        System.out.println('}');
        System.out.println("CellAbsYs: {");
        System.out.println("\t" + Arrays.toString(cellAbsYs));
        System.out.println('}');

        lastXInnerEdge = xInnerEdge;
        lastYInnerEdge = yInnerEdge;
    }

    /**
     * Re-runs {@link #generate(int, int)} at the last-generated viewport edges.
     * Used by callers without access to the camera (the model layer); safe
     * because {@link Camera#scrollBy(int, int)} regenerates on every camera
     * move, so the last edges always match the live offset.
     */
    public void regenerate() {
        generate(lastXInnerEdge, lastYInnerEdge);
    }

    /**
     * Applies a new size offset to a column or row and regenerates positions.
     *
     * @param newOffset {@code [index, pixelOffset]} — the column/row index and
     *                  its pixel offset from the default size
     * @param isXAxis   {@code true} to resize a column, {@code false} for a row
     */
    public void applyOffset(int[] newOffset, boolean isXAxis) {
        if (isXAxis)
            xOffsets.put(newOffset[0], newOffset[1]);
        else
            yOffsets.put(newOffset[0], newOffset[1]);
        regenerate();
    }

    /** @return the current view zoom factor */
    public double getZoom() {
        return zoom;
    }

    /**
     * Sets the view zoom factor, clamped to {@code [MIN_ZOOM, MAX_ZOOM]},
     * and regenerates positions at the last viewport edges.
     *
     * @param zoom the new zoom factor (1.0 = 100%)
     */
    public void setZoom(double zoom) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        regenerate();
    }

    /**
     * Sets new viewport pixel dimensions (on window resize) and regenerates
     * positions at the last viewport edges. Like {@link #setZoom(double)},
     * this is safe without camera access because {@link Camera#scrollBy(int, int)}
     * regenerates on every camera move, so the last edges always match the
     * live offset.
     *
     * @param picW the new picture area width in pixels
     * @param picH the new picture area height in pixels
     */
    public void setViewport(int picW, int picH) {
        this.picW = picW;
        this.picH = picH;
        regenerate();
    }

    /** @return the first visible column coordinate */
    public int getFirstXC() {
        return firstXC;
    }

    /** @return the last visible column coordinate */
    public int getLastXC() {
        return lastXC;
    }

    /** @return the first visible row coordinate */
    public int getFirstYC() {
        return firstYC;
    }

    /** @return the last visible row coordinate */
    public int getLastYC() {
        return lastYC;
    }

    /** @return the array of absolute x pixel positions for column boundaries */
    public int[] getCellAbsXs() {
        return cellAbsXs;
    }

    /** @return the array of absolute y pixel positions for row boundaries */
    public int[] getCellAbsYs() {
        return cellAbsYs;
    }

    /** @return the per-column pixel offset map */
    public HashMap<Integer, Integer> getxOffsets() {
        return xOffsets;
    }

    /** @return the per-row pixel offset map */
    public HashMap<Integer, Integer> getyOffsets() {
        return yOffsets;
    }

    /** @return the maximum column coordinate with data */
    public int getMaxXC() {
        return maxXC;
    }

    /** @return the maximum row coordinate with data */
    public int getMaxYC() {
        return maxYC;
    }

    /** @param maxXC the maximum column coordinate */
    public void setMaxXC(int maxXC) {
        this.maxXC = maxXC;
    }

    /** @param maxYC the maximum row coordinate */
    public void setMaxYC(int maxYC) {
        this.maxYC = maxYC;
    }

    /** @param xOffsets the per-column pixel offset map */
    public void setxOffsets(HashMap<Integer, Integer> xOffsets) {
        this.xOffsets = xOffsets;
    }

    /** @param yOffsets the per-row pixel offset map */
    public void setyOffsets(HashMap<Integer, Integer> yOffsets) {
        this.yOffsets = yOffsets;
    }
}

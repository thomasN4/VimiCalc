package vimicalc.view;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the viewport camera that tracks which portion of the spreadsheet
 * is currently visible on screen.
 *
 * <p>Maintains absolute pixel offsets ({@code absX}, {@code absY}) that determine
 * the scroll position. Owns a {@link Picture} instance responsible for rendering
 * the visible cells.</p>
 */
public class Camera {
    private int absX;
    private int absY;
    /** The picture component responsible for rendering visible cells. */
    public Picture picture;

    /**
     * Creates a camera with an associated {@link Picture} for rendering.
     *
     * @param x     the x-coordinate of the picture area
     * @param y     the y-coordinate of the picture area
     * @param w     the width of the picture area (pixels)
     * @param h     the height of the picture area (pixels)
     * @param c     the background color of the picture area
     * @param DCW   the default cell width (pixels)
     * @param DCH   the default cell height (pixels)
     * @param metadata       position metadata for cell layout
     * @param cellsFormatting per-cell formatting overrides
     */
    public Camera(int x, int y, int w, int h, Color c, int DCW, int DCH, Positions metadata,
                  HashMap<List<Integer>, Formatting> cellsFormatting) {
        absX = DCW/2;
        absY = DCH;
        picture = new Picture(x, y, w, h, c, DCW, DCH, metadata, cellsFormatting);
    }

    /**
     * Returns the horizontal scroll offset in pixels.
     *
     * @return the horizontal scroll offset
     */
    public int getAbsX() {
        return absX;
    }

    /**
     * Returns the vertical scroll offset in pixels.
     *
     * @return the vertical scroll offset
     */
    public int getAbsY() {
        return absY;
    }

    /**
     * Adjusts the horizontal scroll offset by the given delta.
     *
     * @param x_mov the horizontal pixel delta
     */
    public void updateAbsX(int x_mov) {
        absX += x_mov;
    }

    /**
     * Adjusts the vertical scroll offset by the given delta.
     *
     * @param y_mov the vertical pixel delta
     */
    public void updateAbsY(int y_mov) {
        absY += y_mov;
    }

    /** Marks the picture as ready for a lightweight re-render (no full snapshot). */
    public void ready() {
        picture.setIsntReady(false);
    }
}

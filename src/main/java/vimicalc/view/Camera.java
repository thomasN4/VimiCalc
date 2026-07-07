package vimicalc.view;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;

import static vimicalc.view.Defaults.GUTTER_W;
import static vimicalc.view.Defaults.HEADER_H;

/**
 * Represents the viewport camera that tracks which portion of the spreadsheet
 * is currently visible on screen.
 *
 * <p>Maintains absolute pixel offsets ({@code absX}, {@code absY}) that determine
 * the scroll position. These offsets are the single source of truth for the
 * viewport: every renderer (cells, headers, cursor) derives its screen positions
 * from them. Owns a {@link Picture} instance responsible for rendering the
 * visible cells.</p>
 */
public class Camera {
    private int absX;
    private int absY;
    private final Positions positions;
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
     * @param metadata       position metadata for cell layout
     * @param cellsFormatting per-cell formatting overrides
     */
    public Camera(int x, int y, int w, int h, Color c, Positions metadata,
                  HashMap<List<Integer>, Formatting> cellsFormatting) {
        absX = GUTTER_W;
        absY = HEADER_H;
        positions = metadata;
        picture = new Picture(x, y, w, h, c, metadata, cellsFormatting);
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
     * Scrolls the viewport by the given pixel deltas and regenerates the cell
     * layout metadata. This is the only way to move the camera, so layout can
     * never be observed out of sync with the scroll offset (issue #30).
     *
     * <p>The offsets are clamped to the home position ({@code GUTTER_W},
     * {@code HEADER_H}) so the sheet can never scroll past its top-left
     * corner.</p>
     *
     * @param dx the horizontal pixel delta
     * @param dy the vertical pixel delta
     */
    public void scrollBy(int dx, int dy) {
        absX = Math.max(GUTTER_W, absX + dx);
        absY = Math.max(HEADER_H, absY + dy);
        positions.generate(absX, absY);
    }

    /** Marks the picture as ready for a lightweight re-render (no full snapshot). */
    public void ready() {
        picture.setIsntReady(false);
    }
}

package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Default visual constants shared across the application.
 *
 * <p>Centralizes the default cell dimensions, colors, and text alignment
 * so that model classes do not need to depend on {@code Controller}.</p>
 */
public final class Defaults {
    private Defaults() {} // utility class

    /** Default cell height in pixels. */
    public static final int DEFAULT_CELL_H = 24;
    /** Default cell width in pixels (4x the height; halved for the first-column gutter in layout). */
    public static final int DEFAULT_CELL_W = DEFAULT_CELL_H * 4;
    /** Default cell background color. */
    public static final Color DEFAULT_CELL_C = Color.WHITE;
    /** Default cell text color. */
    public static final Color DEFAULT_TXT_C = Color.BLACK;
    /** Default vertical text position within a cell. */
    public static final VPos DEFAULT_VPOS = VPos.CENTER;
    /** Default horizontal text alignment within a cell. */
    public static final TextAlignment DEFAULT_ALIGNMENT = TextAlignment.CENTER;
}

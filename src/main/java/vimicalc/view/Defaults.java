package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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
    /** Width in pixels of the row-number gutter ({@link FirstCol}) on the left edge. */
    public static final int GUTTER_W = DEFAULT_CELL_W / 2;
    /** Height in pixels of the column-letter header row ({@link FirstRow}) on the top edge. */
    public static final int HEADER_H = DEFAULT_CELL_H;
    /** Default font size in points (matches the JavaFX canvas default font). */
    public static final int DEFAULT_FONT_SIZE = 13;
    /** Default view zoom factor (100%). */
    public static final double DEFAULT_ZOOM = 1.0;
    /** Minimum view zoom factor (25%). */
    public static final double MIN_ZOOM = 0.25;
    /** Maximum view zoom factor (400%). */
    public static final double MAX_ZOOM = 4.0;
    /** Multiplicative step applied per zoom-in/zoom-out key press. */
    public static final double ZOOM_STEP = 1.1;
    /** Default delay between replayed macro keystrokes, in milliseconds (0 = instant). */
    public static final int DEFAULT_MACRO_DELAY_MS = 0;
    /** Maximum delay accepted by {@code :macroDelay}, in milliseconds. */
    public static final int MAX_MACRO_DELAY_MS = 10_000;
    /** Default cell gridline visibility. */
    public static final boolean DEFAULT_GRIDLINES = true;
    /** Default cell background color. */
    public static final Color DEFAULT_CELL_C = Color.WHITE;
    /** Default cell text color. */
    public static final Color DEFAULT_TXT_C = Color.BLACK;
    /** Lightest chrome shade; top stop of the header gradients (mirrors {@code theme.css}). */
    public static final Color CHROME_TOP = Color.web("#f4f6f8");
    /** Darkest chrome shade; bottom stop of the header gradients (mirrors {@code theme.css}). */
    public static final Color CHROME_BOTTOM = Color.web("#dfe3e8");
    /** Hairline border color separating chrome (headers, bars) from the grid. */
    public static final Color CHROME_BORDER = Color.web("#a9b1ba");
    /** Hairline separator color between labels inside the headers. */
    public static final Color HEADER_SEPARATOR = Color.web("#c3cad2");
    /** Header label text color. */
    public static final Color HEADER_TXT = Color.web("#333333");
    /** Cell gridline color. */
    public static final Color GRIDLINE_C = Color.web("#d8dde3");
    /** Accent color for the selector border and other emphasis. */
    public static final Color ACCENT = Color.rgb(0, 171, 255);
    /**
     * Selection fill tint for the cell selector and VISUAL-mode ranges. Opaque
     * on purpose: the selector repaints the cell text itself, so a translucent
     * fill over the grid's already-drawn text would double-render it.
     */
    public static final Color SELECT_TINT = Color.web("#cfe9ff");
    /** Top-lit vertical gradient filling the column header row and corner block. */
    public static final LinearGradient HEADER_FILL_V = new LinearGradient(
        0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
        new Stop(0, CHROME_TOP), new Stop(1, CHROME_BOTTOM));
    /** Left-lit horizontal gradient filling the row-number gutter. */
    public static final LinearGradient HEADER_FILL_H = new LinearGradient(
        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0, CHROME_TOP), new Stop(1, CHROME_BOTTOM));
    /** Default vertical text position within a cell. */
    public static final VPos DEFAULT_VPOS = VPos.CENTER;
    /** Default horizontal text alignment within a cell. */
    public static final TextAlignment DEFAULT_ALIGNMENT = TextAlignment.CENTER;
}

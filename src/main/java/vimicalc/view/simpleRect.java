package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all rectangular UI components drawn on the canvas.
 *
 * <p>Provides position ({@code x}, {@code y}), dimensions ({@code w}, {@code h}),
 * and background color ({@code c}). Subclasses override {@link #draw(GraphicsContext)}
 * to render their specific content on top of the filled rectangle.</p>
 */
public abstract class simpleRect {
    /** The x-coordinate of the top-left corner in pixels. */
    protected int x;
    /** The y-coordinate of the top-left corner in pixels. */
    protected int y;
    /** The width in pixels. */
    protected int w;
    /** The height in pixels. */
    protected int h;
    /** The fill color. */
    protected Color c;

    /**
     * Creates a new rectangle at the given position with the given size and color.
     *
     * @param x the x-coordinate of the top-left corner (pixels)
     * @param y the y-coordinate of the top-left corner (pixels)
     * @param w the width (pixels)
     * @param h the height (pixels)
     * @param c the fill color
     */
    public simpleRect(int x, int y, int w, int h, Color c) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.c = c;
    }

    /**
     * Fills this rectangle on the canvas with its background color.
     * Subclasses should call {@code super.draw(gc)} before rendering their content.
     *
     * @param gc the graphics context to draw on
     */
    public void draw(@NotNull GraphicsContext gc) {
        gc.setFill(c);
        gc.fillRect(x, y, w, h);
    }

    /** @return the x-coordinate */
    public int getX() {
        return x;
    }

    /** @param x the new x-coordinate */
    public void setX(int x) {
        this.x = x;
    }

    /** @return the y-coordinate */
    public int getY() {
        return y;
    }

    /** @return the width */
    public int getW() {
        return w;
    }

    /** @return the height */
    public int getH() {
        return h;
    }

    /** @return the fill color */
    public Color getC() {
        return c;
    }

    /** @param c the new fill color */
    public void setC(Color c) {
        this.c = c;
    }

    /** @param w the new width */
    public void setW(int w) {
        this.w = w;
    }

    /** @param h the new height */
    public void setH(int h) {
        this.h = h;
    }
}

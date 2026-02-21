package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;

import java.util.function.Supplier;

/**
 * The status bar displayed near the bottom of the window.
 *
 * <p>Shows the current editing {@link vimicalc.controller.Mode} and the
 * name of the open file (or "new_file" if unsaved).</p>
 */
public class StatusBar extends simpleRect {
    private String filename;
    private final Supplier<Mode> modeSupplier;

    /**
     * Creates the status bar.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the background color
     * @param modeSupplier supplies the current editing mode
     */
    public StatusBar(int x, int y, int w, int h, Color c, Supplier<Mode> modeSupplier) {
        super(x, y, w, h, c);
        this.modeSupplier = modeSupplier;
        filename = "new_file";
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLUE);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" [" + modeSupplier.get() + "]  --" + filename + "--", 2, y + (float)h/2);
    }

    /** @param filename the name of the open file */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
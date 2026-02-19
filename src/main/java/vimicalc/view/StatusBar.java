package vimicalc.view;

import static vimicalc.controller.Controller.currMode;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

/**
 * The status bar displayed near the bottom of the window.
 *
 * <p>Shows the current editing {@link vimicalc.controller.Mode} and the
 * name of the open file (or "new_file" if unsaved).</p>
 */
public class StatusBar extends simpleRect {
    private String filename;

    /**
     * Creates the status bar.
     *
     * @param x the x pixel position
     * @param y the y pixel position
     * @param w the width in pixels
     * @param h the height in pixels
     * @param c the background color
     */
    public StatusBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        filename = "new_file";
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLUE);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" [" + currMode + "]  --" + filename + "--", 2, y + (float)h/2);
    }

    /** @param filename the name of the open file */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
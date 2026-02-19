package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

/**
 * A small UI cell in the top-left corner that displays the name of the
 * last key pressed.
 *
 * <p>Provides visual feedback so the user can see which key event was
 * registered, useful when building multi-key commands.</p>
 */
public class KeyStrokeCell extends simpleRect {
    private String keyStroke;
    /**
     * Creates a keystroke display cell.
     *
     * @param x the x pixel position
     * @param y the y pixel position
     * @param w the width in pixels
     * @param h the height in pixels
     * @param c the background color
     */
    public KeyStrokeCell(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
    }

    /**
     * Sets the key stroke text to display.
     *
     * @param keyStroke the key name
     */
    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLUE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(keyStroke, (float) w/2, (float) h/2, w);
    }
}

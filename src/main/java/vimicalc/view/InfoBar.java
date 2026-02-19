package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The information bar at the very bottom of the window.
 *
 * <p>Displays contextual information depending on the current mode:</p>
 * <ul>
 *   <li><b>NORMAL</b> — the selected cell's value and formula expression</li>
 *   <li><b>COMMAND</b> — the command being typed, prefixed with {@code :}</li>
 *   <li><b>FORMULA</b> — the formula being entered, prefixed with {@code %}</li>
 *   <li><b>VISUAL</b> — can also show a command being entered for the selection</li>
 * </ul>
 *
 * <p>The static field {@link #iBarExpr} holds the current key-command expression
 * displayed on the right side of the bar.</p>
 */
public class InfoBar extends simpleRect {

    /** The current key-command expression, displayed right-aligned in the info bar. */
    public static String iBarExpr;
    private String infobarTxt;
    private boolean enteringCommandInVISUAL;

    /**
     * Creates the info bar at the given position.
     *
     * @param x the x pixel position
     * @param y the y pixel position
     * @param w the width in pixels
     * @param h the height in pixels
     * @param c the background color
     */
    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        iBarExpr = "";
    }

    /**
     * Returns whether a command is being entered in VISUAL mode.
     *
     * @return {@code true} if entering a command in VISUAL mode
     */
    public boolean isEnteringCommandInVISUAL() {
        return enteringCommandInVISUAL;
    }

    /**
     * Sets whether a command is being entered in VISUAL mode.
     *
     * @param enteringCommandInVISUAL the flag
     */
    public void setEnteringCommandInVISUAL(boolean enteringCommandInVISUAL) {
        this.enteringCommandInVISUAL = enteringCommandInVISUAL;
    }

    /**
     * Sets the info bar text for COMMAND mode, prefixing with {@code :}.
     *
     * @param infobarTxt the command text to display
     */
    public void setCommandTxt(String infobarTxt) {
        this.infobarTxt = ":" + infobarTxt;
    }

    /**
     * Sets the info bar text for FORMULA mode, prefixing with {@code %} and stripping ".0" suffixes.
     *
     * @param infobarTxt the formula text to display
     */
    public void setEnteringFormula(String infobarTxt) {
        this.infobarTxt = "% " + infobarTxt.replace(".0" , "");
    }

    /**
     * Sets the info bar display text.
     *
     * @param infobarTxt the text to display, or {@code null} for the default "(=I)"
     */
    public void setInfobarTxt(String infobarTxt) {
        this.infobarTxt = Objects.requireNonNullElse(infobarTxt, "(=I)");
    }

    /**
     * Returns the current info bar text.
     *
     * @return the info bar text
     */
    public String getInfobarTxt() {
        return infobarTxt;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(iBarExpr, x + w - 4, y + (float)h/2);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(infobarTxt,2, y + (float)h/2);
    }
}
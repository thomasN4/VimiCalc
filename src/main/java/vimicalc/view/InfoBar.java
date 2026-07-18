package vimicalc.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;

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
 * <p>The left side is a single {@code Text} node holding the whole line, with
 * two unmanaged overlays on top: a block-caret {@code Region} and a
 * {@code Text} that repaints the character under the block in inverse video.
 * COMMAND mode shows the blinking block over the character at the caret
 * position ({@link #setCommandTxt(String, int)}), Vim-style; at the end of
 * the line the block sits on a blank one-character cell. Because the caret
 * is pure overlay, moving it changes nothing in the layout — the line's text
 * node is untouched, so the letters cannot shift as the caret passes over
 * them. Every non-command display path (formula echo, errors, NORMAL info)
 * hides both overlays, so the caret can never linger over an error
 * message.</p>
 *
 * <p>The field {@link #iBarExpr} holds the current key-command expression
 * displayed on the right side of the bar. Setters update the nodes
 * immediately.</p>
 */
public class InfoBar {

    /** Blink half-period: caret visibility toggles at this interval. */
    private static final Duration BLINK_INTERVAL = Duration.millis(530);
    /**
     * Probe string for the block's vertical extent: {@code l} for the tallest
     * lowercase ascender, {@code g} for the descender. The block spans
     * exactly this glyph range so it never pokes above tall letters or stops
     * short of descenders.
     */
    private static final String EXTENT_PROBE = "lg";

    /** The whole line of text currently displayed on the left side. */
    private final Text infoText;
    /** The block caret, visible and blinking only while a command is edited. */
    private final Region infoCaret;
    /**
     * Overlay repainting the character under the block in inverse video
     * while the blink phase is on; empty at end of line.
     */
    private final Text infoCaretChar;
    private final Label exprLabel;

    /**
     * Toggles the caret's visibility while a command is being edited.
     * Created lazily so the class stays constructible without the JavaFX
     * toolkit (headless unit tests stub the nodes with {@code null}).
     */
    private Timeline blink;

    /** The current key-command expression, displayed right-aligned in the info bar. */
    private String iBarExpr;
    private String infobarTxt;
    /** The caret's display index into {@link #infobarTxt} while showing. */
    private int caretAt;
    /** Whether the caret is logically shown (blinking may have it invisible). */
    private boolean caretShowing;
    /** Reusable probe for measuring text extents in the info font. */
    private Text measureProbe;
    /** Cached advance width of one character of the (monospace) info font. */
    private double charWidth = -1;
    /** Cached block top relative to the text baseline (top of {@code l}, negative). */
    private double blockMinY;
    /** Cached block bottom relative to the text baseline (descender bottom). */
    private double blockMaxY;

    /**
     * Creates the info bar bound to the given nodes.
     *
     * @param infoText      the left-side text line
     * @param infoCaret     the block-caret overlay
     * @param infoCaretChar the inverse-video overlay for the caret character
     * @param exprLabel     the right-side key-command expression label
     */
    public InfoBar(Text infoText, Region infoCaret, Text infoCaretChar,
                   Label exprLabel) {
        this.infoText = infoText;
        this.infoCaret = infoCaret;
        this.infoCaretChar = infoCaretChar;
        this.exprLabel = exprLabel;
        iBarExpr = "";
        infobarTxt = "(=I)";
        // A text change can transiently reflow the line (e.g. wrap to two
        // lines until the TextFlow is resized by the next layout pass), so
        // the caret is re-placed whenever the text node's bounds settle
        // rather than only at the moment the text is set.
        if (infoText != null && infoCaret != null)
            infoText.boundsInParentProperty().addListener(
                (obs, oldBounds, newBounds) -> { if (caretShowing) placeCaret(); });
    }

    /**
     * Returns the current key-command expression displayed in the info bar.
     *
     * @return the key-command expression string
     */
    public String getIBarExpr() {
        return iBarExpr;
    }

    /**
     * Sets the key-command expression displayed right-aligned in the info bar.
     *
     * @param iBarExpr the expression string to display
     */
    public void setIBarExpr(String iBarExpr) {
        this.iBarExpr = iBarExpr;
        exprLabel.setText(iBarExpr);
    }

    /**
     * Sets the info bar text for COMMAND mode with the caret at the end,
     * prefixing with {@code :}.
     *
     * @param infobarTxt the command text to display
     */
    public void setCommandTxt(String infobarTxt) {
        setCommandTxt(infobarTxt, infobarTxt.length());
    }

    /**
     * Sets the info bar text for COMMAND mode, prefixing with {@code :} and
     * showing a blinking block caret over the character at the given
     * position within the command text (a blank cell at the end).
     *
     * @param infobarTxt the command text to display
     * @param caret      the caret index, in {@code 0..infobarTxt.length()}
     */
    public void setCommandTxt(String infobarTxt, int caret) {
        this.infobarTxt = ":" + infobarTxt;
        // The ':' prefix occupies index 0 of the displayed string, so the
        // caret's display position is shifted one to the right.
        caretAt = caret + 1;
        infoText.setText(this.infobarTxt);
        infoCaretChar.setText(caretAt < this.infobarTxt.length()
            ? this.infobarTxt.substring(caretAt, caretAt + 1) : "");
        showCaret();
    }

    /**
     * Sets the info bar text for FORMULA mode, prefixing with {@code %} and stripping ".0" suffixes.
     *
     * @param infobarTxt the formula text to display
     */
    public void setEnteringFormula(String infobarTxt) {
        this.infobarTxt = "% " + infobarTxt.replace(".0" , "");
        displayWithoutCaret();
    }

    /**
     * Sets the info bar display text.
     *
     * @param infobarTxt the text to display, or {@code null} for the default "(=I)"
     */
    public void setInfobarTxt(String infobarTxt) {
        this.infobarTxt = Objects.requireNonNullElse(infobarTxt, "(=I)");
        displayWithoutCaret();
    }

    /**
     * Returns the current info bar text.
     *
     * @return the info bar text
     */
    public String getInfobarTxt() {
        return infobarTxt;
    }

    /** Shows the current text with both caret overlays hidden. */
    private void displayWithoutCaret() {
        // Hide first: setting the text fires the bounds listener, which must
        // not re-place the caret against the already-swapped text.
        hideCaret();
        infoText.setText(infobarTxt);
        infoCaretChar.setText("");
    }

    /**
     * Places the block caret over the caret character's cell, and the
     * inverse-video overlay on the same spot. Horizontally the block starts
     * where the text before the caret ends (measured with a probe — the
     * displayed text node itself is never split, so placing the caret cannot
     * move a single letter); vertically it spans from the top of an {@code l}
     * to the bottom of a descender, anchored on the text's baseline.
     */
    private void placeCaret() {
        measureExtents();
        Bounds b = infoText.getBoundsInParent();
        double baselineY = b.getMinY() + infoText.getBaselineOffset();
        // The bounds listener can fire while the text is mid-swap; clamp so
        // a transiently shorter string can't put the prefix out of range.
        int at = Math.min(caretAt, infobarTxt.length());
        double x = b.getMinX() + measureWidth(infobarTxt.substring(0, at));
        double w = infoCaretChar.getText().isEmpty()
            ? charWidth : measureWidth(infoCaretChar.getText());
        infoCaret.resizeRelocate(x, baselineY + blockMinY, w, blockMaxY - blockMinY);
        infoCaretChar.relocate(x, baselineY - infoCaretChar.getBaselineOffset());
    }

    /**
     * Caches the info font's metrics on first use (and again if the font
     * changed, e.g. once CSS is first applied): one character's advance
     * width and the block's vertical extent around the baseline.
     */
    private void measureExtents() {
        if (measureProbe != null && measureProbe.getFont().equals(infoText.getFont()))
            return;
        measureProbe = new Text();
        measureProbe.setFont(infoText.getFont());
        charWidth = measureWidth("0");
        measureProbe.setBoundsType(TextBoundsType.VISUAL);
        measureProbe.setText(EXTENT_PROBE);
        // Visual bounds are glyph-tight and baseline-relative: minY is the
        // top of the 'l', maxY the bottom of the 'g'.
        blockMinY = measureProbe.getLayoutBounds().getMinY();
        blockMaxY = measureProbe.getLayoutBounds().getMaxY();
        measureProbe.setBoundsType(TextBoundsType.LOGICAL);
    }

    /**
     * Returns the width of the given string in the info font.
     *
     * @param s the string to measure
     * @return the advance width in pixels
     */
    private double measureWidth(String s) {
        measureProbe.setText(s);
        return measureProbe.getLayoutBounds().getWidth();
    }

    /**
     * Sets the caret's blink phase: the block's visibility plus the
     * inverse-video overlay, which must track the block exactly.
     */
    private void setCaretOn(boolean on) {
        infoCaret.setVisible(on);
        infoCaretChar.setVisible(on && !infoCaretChar.getText().isEmpty());
    }

    /** Shows the caret and (re)starts its blink cycle from visible. */
    private void showCaret() {
        caretShowing = true;
        placeCaret();
        setCaretOn(true);
        if (blink == null) {
            blink = new Timeline(new KeyFrame(BLINK_INTERVAL,
                e -> setCaretOn(!infoCaret.isVisible())));
            blink.setCycleCount(Animation.INDEFINITE);
        }
        // Restart so the caret is solidly visible right after each keystroke,
        // the way most editors blink.
        blink.playFromStart();
    }

    /** Hides the caret and stops the blink cycle. */
    private void hideCaret() {
        caretShowing = false;
        if (blink != null) blink.stop();
        setCaretOn(false);
    }
}

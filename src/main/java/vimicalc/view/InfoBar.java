package vimicalc.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
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
 * <p>The left side is a {@code TextFlow} of four nodes: text before the
 * caret, a block-caret {@code Region}, the single character under the caret,
 * and the text after it. COMMAND mode shows a blinking block caret over the
 * character at the caret position ({@link #setCommandTxt(String, int)}),
 * Vim-style — the character renders inverted while the blink is on, and at
 * the end of the line the block sits on a blank one-character cell. Every
 * non-command display path (formula echo, errors, NORMAL info) collapses to
 * the "before" node and hides the caret, so the caret can never linger over
 * an error message.</p>
 *
 * <p>The field {@link #iBarExpr} holds the current key-command expression
 * displayed on the right side of the bar. Setters update the nodes
 * immediately.</p>
 */
public class InfoBar {

    /** Blink half-period: caret visibility toggles at this interval. */
    private static final Duration BLINK_INTERVAL = Duration.millis(530);
    /** Style class inverting the character under the block caret (theme.css). */
    private static final String CARET_CHAR_CLASS = "info-caret-char";

    /** Text before the caret (or the whole text when the caret is hidden). */
    private final Text infoTextBefore;
    /** The block caret, visible and blinking only while a command is edited. */
    private final Region infoCaret;
    /** The single character under the block caret; empty at end of line. */
    private final Text infoTextAt;
    /** Text after the caret character; empty when the caret is hidden. */
    private final Text infoTextAfter;
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
    /** Whether the caret is logically shown (blinking may have it invisible). */
    private boolean caretShowing;
    /** Cached advance width of one character of the (monospace) info font. */
    private double charWidth = -1;

    /**
     * Creates the info bar bound to the given nodes.
     *
     * @param infoTextBefore the left-side text up to the caret
     * @param infoCaret      the block-caret node
     * @param infoTextAt     the character under the caret
     * @param infoTextAfter  the left-side text after the caret character
     * @param exprLabel      the right-side key-command expression label
     */
    public InfoBar(Text infoTextBefore, Region infoCaret, Text infoTextAt,
                   Text infoTextAfter, Label exprLabel) {
        this.infoTextBefore = infoTextBefore;
        this.infoCaret = infoCaret;
        this.infoTextAt = infoTextAt;
        this.infoTextAfter = infoTextAfter;
        this.exprLabel = exprLabel;
        iBarExpr = "";
        infobarTxt = "(=I)";
        // A text change can transiently reflow the before-run (e.g. wrap to
        // two lines until the TextFlow is resized by the next layout pass),
        // so the caret is re-placed whenever the run's bounds settle rather
        // than only at the moment the text is set.
        if (infoTextBefore != null && infoCaret != null)
            infoTextBefore.boundsInParentProperty().addListener(
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
        int at = caret + 1;
        infoTextBefore.setText(this.infobarTxt.substring(0, at));
        infoTextAt.setText(at < this.infobarTxt.length()
            ? this.infobarTxt.substring(at, at + 1) : "");
        infoTextAfter.setText(at < this.infobarTxt.length()
            ? this.infobarTxt.substring(at + 1) : "");
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

    /** Puts the whole current text into the "before" node and hides the caret. */
    private void displayWithoutCaret() {
        infoTextBefore.setText(infobarTxt);
        infoTextAt.setText("");
        infoTextAfter.setText("");
        hideCaret();
    }

    /**
     * Places the block caret over the caret character's cell: one character
     * advance wide, spanning the before-run's line box, right at the seam.
     * The caret is an unmanaged child of the TextFlow, so it takes no part
     * in layout and can never shift the text around it; it paints beneath
     * the caret character, which inverts its fill while the blink is on.
     */
    private void placeCaret() {
        Bounds b = infoTextBefore.getBoundsInParent();
        double w = infoTextAt.getText().isEmpty()
            ? charWidth() : infoTextAt.getBoundsInParent().getWidth();
        infoCaret.resizeRelocate(b.getMaxX(), b.getMinY(), w, b.getHeight());
    }

    /**
     * Returns the advance width of one character of the info font, measured
     * once off-scene — the font is monospace (theme.css), so this is the
     * block caret's width on an end-of-line blank cell.
     */
    private double charWidth() {
        if (charWidth < 0) {
            Text probe = new Text("0");
            probe.setFont(infoTextBefore.getFont());
            charWidth = probe.getLayoutBounds().getWidth();
        }
        return charWidth;
    }

    /**
     * Sets the caret's blink phase: the block's visibility plus the inverted
     * fill on the character under it, which must track the block exactly.
     */
    private void setCaretOn(boolean on) {
        infoCaret.setVisible(on);
        if (on) {
            if (!infoTextAt.getStyleClass().contains(CARET_CHAR_CLASS))
                infoTextAt.getStyleClass().add(CARET_CHAR_CLASS);
        } else {
            infoTextAt.getStyleClass().remove(CARET_CHAR_CLASS);
        }
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

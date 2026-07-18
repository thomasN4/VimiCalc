package vimicalc.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
 * <p>The left side is a {@code TextFlow} of three nodes — text before the
 * caret, a thin caret {@code Region}, and text after the caret — so COMMAND
 * mode can show a blinking insertion caret at any position
 * ({@link #setCommandTxt(String, int)}). Every non-command display path
 * (formula echo, errors, NORMAL info) collapses to the "before" node and
 * hides the caret, so the caret can never linger over an error message.</p>
 *
 * <p>The field {@link #iBarExpr} holds the current key-command expression
 * displayed on the right side of the bar. Setters update the nodes
 * immediately.</p>
 */
public class InfoBar {

    /** Blink half-period: caret visibility toggles at this interval. */
    private static final Duration BLINK_INTERVAL = Duration.millis(530);

    /** Text before the caret (or the whole text when the caret is hidden). */
    private final Text infoTextBefore;
    /** The caret node, visible and blinking only while a command is edited. */
    private final Region infoCaret;
    /** Text after the caret; empty when the caret is hidden. */
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

    /**
     * Creates the info bar bound to the given nodes.
     *
     * @param infoTextBefore the left-side text up to the caret
     * @param infoCaret      the caret node between the two text runs
     * @param infoTextAfter  the left-side text after the caret
     * @param exprLabel      the right-side key-command expression label
     */
    public InfoBar(Text infoTextBefore, Region infoCaret, Text infoTextAfter,
                   Label exprLabel) {
        this.infoTextBefore = infoTextBefore;
        this.infoCaret = infoCaret;
        this.infoTextAfter = infoTextAfter;
        this.exprLabel = exprLabel;
        iBarExpr = "";
        infobarTxt = "(=I)";
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
     * showing a blinking caret at the given position within the command text.
     *
     * @param infobarTxt the command text to display
     * @param caret      the caret index, in {@code 0..infobarTxt.length()}
     */
    public void setCommandTxt(String infobarTxt, int caret) {
        this.infobarTxt = ":" + infobarTxt;
        // The ':' prefix occupies index 0 of the displayed string, so the
        // caret's display position is shifted one to the right.
        infoTextBefore.setText(this.infobarTxt.substring(0, caret + 1));
        infoTextAfter.setText(this.infobarTxt.substring(caret + 1));
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
        infoTextAfter.setText("");
        hideCaret();
    }

    /** Shows the caret node and (re)starts its blink cycle from visible. */
    private void showCaret() {
        infoCaret.setManaged(true);
        infoCaret.setVisible(true);
        if (blink == null) {
            blink = new Timeline(new KeyFrame(BLINK_INTERVAL,
                e -> infoCaret.setVisible(!infoCaret.isVisible())));
            blink.setCycleCount(Animation.INDEFINITE);
        }
        // Restart so the caret is solidly visible right after each keystroke,
        // the way most editors blink.
        blink.playFromStart();
    }

    /** Hides the caret node and stops the blink cycle. */
    private void hideCaret() {
        if (blink != null) blink.stop();
        infoCaret.setVisible(false);
        infoCaret.setManaged(false);
    }
}

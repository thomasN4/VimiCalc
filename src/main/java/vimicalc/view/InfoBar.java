package vimicalc.view;

import javafx.scene.control.Label;

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
 * <p>The field {@link #iBarExpr} holds the current key-command expression
 * displayed on the right side of the bar. Both sides are scene-graph labels;
 * setters update the labels immediately.</p>
 */
public class InfoBar {

    private final Label infoLabel;
    private final Label exprLabel;

    /** The current key-command expression, displayed right-aligned in the info bar. */
    private String iBarExpr;
    private String infobarTxt;

    /**
     * Creates the info bar bound to the given labels.
     *
     * @param infoLabel the left-side contextual text label
     * @param exprLabel the right-side key-command expression label
     */
    public InfoBar(Label infoLabel, Label exprLabel) {
        this.infoLabel = infoLabel;
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
     * Sets the info bar text for COMMAND mode, prefixing with {@code :}.
     *
     * @param infobarTxt the command text to display
     */
    public void setCommandTxt(String infobarTxt) {
        this.infobarTxt = ":" + infobarTxt;
        infoLabel.setText(this.infobarTxt);
    }

    /**
     * Sets the info bar text for FORMULA mode, prefixing with {@code %} and stripping ".0" suffixes.
     *
     * @param infobarTxt the formula text to display
     */
    public void setEnteringFormula(String infobarTxt) {
        this.infobarTxt = "% " + infobarTxt.replace(".0" , "");
        infoLabel.setText(this.infobarTxt);
    }

    /**
     * Sets the info bar display text.
     *
     * @param infobarTxt the text to display, or {@code null} for the default "(=I)"
     */
    public void setInfobarTxt(String infobarTxt) {
        this.infobarTxt = Objects.requireNonNullElse(infobarTxt, "(=I)");
        infoLabel.setText(this.infobarTxt);
    }

    /**
     * Returns the current info bar text.
     *
     * @return the info bar text
     */
    public String getInfobarTxt() {
        return infobarTxt;
    }
}

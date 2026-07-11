package vimicalc.view;

import javafx.scene.control.Label;

/**
 * A small UI cell in the top-left corner that displays the name of the
 * last key pressed.
 *
 * <p>Provides visual feedback so the user can see which key event was
 * registered, useful when building multi-key commands. Backed by a
 * scene-graph {@link Label}; {@link #setKeyStroke(String)} updates the
 * label immediately.</p>
 */
public class KeyStrokeCell {
    private final Label keyStrokeLabel;

    /**
     * Creates a keystroke display bound to the given label.
     *
     * @param keyStrokeLabel the scene-graph label to update
     */
    public KeyStrokeCell(Label keyStrokeLabel) {
        this.keyStrokeLabel = keyStrokeLabel;
    }

    /**
     * Sets the key stroke text to display. The chip is hidden while the text
     * is empty so it only floats over the header corner once a key is pressed.
     *
     * @param keyStroke the key name
     */
    public void setKeyStroke(String keyStroke) {
        keyStrokeLabel.setText(keyStroke);
        keyStrokeLabel.setVisible(keyStroke != null && !keyStroke.isEmpty());
    }
}

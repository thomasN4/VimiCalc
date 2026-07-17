package vimicalc.view;

import javafx.scene.control.Label;

/**
 * A small chip in the top-left header corner that indicates a macro is
 * being recorded, showing the register name (e.g. {@code ● q}).
 *
 * <p>Mirrors Vim's persistent {@code recording @q} statusline hint: the
 * chip appears the moment recording starts and stays visible across mode
 * changes until recording stops. Backed by a scene-graph {@link Label};
 * {@link #setRecording(char)} and {@link #clearRecording()} update the
 * label immediately.</p>
 */
public class RecordingIndicator {
    private final Label recordingIndicatorLabel;

    /**
     * Creates a recording indicator bound to the given label.
     *
     * @param recordingIndicatorLabel the scene-graph label to update
     */
    public RecordingIndicator(Label recordingIndicatorLabel) {
        this.recordingIndicatorLabel = recordingIndicatorLabel;
    }

    /**
     * Shows the indicator for the macro register being recorded into.
     *
     * @param name the register name of the macro being recorded
     */
    public void setRecording(char name) {
        recordingIndicatorLabel.setText("● " + name);
        recordingIndicatorLabel.setVisible(true);
    }

    /**
     * Hides the indicator. The chip is hidden while its text is empty so it
     * only floats over the header corner while a recording is in progress.
     */
    public void clearRecording() {
        recordingIndicatorLabel.setText("");
        recordingIndicatorLabel.setVisible(false);
    }
}

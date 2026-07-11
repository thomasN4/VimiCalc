package vimicalc.view;

import javafx.scene.control.Label;
import vimicalc.controller.Mode;

import java.util.function.Supplier;

/**
 * The status bar displayed near the bottom of the window.
 *
 * <p>Shows the current editing {@link vimicalc.controller.Mode} and the
 * name of the open file (or "new_file" if unsaved). Backed by a scene-graph
 * {@link Label}; call {@link #refresh()} after mode or filename changes.</p>
 */
public class StatusBar {
    private final Label statusLabel;
    private String filename;
    private final Supplier<Mode> modeSupplier;

    /**
     * Creates the status bar bound to the given label.
     *
     * @param statusLabel  the scene-graph label to update
     * @param modeSupplier supplies the current editing mode
     */
    public StatusBar(Label statusLabel, Supplier<Mode> modeSupplier) {
        this.statusLabel = statusLabel;
        this.modeSupplier = modeSupplier;
        filename = "new_file";
    }

    /**
     * Updates the label text to reflect the current mode and filename.
     *
     * <p>Format: {@code  [MODE]  --filename--}</p>
     */
    public void refresh() {
        statusLabel.setText(" [" + modeSupplier.get() + "]  --" + filename + "--");
    }

    /** @param filename the name of the open file */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}

package vimicalc.model;

/**
 * Callbacks for file I/O operations in {@link Sheet}.
 *
 * <p>Decouples the model from the controller/view by allowing Sheet
 * to notify the application when files are saved or loaded, without
 * directly referencing Controller or JavaFX types.</p>
 */
public interface FileIOCallbacks {

    /**
     * Called after a file has been saved successfully.
     *
     * @param filename the name of the saved file
     */
    void onFileSaved(String filename);

    /**
     * Called after a file has been loaded. The implementation should
     * reset the UI state (camera, cell selector, etc.) and update
     * the displayed filename.
     *
     * @param filename the name of the loaded file
     */
    void onFileLoaded(String filename);
}

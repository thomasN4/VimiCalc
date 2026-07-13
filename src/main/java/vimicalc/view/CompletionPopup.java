package vimicalc.view;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The COMMAND-mode completion popup: a small floating card listing the
 * command names matching what has been typed, anchored to the bottom-left of
 * the canvas just above the command line in the info bar.
 *
 * <p>Backed by a scene-graph {@link VBox} of {@link Label}s inside the
 * overlay pane. The box is keyboard-only (the overlay pane is mouse
 * transparent); the controller drives it via {@link #show(List, int)} and
 * {@link #hide()}. At most {@link #MAX_VISIBLE} items are rendered — when
 * there are more matches, the visible window slides so the selected item
 * stays in view.</p>
 */
public class CompletionPopup {
    /** The maximum number of matches rendered at once. */
    static final int MAX_VISIBLE = 8;

    /** Gap in pixels between the popup and the canvas edges. */
    private static final double MARGIN_X = 8, MARGIN_Y = 6;

    private final VBox box;

    /**
     * Creates the popup bound to the given box, anchoring it to the
     * bottom-left of the overlay pane. The Y anchor is a binding, not a
     * constant, so the popup follows both window resizes and its own height
     * changes as the match list shrinks and grows.
     *
     * @param box         the scene-graph node to render matches into
     * @param overlayPane the overlay hosting the box
     */
    public CompletionPopup(@NotNull VBox box, @NotNull Pane overlayPane) {
        this.box = box;
        box.setLayoutX(MARGIN_X);
        box.layoutYProperty().bind(
            overlayPane.heightProperty()
                .subtract(box.heightProperty())
                .subtract(MARGIN_Y));
    }

    /**
     * Renders the given matches and makes the popup visible, highlighting
     * the selected one. Hides the popup instead when there are no matches.
     *
     * @param matches       the ranked command-name matches
     * @param selectedIndex the index of the selected match in {@code matches},
     *                      or {@code -1} when none is selected (list only)
     */
    public void show(@NotNull List<String> matches, int selectedIndex) {
        if (matches.isEmpty()) {
            hide();
            return;
        }
        int start = 0;
        if (matches.size() > MAX_VISIBLE && selectedIndex >= MAX_VISIBLE)
            start = Math.min(selectedIndex - MAX_VISIBLE + 1, matches.size() - MAX_VISIBLE);
        box.getChildren().clear();
        for (int i = start; i < Math.min(start + MAX_VISIBLE, matches.size()); i++) {
            Label item = new Label(matches.get(i));
            item.getStyleClass().add("completion-item");
            if (i == selectedIndex) item.getStyleClass().add("completion-item-selected");
            item.setMaxWidth(Double.MAX_VALUE);
            box.getChildren().add(item);
        }
        box.setVisible(true);
    }

    /** Hides the popup and drops its rendered items. */
    public void hide() {
        box.getChildren().clear();
        box.setVisible(false);
    }

    /** Returns whether the popup is currently shown. */
    public boolean isVisible() {
        return box.isVisible();
    }
}

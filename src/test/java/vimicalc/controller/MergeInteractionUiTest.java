package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import vimicalc.Main;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for cursor/rendering interactions with merged cells at
 * viewport edges: merge-range-aware visibility culling in
 * {@link vimicalc.view.Picture#take}, axis re-checking in
 * {@link EditorOperations#goTo} (colon commands executed beside a merge used
 * to displace the cursor via the phantom-move/merge-pull interaction), and
 * VISUAL-mode merge/unmerge mode transitions.
 */
@ExtendWith(ApplicationExtension.class)
class MergeInteractionUiTest {

    private Controller controller;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(controller::onKeyPressed);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void mergeStaysVisibleWhenItsStartScrollsOffscreen(FxRobot robot) {
        // Merge (2,2)-(8,3), seven columns wide
        robot.type(KeyCode.V);
        for (int i = 0; i < 6; i++) robot.type(KeyCode.L);
        robot.type(KeyCode.J, KeyCode.M);
        assertEquals(2, controller.cellSelector.getXCoord(), "cursor should land on merge start");

        // Scroll right until the merge-start column leaves the viewport
        for (int i = 0; i < 5; i++) robot.type(KeyCode.L);
        assertTrue(controller.camera.picture.metadata().getFirstXC() > 2,
            "viewport should have scrolled past the merge-start column");

        assertTrue(controller.camera.picture.data().stream()
                .anyMatch(c -> c.xCoord() == 2 && c.yCoord() == 2 && c.isMergeStart()),
            "merge start must stay in the visible cell list while its range intersects the viewport");
    }

    @Test
    void commandExecutedBesideMergeKeepsCursorPosition(FxRobot robot) {
        // Merge (2,2)-(5,3), then move to (3,4) — directly below the merge
        robot.type(KeyCode.V, KeyCode.L, KeyCode.L, KeyCode.L, KeyCode.J, KeyCode.M);
        robot.type(KeyCode.J, KeyCode.L);
        assertEquals(3, controller.cellSelector.getXCoord());
        assertEquals(4, controller.cellSelector.getYCoord());

        // :cellColor red — the ENTER handler's phantom moves + goTo used to
        // strand the cursor at (2,4) here, turning macro replays into a loop
        robot.type(KeyCode.SEMICOLON);
        typeText(robot, "cellColor red");
        robot.type(KeyCode.ENTER);

        assertEquals(3, controller.cellSelector.getXCoord(),
            "colon command below a merge must not displace the cursor column");
        assertEquals(4, controller.cellSelector.getYCoord());
        assertNotNull(controller.sheet.findFormatting(3, 4),
            "the command should have applied to the cell it was typed on");
    }

    @Test
    void unmergeFromInteriorWorksAfterEditingMergedCell(FxRobot robot) {
        // Merge (2,2)-(5,4), then type text into the merged cell — the INSERT
        // commit replaces the merge-start object in the cell map while the
        // interior cells keep referencing the old one (issue #29)
        robot.type(KeyCode.V, KeyCode.L, KeyCode.L, KeyCode.L, KeyCode.J, KeyCode.J, KeyCode.M);
        robot.type(KeyCode.I);
        typeText(robot, "hi");
        robot.type(KeyCode.ESCAPE);
        assertTrue(controller.sheet.simplyFindCell(2, 2).isMergeStart());

        // Move below an interior column and unmerge via a VISUAL selection
        // that touches the interior cell (4,4)
        robot.type(KeyCode.J, KeyCode.L, KeyCode.L);
        assertEquals(4, controller.cellSelector.getXCoord());
        assertEquals(5, controller.cellSelector.getYCoord());
        robot.type(KeyCode.V, KeyCode.K, KeyCode.M);
        robot.type(KeyCode.ESCAPE);

        assertFalse(controller.sheet.simplyFindCell(2, 2).isMergeStart(),
            "the merge-start stored in the cell map must be unmerged");
        assertTrue(controller.camera.picture.data().stream()
                .noneMatch(vimicalc.model.Cell::isMergeStart),
            "no visible cell may still render as a merged block (ghost merge)");
    }

    @Test
    void visualUnmergeExitsToNormalWithSelectionCleared(FxRobot robot) {
        // Merge (2,2)-(8,3); the merge branch exits to NORMAL on the start cell
        robot.type(KeyCode.V);
        for (int i = 0; i < 6; i++) robot.type(KeyCode.L);
        robot.type(KeyCode.J, KeyCode.M);
        assertEquals(Mode.NORMAL, controller.currMode);
        assertEquals(2, controller.cellSelector.getXCoord());

        // VISUAL-select the merge start and unmerge with 'm' — the unmerge
        // branch used to stay in VISUAL with the selection live, so the next
        // 'm' re-merged the selected rectangle (issue #32)
        robot.type(KeyCode.V, KeyCode.M);

        assertEquals(Mode.NORMAL, controller.currMode,
            "VISUAL 'm' unmerge must exit to NORMAL like the merge branch");
        assertTrue(controller.selectedCoords.isEmpty(),
            "the selection must be cleared after unmerging");
        assertTrue(controller.camera.picture.data().stream()
                .noneMatch(vimicalc.model.Cell::isMergeStart),
            "the range must actually be unmerged");
    }

    @Test
    void goToMergeInteriorLandsOnMergeStart(FxRobot robot) {
        // Build merge B2:E4 = (2,2)-(5,4) on the model (avoids TestFX key
        // delivery, which is unreliable on rootless Xwayland), then approach
        // from below the range and go-to an interior cell (issue #31).
        robot.interact(() -> {
            mergeRange(2, 2, 5, 4);
            refreshView();
            controller.editorOps.goTo(2, 5); // below merge start
            assertEquals(2, controller.cellSelector.getXCoord());
            assertEquals(5, controller.cellSelector.getYCoord());

            controller.editorOps.goTo(3, 3); // interior C3

            assertEquals(2, controller.cellSelector.getXCoord(),
                "go-to merge interior must land on merge-start column");
            assertEquals(2, controller.cellSelector.getYCoord(),
                "go-to merge interior must land on merge-start row");
            assertTrue(controller.cellSelector.getSelectedCell().isMergeStart(),
                "selected cell must be the merge start");
        });
    }

    @Test
    void goToMergeDelimiterFromBelowRightLandsOnMergeStart(FxRobot robot) {
        // Same merge, approach from below-right, target the non-start
        // delimiter corner E4 — different approach direction, same landing.
        robot.interact(() -> {
            mergeRange(2, 2, 5, 4);
            refreshView();
            controller.editorOps.goTo(6, 5); // below-right of merge
            assertEquals(6, controller.cellSelector.getXCoord());
            assertEquals(5, controller.cellSelector.getYCoord());

            controller.editorOps.goTo(5, 4); // merge-end delimiter

            assertEquals(2, controller.cellSelector.getXCoord(),
                "go-to merge delimiter must land on merge-start column");
            assertEquals(2, controller.cellSelector.getYCoord(),
                "go-to merge delimiter must land on merge-start row");
            assertTrue(controller.cellSelector.getSelectedCell().isMergeStart());
        });
    }

    /**
     * Creates a rectangular merge on the sheet matching VISUAL-mode {@code m}
     * wiring (start at top-left, end at bottom-right, interiors point at start).
     */
    private void mergeRange(int minXC, int minYC, int maxXC, int maxYC) {
        vimicalc.model.Cell mergeStart = controller.sheet.findCell(minXC, minYC);
        vimicalc.model.Cell mergeEnd = controller.sheet.findCell(maxXC, maxYC);
        if (mergeStart.isEmpty()) controller.sheet.addCell(mergeStart);
        if (mergeEnd.isEmpty()) controller.sheet.addCell(mergeEnd);
        else mergeEnd = new vimicalc.model.Cell(mergeEnd.xCoord(), mergeEnd.yCoord());
        mergeStart.setMergeStart(true);
        mergeStart.mergeWith(mergeEnd);
        mergeEnd.mergeWith(mergeStart);
        for (int i = minXC; i <= maxXC; i++) {
            for (int j = minYC; j <= maxYC; j++) {
                if (i == minXC && j == minYC) continue;
                if (i == maxXC && j == maxYC) continue;
                vimicalc.model.Cell c = new vimicalc.model.Cell(i, j);
                c.mergeWith(mergeStart);
                controller.sheet.addCell(c);
            }
        }
        // ensure end is in the map (may have been replaced when non-empty)
        controller.sheet.addCell(mergeEnd);
    }

    private void refreshView() {
        controller.camera.picture.take(
            controller.gc, controller.sheet, controller.selectedCoords,
            controller.camera.getAbsX(), controller.camera.getAbsY());
        controller.cellSelector.readCell(controller.camera.picture.data());
    }

    /** Types text as real key codes; the Controller reads KEY_PRESSED text. */
    private void typeText(FxRobot robot, String s) {
        for (char c : s.toCharArray()) {
            if (c == ' ') robot.type(KeyCode.SPACE);
            else if (Character.isUpperCase(c))
                robot.press(KeyCode.SHIFT)
                     .type(KeyCode.getKeyCode(String.valueOf(c)))
                     .release(KeyCode.SHIFT);
            else robot.type(KeyCode.getKeyCode(String.valueOf(Character.toUpperCase(c))));
        }
    }
}

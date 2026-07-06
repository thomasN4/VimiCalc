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
 * {@link vimicalc.view.Picture#take}, and axis re-checking in
 * {@link EditorOperations#goTo} (colon commands executed beside a merge used
 * to displace the cursor via the phantom-move/merge-pull interaction).
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

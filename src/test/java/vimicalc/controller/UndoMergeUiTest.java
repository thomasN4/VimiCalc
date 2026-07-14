package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import vimicalc.Main;
import vimicalc.model.Cell;
import vimicalc.view.Formatting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.view.Defaults.GRIDLINE_C;
import static vimicalc.view.Defaults.GUTTER_W;
import static vimicalc.view.Defaults.HEADER_H;

/**
 * Regression tests for issue #77: compound undo/redo of merge, unmerge, and
 * VISUAL-mode delete; merged cells covering their interior gridlines; and the
 * VISUAL-mode repaint explosion on large formatted selections.
 */
@ExtendWith(ApplicationExtension.class)
class UndoMergeUiTest {

    private Controller controller;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(controller::onKeyPressed);
        stage.setScene(scene);
        // TestFX reuses one primary stage across test classes; with the
        // resizable layout (#46) content follows the stage, so pin the size.
        stage.setWidth(900);
        stage.setHeight(600);
        stage.show();
    }

    @Test
    void mergeIsUndoableAndRedoable(FxRobot robot) {
        // B2 = "top", C2 = "mid", cursor back on B2
        robot.type(KeyCode.I);
        typeText(robot, "top");
        robot.type(KeyCode.ESCAPE, KeyCode.L, KeyCode.I);
        typeText(robot, "mid");
        robot.type(KeyCode.ESCAPE, KeyCode.H);

        // Merge B2:C3 — the merge blanks every non-top-left cell
        robot.type(KeyCode.V, KeyCode.L, KeyCode.J, KeyCode.M);
        assertTrue(controller.sheet.simplyFindCell(2, 2).isMergeStart());
        assertNull(controller.sheet.simplyFindCell(3, 2).txt(),
            "merging must blank the non-top-left contents");

        robot.type(KeyCode.U);
        assertFalse(controller.sheet.simplyFindCell(2, 2).isMergeStart(),
            "undo must dissolve the merge");
        assertNull(controller.sheet.simplyFindCell(3, 2).getMergeDelimiter(),
            "undo must clear interior merge pointers");
        assertEquals("top", controller.sheet.simplyFindCell(2, 2).txt());
        assertEquals("mid", controller.sheet.simplyFindCell(3, 2).txt(),
            "undo must bring back the contents the merge destroyed");

        robot.type(KeyCode.R);
        Cell start = controller.sheet.simplyFindCell(2, 2);
        assertTrue(start.isMergeStart(), "redo must re-apply the merge");
        assertEquals("top", start.txt());
        assertNull(controller.sheet.simplyFindCell(3, 2).txt());
        assertSame(start, controller.sheet.simplyFindCell(3, 2).getMergeDelimiter(),
            "re-applied interior pointers must reference the live merge start");

        robot.type(KeyCode.U);
        assertEquals("mid", controller.sheet.simplyFindCell(3, 2).txt(),
            "a second undo must restore the contents again");
    }

    @Test
    void normalModeUnmergeIsUndoable(FxRobot robot) {
        robot.type(KeyCode.V, KeyCode.L, KeyCode.J, KeyCode.M); // merge B2:C3
        robot.type(KeyCode.M);                                  // NORMAL-mode unmerge
        assertFalse(controller.sheet.simplyFindCell(2, 2).isMergeStart());

        robot.type(KeyCode.U);
        Cell start = controller.sheet.simplyFindCell(2, 2);
        assertTrue(start.isMergeStart(), "undo must restore the merge");
        assertNotNull(start.getMergeDelimiter());
        assertEquals(3, start.getMergeDelimiter().xCoord());
        assertEquals(3, start.getMergeDelimiter().yCoord());
        assertSame(start, controller.sheet.simplyFindCell(3, 2).getMergeDelimiter(),
            "restored interior pointers must reference the live merge start");
        assertSame(start, controller.sheet.findCell(3, 3),
            "merge redirection must work on the restored range");

        // One more undo steps back over the original merge as well
        robot.type(KeyCode.U);
        assertFalse(controller.sheet.simplyFindCell(2, 2).isMergeStart());
    }

    @Test
    void visualModeUnmergeIsUndoable(FxRobot robot) {
        robot.type(KeyCode.V, KeyCode.L, KeyCode.J, KeyCode.M); // merge B2:C3
        robot.type(KeyCode.V, KeyCode.M);                       // VISUAL m on a merged cell unmerges
        assertFalse(controller.sheet.simplyFindCell(2, 2).isMergeStart());

        robot.type(KeyCode.U);
        assertTrue(controller.sheet.simplyFindCell(2, 2).isMergeStart(),
            "undo must restore the merge dissolved from VISUAL mode");
    }

    @Test
    void visualDeleteIsUndoableAsOneStep(FxRobot robot) {
        // B2 = "aa", C2 = "bb", B3 = "cc", cursor back on B2
        robot.type(KeyCode.I);
        typeText(robot, "aa");
        robot.type(KeyCode.ESCAPE, KeyCode.L, KeyCode.I);
        typeText(robot, "bb");
        robot.type(KeyCode.ESCAPE, KeyCode.H, KeyCode.J, KeyCode.I);
        typeText(robot, "cc");
        robot.type(KeyCode.ESCAPE, KeyCode.K);

        robot.type(KeyCode.V, KeyCode.L, KeyCode.J, KeyCode.D);
        robot.type(KeyCode.ESCAPE);
        assertNull(controller.sheet.simplyFindCell(2, 2).txt());
        assertNull(controller.sheet.simplyFindCell(3, 2).txt());
        assertNull(controller.sheet.simplyFindCell(2, 3).txt());

        robot.type(KeyCode.U);
        assertEquals("aa", controller.sheet.simplyFindCell(2, 2).txt(),
            "one undo must restore every cell the VISUAL delete removed");
        assertEquals("bb", controller.sheet.simplyFindCell(3, 2).txt());
        assertEquals("cc", controller.sheet.simplyFindCell(2, 3).txt());

        robot.type(KeyCode.R);
        assertNull(controller.sheet.simplyFindCell(2, 2).txt(),
            "redo must re-apply the whole delete step");
        assertNull(controller.sheet.simplyFindCell(3, 2).txt());
        assertNull(controller.sheet.simplyFindCell(2, 3).txt());
    }

    @Test
    void largeFormattedVisualSelectionDoesNotThrow(FxRobot robot) {
        // Issue #77 bug 3: with per-cell formatting present, extending a big
        // VISUAL selection used to trigger one full repaint per selected
        // coordinate per keystroke, flooding the Canvas command buffer until
        // the heap died (OutOfMemoryError below Formatting.renderCell).
        List<Throwable> fxErrors = new CopyOnWriteArrayList<>();
        robot.interact(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> fxErrors.add(e));
            Formatting red = new Formatting();
            red.setCellColor(Color.RED);
            for (int i = 2; i <= 21; i++)
                for (int j = 2; j <= 21; j++)
                    controller.sheet.addFormatting(i, j, red);
        });

        robot.type(KeyCode.V);
        for (int i = 0; i < 19; i++) robot.type(KeyCode.L);
        for (int i = 0; i < 19; i++) robot.type(KeyCode.J);

        assertEquals(400, controller.selectedCoords.size(),
            "the whole 20x20 formatted block should be selected");
        assertTrue(fxErrors.isEmpty(),
            "selecting a large formatted block must not raise FX-thread errors: " + fxErrors);
    }

    @Test
    void unformattedMergeCoversItsInteriorGridlines(FxRobot robot) {
        // Merge B2:C3 with no formatting anywhere; gridlines are on by default
        robot.type(KeyCode.V, KeyCode.L, KeyCode.J, KeyCode.M);
        // Move the cursor off the merge so its opaque highlight doesn't
        // cover the pixels under test
        robot.type(KeyCode.L, KeyCode.L, KeyCode.J, KeyCode.J);

        AtomicReference<WritableImage> shot = new AtomicReference<>();
        robot.interact(() -> {
            Canvas canvas = (Canvas) scene.lookup("#canvas");
            shot.set(canvas.snapshot(null, null));
        });
        PixelReader pixels = shot.get().getPixelReader();

        int[] xs = controller.camera.picture.metadata().getCellAbsXs();
        int[] ys = controller.camera.picture.metadata().getCellAbsYs();
        // Canvas-local x of the column 2|3 boundary — inside the merge from
        // row 2 downward, a regular gridline in row 1
        int boundaryX = xs[3] - controller.camera.getAbsX() + GUTTER_W;
        int insideMergeY = ys[2] - controller.camera.getAbsY() + HEADER_H + (ys[3] - ys[2]) / 2;
        int aboveMergeY = ys[1] - controller.camera.getAbsY() + HEADER_H + (ys[2] - ys[1]) / 2;

        assertTrue(rowContainsColor(pixels, boundaryX, aboveMergeY, GRIDLINE_C),
            "control: the same column boundary outside the merge must show a gridline");
        assertFalse(rowContainsColor(pixels, boundaryX, insideMergeY, GRIDLINE_C),
            "the merged block must cover the gridline between its columns");
    }

    /** Checks the three pixels around (x, y) horizontally for the given color. */
    private boolean rowContainsColor(PixelReader pixels, int x, int y, Color color) {
        for (int dx = -1; dx <= 1; dx++)
            if (colorsClose(pixels.getColor(x + dx, y), color)) return true;
        return false;
    }

    private boolean colorsClose(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) < 0.05 &&
               Math.abs(a.getGreen() - b.getGreen()) < 0.05 &&
               Math.abs(a.getBlue() - b.getBlue()) < 0.05;
    }

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

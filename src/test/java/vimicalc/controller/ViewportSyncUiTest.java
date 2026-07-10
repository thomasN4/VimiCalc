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
import vimicalc.view.Positions;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.view.Defaults.*;

/**
 * Regression tests for issue #30: the cell grid, headers, and cursor used to
 * track the scroll position through three independent pixel accountings
 * (Camera, Positions, CellSelector) that drifted apart after scrolling or
 * {@code :resCol}/{@code :resRow}. All viewport consumers now derive screen
 * positions from the live camera offset via the shared formula
 * {@code cellAbs - camera.abs + GUTTER_W/HEADER_H}; these tests pin that
 * invariant through the interaction paths that used to desync.
 */
@ExtendWith(ApplicationExtension.class)
class ViewportSyncUiTest {

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
    void cursorStaysAlignedThroughScrollSequences(FxRobot robot) {
        for (int i = 0; i < 12; i++) robot.type(KeyCode.L);
        assertCursorGridAligned();
        for (int i = 0; i < 25; i++) robot.type(KeyCode.J);
        assertCursorGridAligned();
        for (int i = 0; i < 6; i++) robot.type(KeyCode.H);
        assertCursorGridAligned();
        for (int i = 0; i < 10; i++) robot.type(KeyCode.K);
        assertCursorGridAligned();
        assertTrue(controller.camera.getAbsX() >= GUTTER_W,
            "camera must never scroll left of the sheet origin");
        assertTrue(controller.camera.getAbsY() >= HEADER_H,
            "camera must never scroll above the sheet origin");
    }

    @Test
    void scrollOutAndBackReturnsCameraHome(FxRobot robot) {
        for (int i = 0; i < 15; i++) robot.type(KeyCode.L);
        for (int i = 0; i < 20; i++) robot.type(KeyCode.H);
        assertEquals(1, controller.cellSelector.getXCoord());
        assertEquals(GUTTER_W, controller.camera.getAbsX(),
            "scrolling back to column 1 must land the camera exactly home (no residual drift)");
        assertCursorGridAligned();

        for (int i = 0; i < 25; i++) robot.type(KeyCode.J);
        for (int i = 0; i < 30; i++) robot.type(KeyCode.K);
        assertEquals(1, controller.cellSelector.getYCoord());
        assertEquals(HEADER_H, controller.camera.getAbsY(),
            "scrolling back to row 1 must land the camera exactly home (no residual drift)");
        assertCursorGridAligned();
    }

    @Test
    void resColAndResRowKeepCursorAligned(FxRobot robot) {
        // Cursor starts at (2,2); move to column 3 and widen it by 31px —
        // the relayout used to shift the grid under a stale header/cursor
        robot.type(KeyCode.L);
        int prevAbsX = controller.camera.getAbsX(), prevAbsY = controller.camera.getAbsY();
        robot.type(KeyCode.SEMICOLON);
        typeText(robot, "resCol 31");
        robot.type(KeyCode.ENTER);

        Positions pos = controller.camera.picture.metadata();
        assertEquals(DEFAULT_CELL_W + 31, pos.getCellAbsXs()[4] - pos.getCellAbsXs()[3],
            "column 3 must be widened by the resCol offset");
        assertEquals(prevAbsX, controller.camera.getAbsX(), "resCol must not move the camera");
        assertEquals(prevAbsY, controller.camera.getAbsY(), "resCol must not move the camera");
        assertCursorGridAligned();

        robot.type(KeyCode.SEMICOLON);
        typeText(robot, "resRow 13");
        robot.type(KeyCode.ENTER);
        assertEquals(DEFAULT_CELL_H + 13, pos.getCellAbsYs()[3] - pos.getCellAbsYs()[2],
            "row 2 must be taller by the resRow offset");
        assertCursorGridAligned();

        // Scroll across the resized column/row: the exact-overshoot scroll must
        // keep the alignment invariant through non-default cell sizes
        for (int i = 0; i < 10; i++) robot.type(KeyCode.L);
        assertCursorGridAligned();
        for (int i = 0; i < 25; i++) robot.type(KeyCode.J);
        assertCursorGridAligned();
    }

    @Test
    void colonCommandDoesNotMoveCameraOrCursor(FxRobot robot) {
        robot.type(KeyCode.L, KeyCode.J);
        int prevAbsX = controller.camera.getAbsX(), prevAbsY = controller.camera.getAbsY();
        int prevXC = controller.cellSelector.getXCoord(), prevYC = controller.cellSelector.getYCoord();

        // The ENTER handler used to issue phantom moves that nudged the camera
        // without regenerating the layout
        robot.type(KeyCode.SEMICOLON);
        typeText(robot, "cellColor red");
        robot.type(KeyCode.ENTER);

        assertEquals(prevAbsX, controller.camera.getAbsX(), "colon command must not move the camera");
        assertEquals(prevAbsY, controller.camera.getAbsY(), "colon command must not move the camera");
        assertEquals(prevXC, controller.cellSelector.getXCoord());
        assertEquals(prevYC, controller.cellSelector.getYCoord());
        assertCursorGridAligned();
    }

    @Test
    void cursorSitsFlushAtEdgesAfterScroll(FxRobot robot) {
        // 852px of drawable width / 96px columns: the 8th L from B2 scrolls
        for (int i = 0; i < 8; i++) robot.type(KeyCode.L);
        assertTrue(controller.camera.getAbsX() > GUTTER_W, "rightward scroll expected");
        assertEquals(controller.CANVAS_W,
            controller.cellSelector.getX() + controller.cellSelector.getW(),
            "after a scroll the cursor cell must sit flush against the right edge");
        assertCursorGridAligned();

        for (int i = 0; i < 21; i++) robot.type(KeyCode.J);
        assertTrue(controller.camera.getAbsY() > HEADER_H, "downward scroll expected");
        assertEquals(controller.statusBar.getY(),
            controller.cellSelector.getY() + controller.cellSelector.getH(),
            "after a scroll the cursor cell must sit flush against the bottom edge");
        assertCursorGridAligned();
    }

    @Test
    void zoomKeepsCursorAlignedAndCameraClamped(FxRobot robot) {
        Positions pos = controller.camera.picture.metadata();
        int prevAbsX = controller.camera.getAbsX(), prevAbsY = controller.camera.getAbsY();

        robot.press(KeyCode.CONTROL).type(KeyCode.EQUALS).release(KeyCode.CONTROL);
        assertEquals(Math.round(DEFAULT_CELL_W * ZOOM_STEP), pos.getCellAbsXs()[3] - pos.getCellAbsXs()[2],
            "one zoom-in step must scale column widths by ZOOM_STEP");
        assertEquals(prevAbsX, controller.camera.getAbsX(), "zoom must not move the camera");
        assertEquals(prevAbsY, controller.camera.getAbsY(), "zoom must not move the camera");
        assertCursorGridAligned();

        robot.press(KeyCode.CONTROL).type(KeyCode.DIGIT0).release(KeyCode.CONTROL);
        assertEquals(1.0, pos.getZoom(), "Ctrl-0 must reset the zoom factor");
        assertEquals(DEFAULT_CELL_W, pos.getCellAbsXs()[3] - pos.getCellAbsXs()[2],
            "Ctrl-0 must restore the default column width");
        assertCursorGridAligned();

        robot.type(KeyCode.SEMICOLON);
        typeText(robot, "zoom 200");
        robot.type(KeyCode.ENTER);
        assertEquals(2.0, pos.getZoom(), ":zoom 200 must set the zoom factor to 2.0");
        assertEquals(2 * DEFAULT_CELL_W, pos.getCellAbsXs()[3] - pos.getCellAbsXs()[2]);
        assertCursorGridAligned();
    }

    /**
     * The issue #30 invariant: the cursor's pixel rectangle equals the shared
     * viewport formula applied to its logical cell — the same arithmetic the
     * grid and headers render with.
     */
    private void assertCursorGridAligned() {
        Positions pos = controller.camera.picture.metadata();
        int xc = controller.cellSelector.getXCoord(), yc = controller.cellSelector.getYCoord();
        assertEquals(pos.getCellAbsXs()[xc] - controller.camera.getAbsX() + GUTTER_W,
            controller.cellSelector.getX(), "cursor X must derive from the live camera offset");
        assertEquals(pos.getCellAbsYs()[yc] - controller.camera.getAbsY() + HEADER_H,
            controller.cellSelector.getY(), "cursor Y must derive from the live camera offset");
        assertEquals(pos.getCellAbsXs()[xc+1] - pos.getCellAbsXs()[xc],
            controller.cellSelector.getW(), "cursor width must match the cell's boundary span");
        assertEquals(pos.getCellAbsYs()[yc+1] - pos.getCellAbsYs()[yc],
            controller.cellSelector.getH(), "cursor height must match the cell's boundary span");
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

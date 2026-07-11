package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import vimicalc.Main;
import vimicalc.view.Positions;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.view.Defaults.*;

/**
 * Tests for issue #46: the window is resizable and the grid canvas tracks the
 * available space. Growing reveals more cells without moving the camera;
 * shrinking scrolls the selected cell back into view; the chrome rows stay
 * pinned to the bottom; and the issue #30 cursor alignment invariant holds
 * across every size change.
 */
@ExtendWith(ApplicationExtension.class)
class ResizeUiTest {

    private Controller controller;
    private Parent root;
    private Stage stage;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        root = loader.load();
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

    private void resizeStage(FxRobot robot, double w, double h) {
        robot.interact(() -> {
            stage.setWidth(w);
            stage.setHeight(h);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Canvas canvas() {
        return (Canvas) root.lookup("#canvas");
    }

    private StackPane canvasStack() {
        return (StackPane) root.lookup("#canvasStack");
    }

    @Test
    void growRevealsMoreGridWithoutMovingCamera(FxRobot robot) {
        int prevAbsX = controller.camera.getAbsX(), prevAbsY = controller.camera.getAbsY();
        int prevLastXC = controller.camera.picture.metadata().getLastXC();

        resizeStage(robot, 1200, 720);

        assertTrue(canvasStack().getWidth() > 900,
            "the canvas stack must claim the extra window width");
        assertEquals((int) canvasStack().getWidth(), (int) canvas().getWidth(),
            "the canvas must track the canvas stack's width");
        assertEquals((int) canvasStack().getHeight(), (int) canvas().getHeight(),
            "the canvas must track the canvas stack's height");
        assertEquals((int) canvas().getWidth(), controller.CANVAS_W);
        assertEquals((int) canvas().getHeight(), controller.viewportBottom());

        assertEquals(prevAbsX, controller.camera.getAbsX(), "growing must not move the camera");
        assertEquals(prevAbsY, controller.camera.getAbsY(), "growing must not move the camera");
        assertTrue(controller.camera.picture.metadata().getLastXC() > prevLastXC,
            "a wider viewport must make more columns visible");
        assertCursorGridAligned();

        HBox infoRow = (HBox) root.lookup("#infoRow");
        assertEquals(scene.getHeight(),
            infoRow.localToScene(infoRow.getBoundsInLocal()).getMaxY(), 0.5,
            "the info row must stay pinned to the bottom of the window");
    }

    @Test
    void shrinkWidthPullsCursorBackIntoView(FxRobot robot) {
        // Same setup as ViewportSyncUiTest: 8 L presses scroll rightward,
        // leaving the cursor flush against the right edge.
        for (int i = 0; i < 8; i++) robot.type(KeyCode.L);
        int prevAbsX = controller.camera.getAbsX();
        assertTrue(prevAbsX > GUTTER_W, "rightward scroll expected");

        resizeStage(robot, 600, 600);

        assertEquals(controller.CANVAS_W,
            controller.cellSelector.getX() + controller.cellSelector.getW(),
            "after a width shrink the cursor must sit flush against the new right edge");
        assertTrue(controller.camera.getAbsX() > prevAbsX,
            "the camera must scroll right to keep the cursor visible");
        assertCursorGridAligned();
    }

    @Test
    void shrinkHeightPullsCursorBackIntoView(FxRobot robot) {
        for (int i = 0; i < 21; i++) robot.type(KeyCode.J);
        int prevAbsY = controller.camera.getAbsY();
        assertTrue(prevAbsY > HEADER_H, "downward scroll expected");

        resizeStage(robot, 900, 400);

        assertEquals(controller.viewportBottom(),
            controller.cellSelector.getY() + controller.cellSelector.getH(),
            "after a height shrink the cursor must sit flush against the new bottom edge");
        assertTrue(controller.camera.getAbsY() > prevAbsY,
            "the camera must scroll down to keep the cursor visible");
        assertCursorGridAligned();
    }

    @Test
    void sharpShrinkWhileDeepScrolledDoesNotThrow(FxRobot robot) {
        // Regression: generate() only extends the boundary arrays ~2 cells past
        // the new viewport edge, so a sharp shrink while deep-scrolled on empty
        // cells used to leave the cursor column outside the arrays (AIOOBE in
        // syncToGrid/scrollCursorIntoView) without the maxXC coverage bump.
        for (int i = 0; i < 20; i++) robot.type(KeyCode.L);

        resizeStage(robot, 400, 600);

        assertEquals(controller.CANVAS_W,
            controller.cellSelector.getX() + controller.cellSelector.getW(),
            "cursor must land flush at the new right edge after a sharp shrink");
        assertCursorGridAligned();
    }

    @Test
    void resizeDuringInsertPreservesEditBuffer(FxRobot robot) {
        robot.type(KeyCode.I);
        assertEquals(Mode.INSERT, controller.currMode);
        robot.type(KeyCode.H, KeyCode.I);
        assertEquals("hi", controller.cellSelector.getSelectedCell().txt(),
            "sanity: typed text lives in the edit buffer");

        resizeStage(robot, 700, 500);

        assertEquals(Mode.INSERT, controller.currMode,
            "a resize must not change the editing mode");
        assertEquals("hi", controller.cellSelector.getSelectedCell().txt(),
            "a resize mid-edit must not discard the INSERT buffer");
        assertCursorGridAligned();
    }

    /** The issue #30 invariant, same formula as ViewportSyncUiTest. */
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
}

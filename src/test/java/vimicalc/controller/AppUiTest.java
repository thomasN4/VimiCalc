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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Headless-capable UI integration tests for the main VimiCalc controller.
 *
 * <p>These tests boot the full JavaFX scene graph (via TestFX) and assert on
 * controller/model state rather than rendered pixels, since the application
 * draws to a custom {@link javafx.scene.canvas.Canvas}.</p>
 *
 * <p><b>Running headlessly</b> (e.g. over SSH without a display):</p>
 * <pre>{@code
 *   xvfb-run ./gradlew test
 * }</pre>
 */
@ExtendWith(ApplicationExtension.class)
class AppUiTest {

    private Controller controller;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(controller::onKeyPressed);
        stage.setScene(scene);
        // TestFX reuses one primary stage across test classes; with the
        // resizable layout (#46) content follows the stage, so pin the size.
        stage.setWidth(900);
        stage.setHeight(600);
        stage.show();
    }

    @Test
    void appStartsInNormalMode() {
        assertEquals(Mode.NORMAL, controller.currMode,
            "Application should start in NORMAL mode");
    }

    @Test
    void pressingJMovesCursorDown(FxRobot robot) {
        int initialY = controller.cellSelector.getYCoord();
        robot.type(KeyCode.J);
        assertEquals(initialY + 1, controller.cellSelector.getYCoord(),
            "Pressing j should move the cursor down one row");
    }

    @Test
    void pressingILeavesNormalMode(FxRobot robot) {
        robot.type(KeyCode.I);
        assertEquals(Mode.INSERT, controller.currMode,
            "Pressing i should switch to INSERT mode");
    }

    @Test
    void pressingEqualsEntersFormulaMode(FxRobot robot) {
        robot.type(KeyCode.EQUALS);
        assertEquals(Mode.FORMULA, controller.currMode,
            "Pressing = should switch to FORMULA mode");
    }
}

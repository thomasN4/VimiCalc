package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import vimicalc.Main;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX checks for the COMMAND-mode completion popup: it appears while a
 * command name is typed, Ctrl+N / Ctrl+P cycle the selection into the
 * command line, and ESC is two-stage (dismiss popup, then exit COMMAND
 * mode).
 *
 * <p>Keys are injected with {@link FxRobot#type(KeyCode...)} through the
 * scene-level key handler, like the other UI tests. Run headlessly (Monocle
 * or Xvfb); robot input is unreliable against a live display.</p>
 */
@ExtendWith(ApplicationExtension.class)
class CommandCompletionPopupUiTest {

    private Controller controller;
    private Parent root;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        root = loader.load();
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

    private VBox popup() {
        return (VBox) root.lookup("#completionLabelBox");
    }

    private void ctrl(FxRobot robot, KeyCode code) {
        robot.press(KeyCode.CONTROL).type(code).release(KeyCode.CONTROL);
    }

    @Test
    void popupHiddenAtStartup() {
        VBox box = popup();
        assertNotNull(box, "completion popup node must be present");
        assertFalse(box.isVisible(), "popup starts hidden");
    }

    @Test
    void typingShowsPopupAndCtrlNCtrlPCycleTheSelection(FxRobot robot) {
        // App uses ';' for command mode (not ':')
        robot.type(KeyCode.SEMICOLON);
        assertFalse(popup().isVisible(), "no popup on an empty command line");

        robot.type(KeyCode.R, KeyCode.E);
        assertTrue(popup().isVisible(), "popup appears while typing a name");
        assertEquals("re", controller.command.getTxt());
        assertFalse(popup().getChildren().isEmpty());

        ctrl(robot, KeyCode.N);
        assertEquals("resizeColumn", controller.command.getTxt(),
            "Ctrl+N writes the first match into the command line");
        ctrl(robot, KeyCode.N);
        assertEquals("resizeRow", controller.command.getTxt());
        ctrl(robot, KeyCode.P);
        assertEquals("resizeColumn", controller.command.getTxt(),
            "Ctrl+P steps the selection back");

        robot.type(KeyCode.ESCAPE);
        assertFalse(popup().isVisible(), "first ESC dismisses the popup");
        assertEquals(Mode.COMMAND, controller.currMode,
            "first ESC must not leave COMMAND mode");
        assertEquals("resizeColumn", controller.command.getTxt(),
            "first ESC keeps the command line text");

        robot.type(KeyCode.ESCAPE);
        assertEquals(Mode.NORMAL, controller.currMode, "second ESC exits COMMAND mode");
    }

    @Test
    void tabStillCyclesAndArgumentsDismissThePopup(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON);
        robot.type(KeyCode.W);
        assertTrue(popup().isVisible());

        robot.type(KeyCode.TAB);
        assertEquals("write", controller.command.getTxt(), "TAB cycles like Ctrl+N");

        robot.type(KeyCode.SPACE);
        assertFalse(popup().isVisible(), "typing an argument dismisses the popup");

        robot.type(KeyCode.ESCAPE);
        assertEquals(Mode.NORMAL, controller.currMode,
            "with no popup shown, ESC exits COMMAND mode directly");
    }

    @Test
    void cyclingPutsTheCaretAtTheEndAndMovementKeepsThePopup(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON);
        robot.type(KeyCode.R, KeyCode.E);
        assertTrue(popup().isVisible());

        ctrl(robot, KeyCode.N);
        assertEquals("resizeColumn", controller.command.getTxt());
        assertEquals(controller.command.getTxt().length(), controller.command.getCaret(),
            "completion cycling places the caret at the end");

        ctrl(robot, KeyCode.B);
        ctrl(robot, KeyCode.B);
        assertEquals("resizeColumn", controller.command.getTxt(),
            "caret movement must not change the command text");
        assertEquals(controller.command.getTxt().length() - 2, controller.command.getCaret());
        assertTrue(popup().isVisible(),
            "caret movement must not dismiss the popup");

        ctrl(robot, KeyCode.F);
        assertEquals(controller.command.getTxt().length() - 1, controller.command.getCaret(),
            "Ctrl+F moves the caret right again");

        robot.type(KeyCode.ESCAPE, KeyCode.ESCAPE);
        assertEquals(Mode.NORMAL, controller.currMode);
    }

    @Test
    void enterOnHelpLeavesNoPopupOverTheHelpOverlay(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON);
        robot.type(KeyCode.H, KeyCode.E, KeyCode.L, KeyCode.P);
        assertTrue(popup().isVisible(), "typing 'help' filters the popup");

        robot.type(KeyCode.ENTER);
        assertEquals(Mode.HELP, controller.currMode);
        assertFalse(popup().isVisible(), "ENTER hides the popup before opening help");

        robot.type(KeyCode.ESCAPE);
        assertEquals(Mode.NORMAL, controller.currMode);
    }
}

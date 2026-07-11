package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
 * TestFX checks that bottom-chrome Labels track app state after the scene-graph
 * migration (issue #43). Labels are queryable via {@code fx:id} without pixel reads.
 *
 * <p>Keys are injected with {@link FxRobot#type(KeyCode...)} through the
 * scene-level key handler, like the other UI tests. Run headlessly (Monocle
 * or Xvfb); robot input is unreliable against a live display.</p>
 */
@ExtendWith(ApplicationExtension.class)
class ChromeLabelsUiTest {

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

    private Label label(String fxId) {
        return (Label) root.lookup(fxId);
    }

    @Test
    void initialChromeLabelsMatchStartupState() {
        Label status = label("#statusLabel");
        Label coords = label("#coordsLabel");
        Label info = label("#infoLabel");
        Label keyStroke = label("#keyStrokeLabel");

        assertTrue(status.getText().contains("[NORMAL]"),
            "status bar should show NORMAL mode on startup: " + status.getText());
        assertTrue(status.getText().contains("new_file"),
            "status bar should show default filename: " + status.getText());
        assertEquals("B2", coords.getText(),
            "coords should start at B2");
        assertEquals("(=I)", info.getText(),
            "empty cell shows default info placeholder");
        assertNotNull(keyStroke, "keystroke overlay label must be present");
        assertEquals("", keyStroke.getText(),
            "keystroke label starts empty before any key");
    }

    @Test
    void keyStrokeLabelTracksLastKeyPressed(FxRobot robot) {
        robot.type(KeyCode.J);
        Label keyStroke = label("#keyStrokeLabel");
        assertEquals("J", keyStroke.getText(),
            "keystroke label should show KeyCode name after j");

        robot.type(KeyCode.V);
        assertEquals("V", keyStroke.getText(),
            "keystroke label should update to the latest key");
    }

    @Test
    void statusLabelTracksVisualMode(FxRobot robot) {
        robot.type(KeyCode.V);
        assertEquals(Mode.VISUAL, controller.currMode);

        Label status = label("#statusLabel");
        assertTrue(status.getText().contains("[VISUAL]"),
            "status bar should show VISUAL after v: " + status.getText());
    }

    @Test
    void infoLabelEchoesColonCommand(FxRobot robot) {
        // App uses ';' for command mode (not ':')
        robot.type(KeyCode.SEMICOLON);
        assertEquals(Mode.COMMAND, controller.currMode);

        robot.type(KeyCode.W);
        Label info = label("#infoLabel");
        assertTrue(info.getText().startsWith(":"),
            "command mode should prefix with ':': " + info.getText());
        assertTrue(info.getText().contains("w"),
            "typed command characters should appear in infoLabel: " + info.getText());
    }

    @Test
    void coordsLabelUpdatesOnMove(FxRobot robot) {
        robot.type(KeyCode.J);
        Label coords = label("#coordsLabel");
        assertEquals("B3", coords.getText(),
            "moving down from B2 should show B3");
    }

    @Test
    void exprLabelShowsPendingKeyCommand(FxRobot robot) {
        robot.type(KeyCode.DIGIT5);
        Label expr = label("#exprLabel");
        assertEquals("5", expr.getText(),
            "pending multiplier should appear on exprLabel while typing a key command");
    }

    @Test
    void canvasIsGridOnly() {
        javafx.scene.canvas.Canvas canvas = (javafx.scene.canvas.Canvas) root.lookup("#canvas");
        javafx.scene.layout.StackPane stack = (javafx.scene.layout.StackPane) root.lookup("#canvasStack");
        assertEquals((int) canvas.getWidth(), controller.CANVAS_W,
            "tracked canvas width must match the canvas node");
        assertEquals((int) canvas.getHeight(), controller.viewportBottom(),
            "viewport bottom equals canvas height after chrome leaves the canvas");
        assertEquals((int) stack.getWidth(), (int) canvas.getWidth(),
            "the canvas must fill the canvas stack");
        assertEquals((int) stack.getHeight(), (int) canvas.getHeight(),
            "the canvas must fill the canvas stack");
    }
}

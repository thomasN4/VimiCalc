package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
 * <p>Key events are delivered by calling {@link Controller#onKeyPressed} with
 * synthetic events rather than OS-level key injection, so the suite stays reliable
 * without a focused display window.</p>
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
        stage.show();
    }

    private Label label(String fxId) {
        return (Label) root.lookup(fxId);
    }

    /** Delivers a KEY_PRESSED event to the controller on the JavaFX thread. */
    private void press(FxRobot robot, KeyCode code, String text) {
        KeyEvent event = new KeyEvent(
            KeyEvent.KEY_PRESSED, text, text, code,
            false, false, false, false
        );
        robot.interact(() -> controller.onKeyPressed(event));
    }

    private void press(FxRobot robot, KeyCode code) {
        String text = code.isLetterKey() || code.isDigitKey()
            ? code.getName().toLowerCase()
            : "";
        if (code == KeyCode.DIGIT5) text = "5";
        if (code == KeyCode.SEMICOLON) text = ";";
        if (code == KeyCode.V) text = "v";
        if (code == KeyCode.J) text = "j";
        if (code == KeyCode.W) text = "w";
        press(robot, code, text);
    }

    @Test
    void initialChromeLabelsMatchStartupState() {
        Label status = label("#statusLabel");
        Label coords = label("#coordsLabel");
        Label info = label("#infoLabel");

        assertTrue(status.getText().contains("[NORMAL]"),
            "status bar should show NORMAL mode on startup: " + status.getText());
        assertTrue(status.getText().contains("new_file"),
            "status bar should show default filename: " + status.getText());
        assertEquals("B2", coords.getText(),
            "coords should start at B2");
        assertEquals("(=I)", info.getText(),
            "empty cell shows default info placeholder");
    }

    @Test
    void statusLabelTracksVisualMode(FxRobot robot) {
        press(robot, KeyCode.V);
        assertEquals(Mode.VISUAL, controller.currMode);

        Label status = label("#statusLabel");
        assertTrue(status.getText().contains("[VISUAL]"),
            "status bar should show VISUAL after v: " + status.getText());
    }

    @Test
    void infoLabelEchoesColonCommand(FxRobot robot) {
        // App uses ';' for command mode (not ':')
        press(robot, KeyCode.SEMICOLON);
        assertEquals(Mode.COMMAND, controller.currMode);

        press(robot, KeyCode.W);
        Label info = label("#infoLabel");
        assertTrue(info.getText().startsWith(":"),
            "command mode should prefix with ':': " + info.getText());
        assertTrue(info.getText().contains("w"),
            "typed command characters should appear in infoLabel: " + info.getText());
    }

    @Test
    void coordsLabelUpdatesOnMove(FxRobot robot) {
        press(robot, KeyCode.J);
        Label coords = label("#coordsLabel");
        assertEquals("B3", coords.getText(),
            "moving down from B2 should show B3");
    }

    @Test
    void exprLabelShowsPendingKeyCommand(FxRobot robot) {
        press(robot, KeyCode.DIGIT5);
        Label expr = label("#exprLabel");
        assertEquals("5", expr.getText(),
            "pending multiplier should appear on exprLabel while typing a key command");
    }

    @Test
    void canvasIsGridOnlyHeight() {
        assertEquals(900, controller.CANVAS_W);
        assertEquals(548, controller.viewportBottom(),
            "viewport bottom equals canvas height after chrome leaves the canvas");
    }
}

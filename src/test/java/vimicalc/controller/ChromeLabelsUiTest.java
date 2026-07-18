package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
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

    private Text text(String fxId) {
        return (Text) root.lookup(fxId);
    }

    /** The full info-bar text: before-caret, caret-char, and after runs joined. */
    private String infoText() {
        return text("#infoTextBefore").getText() + text("#infoTextAt").getText()
            + text("#infoTextAfter").getText();
    }

    @Test
    void initialChromeLabelsMatchStartupState() {
        Label status = label("#statusLabel");
        Label coords = label("#coordsLabel");
        Label recording = label("#recordingIndicatorLabel");

        assertTrue(status.getText().contains("[NORMAL]"),
            "status bar should show NORMAL mode on startup: " + status.getText());
        assertTrue(status.getText().contains("new_file"),
            "status bar should show default filename: " + status.getText());
        assertEquals("B2", coords.getText(),
            "coords should start at B2");
        assertEquals("(=I)", infoText(),
            "empty cell shows default info placeholder");
        assertFalse(root.lookup("#infoCaret").isVisible(),
            "no caret outside COMMAND mode");
        assertNotNull(recording, "recording indicator label must be present");
        assertEquals("", recording.getText(),
            "recording indicator starts empty while no macro is recording");
        assertFalse(recording.isVisible(),
            "recording indicator starts hidden");
    }

    @Test
    void recordingIndicatorTracksMacroRecording(FxRobot robot) {
        Label recording = label("#recordingIndicatorLabel");

        robot.type(KeyCode.J);
        assertEquals("", recording.getText(),
            "plain key presses must not put a key name on the chip");

        robot.type(KeyCode.Q, KeyCode.A);
        assertEquals("● a", recording.getText(),
            "starting a recording with qa should show the register name");
        assertTrue(recording.isVisible(),
            "indicator must be visible while recording");

        robot.type(KeyCode.ESCAPE);
        robot.type(KeyCode.J);
        assertTrue(recording.isVisible(),
            "Escape / mode resets must not hide the indicator while recording");
        assertEquals("● a", recording.getText(),
            "indicator keeps the register name across keystrokes");

        robot.type(KeyCode.Q);
        assertEquals("", recording.getText(),
            "stopping the recording with q should clear the indicator");
        assertFalse(recording.isVisible(),
            "indicator hides once recording stops");
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
        assertTrue(infoText().startsWith(":"),
            "command mode should prefix with ':': " + infoText());
        assertTrue(infoText().contains("w"),
            "typed command characters should appear in the info bar: " + infoText());

        robot.type(KeyCode.ESCAPE);
    }

    @Test
    void commandCaretSplitsTextAndHidesOnError(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON, KeyCode.W, KeyCode.Q);
        javafx.scene.Node caret = root.lookup("#infoCaret");
        assertTrue(caret.isVisible(),
            "caret must be visible while typing a command");
        assertFalse(caret.isManaged(),
            "caret must not participate in layout, or it nudges the after-caret text");
        assertEquals(text("#infoTextBefore").getBoundsInParent().getHeight(),
            caret.getBoundsInParent().getHeight(), 0.001,
            "caret height must match the text run's line box");
        assertEquals(text("#infoTextBefore").getBoundsInParent().getMaxX(),
            caret.getBoundsInParent().getMinX(), 0.001,
            "block caret must start right at the end of the before-run");
        assertTrue(caret.getBoundsInParent().getWidth() > 1,
            "block caret on an end-of-line blank cell is one character wide");
        assertEquals(":wq", text("#infoTextBefore").getText(),
            "with the caret at the end, all text is in the 'before' run");
        assertEquals("", text("#infoTextAt").getText(),
            "at the end of the line the block sits on a blank cell");
        assertEquals("", text("#infoTextAfter").getText());

        robot.press(KeyCode.CONTROL).type(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(":w", text("#infoTextBefore").getText(),
            "Ctrl+B puts the block caret on the 'q'");
        assertEquals("q", text("#infoTextAt").getText(),
            "the character under the block lives in the caret-char run");
        assertEquals("", text("#infoTextAfter").getText());
        assertEquals("wq", controller.command.getTxt(),
            "caret movement must not change the command text");
        assertEquals(text("#infoTextAt").getBoundsInParent().getWidth(),
            caret.getBoundsInParent().getWidth(), 0.001,
            "the block must cover exactly the caret character's cell");
        assertTrue(text("#infoTextAt").getStyleClass().contains("info-caret-char"),
            "the caret character inverts while the blink phase is on");

        // Mid-line insert: the block stays on the same character.
        robot.type(KeyCode.X);
        assertEquals(":wx", text("#infoTextBefore").getText());
        assertEquals("q", text("#infoTextAt").getText());
        assertEquals("", text("#infoTextAfter").getText());
        assertEquals("wxq", controller.command.getTxt());

        // Executing the (unknown) command fails; the error message replaces
        // the command line and the caret disappears with it.
        robot.type(KeyCode.ENTER);
        assertEquals(Mode.NORMAL, controller.currMode);
        assertFalse(root.lookup("#infoCaret").isVisible(),
            "error display must hide the caret");
        assertEquals("", text("#infoTextAt").getText(),
            "non-command display collapses to the 'before' run");
        assertEquals("", text("#infoTextAfter").getText(),
            "non-command display collapses to the 'before' run");
        assertFalse(text("#infoTextAt").getStyleClass().contains("info-caret-char"),
            "the inverted-char style must not survive the caret");
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

package vimicalc.controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
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

    /** The full info-bar text on the left side of the bar. */
    private String infoText() {
        return text("#infoText").getText();
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
    void commandCaretOverlaysTextAndHidesOnError(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON, KeyCode.W, KeyCode.Q);
        javafx.scene.Node caret = root.lookup("#infoCaret");
        javafx.scene.text.Text caretChar = text("#infoCaretChar");
        javafx.scene.text.Text line = text("#infoText");
        assertTrue(caret.isVisible(),
            "caret must be visible while typing a command");
        assertFalse(caret.isManaged(),
            "caret must not participate in layout, or it nudges the text");
        assertFalse(caretChar.isManaged(),
            "the inverse-video overlay must not participate in layout either");
        assertEquals(":wq", line.getText(),
            "the whole line lives in one text node — it is never split");
        assertEquals("", caretChar.getText(),
            "at the end of the line the block sits on a blank cell");
        double charW = caret.getBoundsInParent().getWidth();
        assertTrue(charW > 1,
            "block caret on an end-of-line blank cell is one character wide");
        assertEquals(line.getBoundsInParent().getMaxX(),
            caret.getBoundsInParent().getMinX(), 1.0,
            "at end of line the block starts right after the text");
        // The block spans the glyph extent (top of 'l' to descender bottom),
        // strictly inside the logical line box — not poking above ascenders.
        assertTrue(caret.getBoundsInParent().getMinY()
                > line.getBoundsInParent().getMinY(),
            "the block must not poke above tall letters");
        assertTrue(caret.getBoundsInParent().getMaxY()
                <= line.getBoundsInParent().getMaxY() + 0.5,
            "the block must stay inside the text's line box");

        Bounds lineBoundsBefore = line.getBoundsInParent();
        robot.press(KeyCode.CONTROL).type(KeyCode.B).release(KeyCode.CONTROL);
        assertEquals(":wq", line.getText(),
            "caret movement must not touch the displayed text");
        assertEquals(lineBoundsBefore, line.getBoundsInParent(),
            "caret movement must not move the text node at all");
        assertEquals("q", caretChar.getText(),
            "Ctrl+B puts the block (and the inverse-video overlay) on the 'q'");
        assertEquals("wq", controller.command.getTxt(),
            "caret movement must not change the command text");
        assertTrue(caretChar.getStyleClass().contains("info-caret-char"),
            "the overlay carries the inverse-video style");
        assertTrue(caretChar.isVisible(),
            "the overlay shows while the blink phase is on");
        assertEquals(caret.getBoundsInParent().getMinX(),
            caretChar.getBoundsInParent().getMinX(), 1.0,
            "the overlay sits exactly on the block");
        // The block sits over the 'q': one character in from the end.
        assertEquals(lineBoundsBefore.getMaxX() - charW,
            caret.getBoundsInParent().getMinX(), 1.0,
            "the block must cover the caret character's cell");

        // Mid-line insert: the block stays on the same character.
        robot.type(KeyCode.X);
        assertEquals(":wxq", line.getText());
        assertEquals("q", caretChar.getText());
        assertEquals("wxq", controller.command.getTxt());

        // Executing the (unknown) command fails; the error message replaces
        // the command line and the caret disappears with it.
        robot.type(KeyCode.ENTER);
        assertEquals(Mode.NORMAL, controller.currMode);
        assertFalse(caret.isVisible(),
            "error display must hide the caret");
        assertFalse(caretChar.isVisible(),
            "error display must hide the inverse-video overlay");
        assertEquals("", caretChar.getText(),
            "the overlay text is cleared with the caret");
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

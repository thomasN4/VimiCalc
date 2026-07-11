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
 * TestFX checks that the help menu is a scene-graph overlay (issue #45):
 * open with {@code :help}, scroll with j/k, dismiss with ESC — without
 * disturbing the cell selector.
 *
 * <p>Key events are delivered by calling {@link Controller#onKeyPressed} with
 * synthetic events rather than OS-level key injection.</p>
 */
@ExtendWith(ApplicationExtension.class)
class HelpMenuUiTest {

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
        if (code == KeyCode.SEMICOLON) text = ";";
        if (code == KeyCode.H) text = "h";
        if (code == KeyCode.E) text = "e";
        if (code == KeyCode.L) text = "l";
        if (code == KeyCode.P) text = "p";
        if (code == KeyCode.J) text = "j";
        if (code == KeyCode.K) text = "k";
        press(robot, code, text);
    }

    private void openHelp(FxRobot robot) {
        // App uses ';' for command mode (not ':')
        press(robot, KeyCode.SEMICOLON);
        press(robot, KeyCode.H);
        press(robot, KeyCode.E);
        press(robot, KeyCode.L);
        press(robot, KeyCode.P);
        press(robot, KeyCode.ENTER, "\n");
    }

    @Test
    void helpLabelHiddenAtStartup() {
        Label help = label("#helpLabel");
        assertNotNull(help, "help overlay label must be present");
        assertFalse(help.isVisible(), "help label starts hidden");
        assertNotEquals(Mode.HELP, controller.currMode);
    }

    @Test
    void helpCommandShowsOverlayAndEscHidesWithoutMovingCursor(FxRobot robot) {
        int xBefore = controller.cellSelector.getXCoord();
        int yBefore = controller.cellSelector.getYCoord();

        openHelp(robot);

        assertEquals(Mode.HELP, controller.currMode);
        Label help = label("#helpLabel");
        assertTrue(help.isVisible(), "help label should be visible after :help");
        assertFalse(help.getText().isEmpty(), "help text should be populated");

        String pctTop = controller.helpMenu.percentage();
        assertEquals("0%", pctTop, "help starts at top of document");

        press(robot, KeyCode.J);
        String pctAfterJ = controller.helpMenu.percentage();
        assertNotEquals(pctTop, pctAfterJ, "j should advance scroll percentage");

        press(robot, KeyCode.K);
        assertEquals(pctTop, controller.helpMenu.percentage(),
            "k should scroll back to previous percentage");

        press(robot, KeyCode.ESCAPE);

        assertEquals(Mode.NORMAL, controller.currMode);
        assertFalse(help.isVisible(), "help label should hide on ESC");
        assertEquals(xBefore, controller.cellSelector.getXCoord(),
            "ESC from help must not move the cell selector X");
        assertEquals(yBefore, controller.cellSelector.getYCoord(),
            "ESC from help must not move the cell selector Y");
    }

    @Test
    void shortHelpCommandAlsoOpensOverlay(FxRobot robot) {
        press(robot, KeyCode.SEMICOLON);
        press(robot, KeyCode.H);
        press(robot, KeyCode.ENTER, "\n");

        assertEquals(Mode.HELP, controller.currMode);
        assertTrue(label("#helpLabel").isVisible());
    }
}

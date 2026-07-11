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
 * TestFX checks that the help menu is a scene-graph overlay (issue #45):
 * open with {@code :help}, scroll with j/k, dismiss with ESC — without
 * disturbing the cell selector.
 *
 * <p>Keys are injected with {@link FxRobot#type(KeyCode...)} through the
 * scene-level key handler, like the other UI tests. Run headlessly (Monocle
 * or Xvfb); robot input is unreliable against a live display.</p>
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

    private void openHelp(FxRobot robot) {
        // App uses ';' for command mode (not ':')
        robot.type(KeyCode.SEMICOLON);
        robot.type(KeyCode.H, KeyCode.E, KeyCode.L, KeyCode.P);
        robot.type(KeyCode.ENTER);
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
        // managed=false would flip isVisible without laying out — bounds prove it's on screen
        assertTrue(help.getBoundsInParent().getWidth() > 700,
            "help label should be laid out at near-pref width, not a zero-size flag flip: "
                + help.getBoundsInParent());

        String pctTop = controller.helpMenu.percentage();
        assertEquals("0%", pctTop, "help starts at top of document");

        robot.type(KeyCode.J);
        String pctAfterJ = controller.helpMenu.percentage();
        assertNotEquals(pctTop, pctAfterJ, "j should advance scroll percentage");

        robot.type(KeyCode.K);
        assertEquals(pctTop, controller.helpMenu.percentage(),
            "k should scroll back to previous percentage");

        robot.type(KeyCode.ESCAPE);

        assertEquals(Mode.NORMAL, controller.currMode);
        assertFalse(help.isVisible(), "help label should hide on ESC");
        assertEquals(xBefore, controller.cellSelector.getXCoord(),
            "ESC from help must not move the cell selector X");
        assertEquals(yBefore, controller.cellSelector.getYCoord(),
            "ESC from help must not move the cell selector Y");
    }

    @Test
    void shortHelpCommandAlsoOpensOverlay(FxRobot robot) {
        robot.type(KeyCode.SEMICOLON);
        robot.type(KeyCode.H);
        robot.type(KeyCode.ENTER);

        assertEquals(Mode.HELP, controller.currMode);
        assertTrue(label("#helpLabel").isVisible());
    }
}

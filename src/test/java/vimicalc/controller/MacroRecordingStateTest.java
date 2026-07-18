package vimicalc.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vimicalc.view.InfoBar;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Display-free tests for macro-recording state on {@link KeyCommand}. The
 * register name is retained while recording (issue #70) so the view can show
 * a persistent recording indicator; visual behavior is covered by
 * {@code ChromeLabelsUiTest} and the manual test plan.
 */
class MacroRecordingStateTest {

    private Controller ctrl;
    private KeyCommand kc;

    @BeforeEach
    void setUp() {
        // Minimal controller: only the fields the `q` branches touch. The
        // recording indicator stays null — Controller's show/hide helpers
        // must tolerate that (headless usage). InfoBar is stubbed because
        // constructing real scene-graph nodes needs the JavaFX toolkit.
        ctrl = new Controller();
        ctrl.macros = new HashMap<>();
        ctrl.infoBar = new InfoBar(null, null, null, null, null) {
            @Override
            public void setInfobarTxt(String infobarTxt) { }
        };
        kc = new KeyCommand(null, ctrl);
    }

    private static KeyEvent key(String letter) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, letter, letter,
            KeyCode.getKeyCode(letter.toUpperCase()), false, false, false, false);
    }

    @Test
    void startingARecordingRetainsTheRegisterName() {
        kc.evaluate("qa");
        assertTrue(kc.recordingMacro);
        assertEquals('a', kc.recordingMacroName);
        assertTrue(ctrl.macros.containsKey('a'), "register must be created");
    }

    @Test
    void stoppingARecordingClearsTheRecordingFlag() {
        kc.evaluate("qb");
        // The stop `q` keypress is appended to the macro before evaluation
        // (Controller.onKeyPressed); simulate it so the stop branch can drop it.
        kc.currMacro.add(key("q"));
        kc.evaluate("q");
        assertFalse(kc.recordingMacro);
    }

    @Test
    void bareQDoesNotStartARecording() {
        kc.evaluate("q");
        assertFalse(kc.recordingMacro);
        assertTrue(ctrl.macros.isEmpty());
    }
}

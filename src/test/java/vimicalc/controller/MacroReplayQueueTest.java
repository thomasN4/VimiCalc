package vimicalc.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Display-free tests for the paced macro replay queue. The queue's expansion
 * order is what makes nested {@code @x} invocations run in place (like Vim's
 * typeahead); the Timeline drainer itself needs a running toolkit and is
 * covered by the manual test plan instead.
 */
class MacroReplayQueueTest {

    private final KeyCommand kc = new KeyCommand(null, null);

    private static KeyEvent key(String letter) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, letter, letter,
            KeyCode.getKeyCode(letter.toUpperCase()), false, false, false, false);
    }

    private String queueTexts() {
        StringBuilder sb = new StringBuilder();
        for (KeyEvent e : kc.replayQueue) sb.append(e.getText());
        return sb.toString();
    }

    @Test
    void notReplayingInitially() {
        assertFalse(kc.isReplaying());
        assertTrue(kc.replayQueue.isEmpty());
    }

    @Test
    void enqueueAtFrontPreservesMacroOrder() {
        kc.enqueueAtFront(new KeyEvent[]{key("a"), key("b"), key("c")});
        assertEquals("abc", queueTexts());
    }

    @Test
    void nestedMacroExpandsBeforeRemainingOuterEvents() {
        // Outer macro's remaining events are already queued...
        kc.enqueueAtFront(new KeyEvent[]{key("x"), key("y")});
        // ...then a nested @-invocation expands in place, at the front.
        kc.enqueueAtFront(new KeyEvent[]{key("a"), key("b")});
        assertEquals("abxy", queueTexts());
    }
}

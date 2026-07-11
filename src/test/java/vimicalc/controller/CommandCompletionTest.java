package vimicalc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vimicalc.model.Command;

import static org.junit.jupiter.api.Assertions.*;

class CommandCompletionTest {

    private CommandCompletion completion;

    @BeforeEach
    void setUp() {
        completion = new CommandCompletion();
    }

    // ── Cycling forward ──

    @Nested
    class NextTests {
        @Test
        void cyclesAlphabeticallyThenReturnsToPrefix() {
            assertEquals("resCol", completion.next("res"));
            assertEquals("resRow", completion.next("resCol"));
            assertEquals("res", completion.next("resRow"));
            assertEquals("resCol", completion.next("res"));
        }

        @Test
        void exactCommandNameStillCyclesItsExtensions() {
            assertEquals("w", completion.next("w"));
            assertEquals("wq", completion.next("w"));
            assertEquals("w", completion.next("wq"));
        }

        @Test
        void emptyPrefixMatchesEveryCommand() {
            assertNotEquals("", completion.next(""));
            int cycled = 1;
            while (!completion.next("").equals("")) cycled++;
            assertEquals(Command.COMMAND_NAMES.size(), cycled,
                "Cycling from an empty prefix should visit every command once");
        }

        @Test
        void noMatchReturnsInputUnchanged() {
            assertEquals("xyz", completion.next("xyz"));
            assertEquals("xyz", completion.next("xyz"));
            assertTrue(completion.getMatches().isEmpty());
        }
    }

    // ── Cycling backward ──

    @Nested
    class PreviousTests {
        @Test
        void freshSessionStartsAtLastMatch() {
            assertEquals("resRow", completion.previous("res"));
            assertEquals("resCol", completion.previous("resRow"));
            assertEquals("res", completion.previous("resCol"));
        }

        @Test
        void previousUndoesNext() {
            assertEquals("resCol", completion.next("res"));
            assertEquals("resRow", completion.next("resCol"));
            assertEquals("resCol", completion.previous("resRow"));
        }
    }

    // ── Session state ──

    @Nested
    class SessionTests {
        @Test
        void inactiveUntilFirstCycle() {
            assertFalse(completion.isActive());
            completion.next("res");
            assertTrue(completion.isActive());
        }

        @Test
        void resetStartsANewSessionWithANewPrefix() {
            assertEquals("resCol", completion.next("res"));
            completion.reset();
            assertFalse(completion.isActive());
            assertEquals("fontSize", completion.next("font"));
            assertEquals("fontWeight", completion.next("fontSize"));
        }

        @Test
        void selectedIndexTracksTheCycle() {
            completion.next("res");
            assertEquals(0, completion.getSelectedIndex());
            completion.next("resCol");
            assertEquals(1, completion.getSelectedIndex());
            completion.next("resRow");
            assertEquals(-1, completion.getSelectedIndex(),
                "Back on the original prefix, no match is selected");
        }

        @Test
        void matchesAreSortedAlphabetically() {
            completion.next("res");
            assertEquals(java.util.List.of("resCol", "resRow"), completion.getMatches());
        }
    }
}

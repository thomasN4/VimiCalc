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
            assertEquals("fontSize", completion.next("font"));
            assertEquals("fontWeight", completion.next("fontSize"));
            assertEquals("font", completion.next("fontWeight"));
            assertEquals("fontSize", completion.next("font"));
        }

        @Test
        void aliasQueryCyclesThroughFuzzyMatchesOfCanonicalNames() {
            // write/writeQuit by prefix, fontWeight by hump, resizeRow by its
            // scattered trailing w — then back to the typed "w".
            assertEquals("write", completion.next("w"));
            assertEquals("writeQuit", completion.next("write"));
            assertEquals("fontWeight", completion.next("writeQuit"));
            assertEquals("resizeRow", completion.next("fontWeight"));
            assertEquals("w", completion.next("resizeRow"));
        }

        @Test
        void emptyPrefixMatchesEveryCanonicalCommand() {
            assertNotEquals("", completion.next(""));
            int cycled = 1;
            while (!completion.next("").equals("")) cycled++;
            assertEquals(Command.CANONICAL_COMMAND_NAMES.size(), cycled,
                "Cycling from an empty prefix should visit every canonical command once");
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
            assertEquals("fontWeight", completion.previous("font"));
            assertEquals("fontSize", completion.previous("fontWeight"));
            assertEquals("font", completion.previous("fontSize"));
        }

        @Test
        void previousUndoesNext() {
            assertEquals("resizeColumn", completion.next("res"));
            assertEquals("resizeRow", completion.next("resizeColumn"));
            assertEquals("resizeColumn", completion.previous("resizeRow"));
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
            assertEquals("resizeColumn", completion.next("res"));
            completion.reset();
            assertFalse(completion.isActive());
            assertEquals("fontSize", completion.next("font"));
            assertEquals("fontWeight", completion.next("fontSize"));
        }

        @Test
        void selectedIndexTracksTheCycle() {
            completion.next("font");
            assertEquals(0, completion.getSelectedIndex());
            completion.next("fontSize");
            assertEquals(1, completion.getSelectedIndex());
            completion.next("fontWeight");
            assertEquals(-1, completion.getSelectedIndex(),
                "Back on the original prefix, no match is selected");
        }

        @Test
        void matchesAreRankedTierThenAlphabetically() {
            completion.next("res");
            assertEquals(
                java.util.List.of("resizeColumn", "resizeRow", "gridlines", "purgeDependencies"),
                completion.getMatches());
        }
    }

    // ── Live updates while typing ──

    @Nested
    class UpdateTests {
        @Test
        void updateStartsASessionWithNoSelection() {
            completion.update("re");
            assertTrue(completion.isActive());
            assertFalse(completion.getMatches().isEmpty());
            assertEquals(-1, completion.getSelectedIndex(),
                "Typing filters the matches but selects none of them");
        }

        @Test
        void updateNarrowsAsMoreIsTyped() {
            completion.update("re");
            int broad = completion.getMatches().size();
            completion.update("res");
            assertTrue(completion.getMatches().size() < broad);
            assertEquals("resizeColumn", completion.getMatches().get(0));
        }

        @Test
        void updateResetsTheCyclePosition() {
            assertEquals("fontSize", completion.next("font"));
            completion.update("font");
            assertEquals(-1, completion.getSelectedIndex());
            assertEquals("fontSize", completion.next("font"),
                "After an update the cycle starts over from the first match");
        }

        @Test
        void suggestionsAreCanonicalNamesOnly() {
            completion.update("");
            assertEquals(Command.CANONICAL_COMMAND_NAMES.size(), completion.getMatches().size());
            for (String alias : java.util.List.of("h", "?", "e", "w", "wq", "q",
                    "resCol", "resRow", "purgeDeps", "txtColor", "boldTxt", "italicTxt"))
                assertFalse(completion.getMatches().contains(alias),
                    "Alias \"" + alias + "\" must not be suggested");
        }
    }
}

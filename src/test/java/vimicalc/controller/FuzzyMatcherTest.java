package vimicalc.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vimicalc.model.Command;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatcherTest {

    // ── Tier classification ──

    @Nested
    class TierTests {
        @Test
        void prefixIsTierZero() {
            assertEquals(0, FuzzyMatcher.tier("res", "resizeColumn"));
            assertEquals(0, FuzzyMatcher.tier("RES", "resizeColumn"));
            assertEquals(0, FuzzyMatcher.tier("", "anything"));
        }

        @Test
        void camelHumpIsTierOne() {
            assertEquals(1, FuzzyMatcher.tier("rc", "resizeColumn"));
            assertEquals(1, FuzzyMatcher.tier("rC", "resizeColumn"));
            assertEquals(1, FuzzyMatcher.tier("fW", "fontWeight"));
            assertEquals(1, FuzzyMatcher.tier("wq", "writeQuit"));
        }

        @Test
        void humpsMayExtendIntoContiguousRuns() {
            // "resc" is not a prefix of resizeColumn, but r-e-s ride the word
            // start and c lands on the C hump.
            assertEquals(1, FuzzyMatcher.tier("rescol", "resizeColumn"));
        }

        @Test
        void scatteredSubsequenceIsTierTwo() {
            assertEquals(2, FuzzyMatcher.tier("res", "gridlines"));
            assertEquals(2, FuzzyMatcher.tier("rc", "purgeDependencies"));
        }

        @Test
        void nonSubsequenceIsNoMatch() {
            assertEquals(-1, FuzzyMatcher.tier("xyz", "resizeColumn"));
            assertEquals(-1, FuzzyMatcher.tier("wqx", "writeQuit"));
        }
    }

    // ── Ranking over the real command set ──

    @Nested
    class RankTests {
        @Test
        void prefixBeatsHumpBeatsScattered() {
            assertEquals(
                List.of("resizeColumn", "resizeRow", "gridlines", "purgeDependencies"),
                FuzzyMatcher.rank("res", Command.CANONICAL_COMMAND_NAMES));
        }

        @Test
        void humpMatchesRankBelowPrefixMatches() {
            // write/writeQuit by prefix, fontWeight by hump, resizeRow by
            // its scattered trailing w.
            assertEquals(
                List.of("write", "writeQuit", "fontWeight", "resizeRow"),
                FuzzyMatcher.rank("w", Command.CANONICAL_COMMAND_NAMES));
        }

        @Test
        void alphabeticalWithinATier() {
            assertEquals(
                List.of("fontSize", "fontWeight"),
                FuzzyMatcher.rank("font", Command.CANONICAL_COMMAND_NAMES));
        }

        @Test
        void emptyQueryReturnsEveryCandidateAlphabetically() {
            List<String> ranked = FuzzyMatcher.rank("", Command.CANONICAL_COMMAND_NAMES);
            assertEquals(Command.CANONICAL_COMMAND_NAMES.size(), ranked.size());
            assertEquals(ranked.stream().sorted().toList(), ranked);
        }

        @Test
        void noMatchesYieldsEmptyList() {
            assertTrue(FuzzyMatcher.rank("xyz", Command.CANONICAL_COMMAND_NAMES).isEmpty());
        }
    }
}

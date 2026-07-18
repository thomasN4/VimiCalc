package vimicalc.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static vimicalc.model.Settings.CompletionNames.LONG;
import static vimicalc.model.Settings.CompletionNames.SHORT;

/**
 * Unit tests for {@link Settings}: the {@code vimicalcrc} parser, the
 * candidate-path resolution, and file loading (issue #76).
 */
class SettingsTest {

    /** Parses the given lines, asserting no warnings were produced. */
    private static Settings parseClean(String... lines) {
        Settings settings = Settings.parse(List.of(lines));
        assertEquals(List.of(), settings.getWarnings());
        return settings;
    }

    // ── Defaults ──

    @Nested
    class DefaultsTests {

        @Test
        void defaultsFactoryYieldsAllDefaults() {
            Settings settings = Settings.defaults();
            assertEquals(LONG, settings.getCompletionNames());
            assertEquals(0, settings.getMacroDelayMs());
            assertTrue(settings.gridlinesOn());
            assertEquals(1.0, settings.getZoom());
            assertEquals(List.of(), settings.getWarnings());
        }

        @Test
        void emptyContentYieldsAllDefaults() {
            Settings settings = parseClean();
            assertEquals(LONG, settings.getCompletionNames());
            assertEquals(0, settings.getMacroDelayMs());
            assertTrue(settings.gridlinesOn());
            assertEquals(1.0, settings.getZoom());
        }
    }

    // ── Grammar ──

    @Nested
    class GrammarTests {

        @Test
        void blankAndWhitespaceLinesAreIgnored() {
            parseClean("", "   ", "\t");
        }

        @Test
        void fullLineCommentsAreIgnored() {
            parseClean("\" VimiCalc configuration", "  \" indented comment");
        }

        @Test
        void trailingCommentsAreStripped() {
            Settings settings = parseClean("set gridlines off \" hide the grid");
            assertFalse(settings.gridlinesOn());
        }

        @Test
        void missingValueWarnsWithLineNumber() {
            Settings settings = Settings.parse(List.of("set macroDelay"));
            assertEquals(List.of("line 1: expected 'set <key> <value>'"), settings.getWarnings());
        }

        @Test
        void extraTokensWarn() {
            Settings settings = Settings.parse(List.of("set macroDelay 100 200"));
            assertEquals(1, settings.getWarnings().size());
            assertEquals(0, settings.getMacroDelayMs());
        }

        @Test
        void nonSetDirectiveWarns() {
            Settings settings = Settings.parse(List.of("let zoom 2.0"));
            assertEquals(List.of("line 1: expected 'set <key> <value>'"), settings.getWarnings());
        }

        @Test
        void parsingContinuesPastBadLines() {
            Settings settings = Settings.parse(List.of("nonsense", "set zoom 2.0"));
            assertEquals(1, settings.getWarnings().size());
            assertEquals(2.0, settings.getZoom());
        }

        @Test
        void warningsReportOneBasedLineNumbers() {
            Settings settings = Settings.parse(List.of("set zoom 2.0", "", "bad line"));
            assertEquals(List.of("line 3: expected 'set <key> <value>'"), settings.getWarnings());
        }
    }

    // ── Keys: valid values ──

    @Nested
    class KeyTests {

        @Test
        void completionNamesLongAndShort() {
            assertEquals(LONG, parseClean("set completionNames long").getCompletionNames());
            assertEquals(SHORT, parseClean("set completionNames short").getCompletionNames());
        }

        @Test
        void macroDelayAcceptsBounds() {
            assertEquals(0, parseClean("set macroDelay 0").getMacroDelayMs());
            assertEquals(10000, parseClean("set macroDelay 10000").getMacroDelayMs());
            assertEquals(100, parseClean("set macroDelay 100").getMacroDelayMs());
        }

        @Test
        void gridlinesOnAndOff() {
            assertTrue(parseClean("set gridlines on").gridlinesOn());
            assertFalse(parseClean("set gridlines off").gridlinesOn());
        }

        @Test
        void zoomAcceptsBounds() {
            assertEquals(0.25, parseClean("set zoom 0.25").getZoom());
            assertEquals(4.0, parseClean("set zoom 4.0").getZoom());
            assertEquals(1.5, parseClean("set zoom 1.5").getZoom());
        }

        @Test
        void duplicateKeyKeepsTheLastValueSilently() {
            Settings settings = parseClean("set macroDelay 100", "set macroDelay 200");
            assertEquals(200, settings.getMacroDelayMs());
        }
    }

    // ── Keys: invalid values fall back to defaults (no clamping) ──

    @Nested
    class RejectionTests {

        /** Asserts the line produces exactly one warning and leaves all defaults. */
        private Settings assertRejected(String line) {
            Settings settings = Settings.parse(List.of(line));
            assertEquals(1, settings.getWarnings().size(), "expected one warning for: " + line);
            assertEquals(LONG, settings.getCompletionNames());
            assertEquals(0, settings.getMacroDelayMs());
            assertTrue(settings.gridlinesOn());
            assertEquals(1.0, settings.getZoom());
            return settings;
        }

        @Test
        void completionNamesRejectsUnknownStyle() {
            assertRejected("set completionNames medium");
        }

        @Test
        void macroDelayRejectsNonInteger() {
            assertRejected("set macroDelay fast");
            assertRejected("set macroDelay 1.5");
        }

        @Test
        void macroDelayRejectsOutOfRange() {
            assertRejected("set macroDelay -1");
            assertRejected("set macroDelay 10001");
        }

        @Test
        void gridlinesRejectsNonToggleWord() {
            assertRejected("set gridlines true");
        }

        @Test
        void zoomRejectsNonNumeric() {
            assertRejected("set zoom big");
        }

        @Test
        void zoomRejectsOutOfRangeWithoutClamping() {
            // 150 is a valid :zoom percentage but not a valid config factor;
            // it must fall back to 1.0, not clamp to 4.0.
            Settings settings = assertRejected("set zoom 150");
            assertTrue(settings.getWarnings().get(0).contains("factor"));
            assertRejected("set zoom 0.1");
        }

        @Test
        void unknownKeyWarnsAndParsingContinues() {
            Settings settings = Settings.parse(List.of("set theme dark", "set zoom 2.0"));
            assertEquals(List.of("line 1: unknown key 'theme'"), settings.getWarnings());
            assertEquals(2.0, settings.getZoom());
        }
    }

    // ── Candidate-path resolution ──

    @Nested
    class PathTests {

        private final Path home = Path.of("/home/someone");

        @Test
        void xdgConfigHomeTakesPriority() {
            List<Path> candidates = Settings.candidatePaths("/custom/xdg", home);
            assertEquals(Path.of("/custom/xdg/vimicalc/vimicalcrc"), candidates.get(0));
        }

        @Test
        void unsetXdgConfigHomeFallsBackToDotConfig() {
            assertEquals(home.resolve(".config/vimicalc/vimicalcrc"),
                         Settings.candidatePaths(null, home).get(0));
            assertEquals(home.resolve(".config/vimicalc/vimicalcrc"),
                         Settings.candidatePaths("  ", home).get(0));
        }

        @Test
        void homeDotFileIsAlwaysTheSecondCandidate() {
            assertEquals(home.resolve(".vimicalc"),
                         Settings.candidatePaths("/custom/xdg", home).get(1));
            assertEquals(home.resolve(".vimicalc"),
                         Settings.candidatePaths(null, home).get(1));
        }
    }

    // ── File loading ──

    @Nested
    class LoadingTests {

        @TempDir
        Path tempDir;

        private Path xdgFile;
        private Path fallbackFile;

        private List<Path> candidates() {
            xdgFile = tempDir.resolve("xdg/vimicalc/vimicalcrc");
            fallbackFile = tempDir.resolve(".vimicalc");
            return List.of(xdgFile, fallbackFile);
        }

        @Test
        void firstExistingCandidateWins() throws Exception {
            List<Path> candidates = candidates();
            Files.createDirectories(xdgFile.getParent());
            Files.write(xdgFile, List.of("set zoom 2.0"));
            Files.write(fallbackFile, List.of("set zoom 3.0"));
            assertEquals(2.0, Settings.loadFrom(candidates).getZoom());
        }

        @Test
        void fallbackIsUsedWhenXdgFileIsAbsent() throws Exception {
            List<Path> candidates = candidates();
            Files.write(fallbackFile, List.of("set gridlines off"));
            assertFalse(Settings.loadFrom(candidates).gridlinesOn());
        }

        @Test
        void noFileYieldsSilentDefaultsAndCreatesNothing() {
            List<Path> candidates = candidates();
            Settings settings = Settings.loadFrom(candidates);
            assertEquals(1.0, settings.getZoom());
            assertEquals(List.of(), settings.getWarnings());
            assertTrue(Files.notExists(xdgFile));
            assertTrue(Files.notExists(fallbackFile));
        }
    }
}

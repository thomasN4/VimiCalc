package vimicalc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Sheet();
    }

    private void run(String commandTxt) throws Exception {
        new Command(commandTxt, 2, 3).interpret(sheet);
    }

    // ── :fontSize ──

    @Nested
    class FontSizeTests {
        @Test
        void setsFontSizeOnCell() throws Exception {
            run("fontSize 18");
            Formatting f = sheet.findFormatting(2, 3);
            assertNotNull(f);
            assertEquals(18, f.getFontSize());
        }

        @Test
        void noArgumentResetsAndRemovesDefaultEntry() throws Exception {
            run("fontSize 18");
            run("fontSize");
            assertNull(sheet.findFormatting(2, 3));
        }

        @Test
        void outOfRangeSizeThrows() {
            Exception e = assertThrows(Exception.class, () -> run("fontSize 500"));
            assertTrue(e.getMessage().contains("fontSize"));
        }

        @Test
        void nonNumericSizeThrows() {
            assertThrows(Exception.class, () -> run("fontSize huge"));
        }
    }

    // ── :fontWeight ──

    @Nested
    class FontWeightTests {
        @Test
        void setsNamedWeight() throws Exception {
            run("fontWeight bold");
            assertEquals("bold", sheet.findFormatting(2, 3).getFontWeight());
        }

        @Test
        void setsNumericWeight() throws Exception {
            run("fontWeight 700");
            assertEquals("700", sheet.findFormatting(2, 3).getFontWeight());
        }

        @Test
        void noArgumentResetsAndRemovesDefaultEntry() throws Exception {
            run("fontWeight bold");
            run("fontWeight");
            assertNull(sheet.findFormatting(2, 3));
        }

        @Test
        void invalidWeightThrows() {
            assertThrows(Exception.class, () -> run("fontWeight banana"));
            assertThrows(Exception.class, () -> run("fontWeight 950"));
        }
    }

    // ── :italicTxt ──

    @Nested
    class ItalicTxtTests {
        @Test
        void togglesItalicOn() throws Exception {
            run("italicTxt");
            assertEquals("italic", sheet.findFormatting(2, 3).getFontPosture());
        }

        @Test
        void togglingTwiceRemovesDefaultEntry() throws Exception {
            run("italicTxt");
            run("italicTxt");
            assertNull(sheet.findFormatting(2, 3));
        }
    }

    // ── :cellColor / :txtColor ──

    @Nested
    class ColorCommandTests {
        @Test
        void hexColorIsApplied() throws Exception {
            run("cellColor #ff8800");
            assertArrayEquals(new short[]{255, 136, 0}, sheet.findFormatting(2, 3).getCellColor());
        }

        @Test
        void cssNameIsApplied() throws Exception {
            run("txtColor crimson");
            assertArrayEquals(new short[]{220, 20, 60}, sheet.findFormatting(2, 3).getTxtColor());
        }

        @Test
        void unknownColorResetsAndRemovesDefaultEntry() throws Exception {
            run("cellColor teal");
            run("cellColor notacolor");
            assertNull(sheet.findFormatting(2, 3));
        }
    }

    // ── :zoom ──

    @Nested
    class ZoomTests {
        @BeforeEach
        void setUpPositions() {
            // :zoom acts on the sheet's Positions, which the bare Sheet from
            // the outer setUp doesn't have.
            sheet.setPositions(new Positions(852, 552, 96, 24, new HashMap<>(), new HashMap<>()));
            sheet.getPositions().generate(48, 24);
        }

        @Test
        void setsZoomFromPercent() throws Exception {
            run("zoom 150");
            assertEquals(1.5, sheet.getPositions().getZoom());
        }

        @Test
        void noArgumentResetsToDefault() throws Exception {
            run("zoom 150");
            run("zoom");
            assertEquals(1.0, sheet.getPositions().getZoom());
        }

        @Test
        void outOfRangePercentThrows() {
            Exception low = assertThrows(Exception.class, () -> run("zoom 10"));
            assertTrue(low.getMessage().contains("zoom"));
            Exception high = assertThrows(Exception.class, () -> run("zoom 500"));
            assertTrue(high.getMessage().contains("zoom"));
        }

        @Test
        void nonNumericPercentThrows() {
            assertThrows(Exception.class, () -> run("zoom huge"));
        }
    }

    // ── Unknown commands ──

    @Nested
    class UnknownCommandTests {
        @Test
        void unknownCommandThrowsAndFlags() {
            Command c = new Command("frobnicate", 1, 1);
            assertThrows(Exception.class, () -> c.interpret(sheet));
            assertFalse(c.commandExists);
        }
    }

    // ── COMMAND_NAMES (autocompletion) ──

    @Nested
    class CommandNamesTests {
        @TempDir
        Path tempDir;

        @Test
        void everyListedNameIsRecognizedByInterpret() {
            for (String name : Command.COMMAND_NAMES) {
                // The tempDir argument keeps :w / :wq from writing into the
                // working directory; commands that reject it still count as
                // recognized (only the default branch clears commandExists).
                Command c = new Command(name + ' ' + tempDir.resolve("out"), 2, 3);
                try {
                    c.interpret(sheet);
                } catch (Exception ignored) {}
                assertTrue(c.commandExists,
                    "\"" + name + "\" is in COMMAND_NAMES but not in the interpret switch");
            }
        }
    }
}

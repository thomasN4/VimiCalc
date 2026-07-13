package vimicalc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Sheet();
    }

    private void run(String commandTxt) throws Exception {
        new Command(commandTxt, 2, 3).execute(sheet);
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

    // ── :gridlines ──

    @Nested
    class GridlinesTests {
        @BeforeEach
        void setUpPositions() {
            // :gridlines acts on the sheet's Positions, which the bare Sheet
            // from the outer setUp doesn't have.
            sheet.setPositions(new Positions(852, 552, 96, 24, new HashMap<>(), new HashMap<>()));
            sheet.getPositions().generate(48, 24);
        }

        @Test
        void gridlinesAreOnByDefault() {
            assertTrue(sheet.getPositions().gridlinesOn());
        }

        @Test
        void togglesGridlinesOff() throws Exception {
            run("gridlines");
            assertFalse(sheet.getPositions().gridlinesOn());
        }

        @Test
        void togglingTwiceRestoresGridlines() throws Exception {
            run("gridlines");
            run("gridlines");
            assertTrue(sheet.getPositions().gridlinesOn());
        }
    }

    // ── :macroDelay ──

    @Nested
    class MacroDelayTests {
        @BeforeEach
        void setUpPositions() {
            // :macroDelay acts on the sheet's Positions, which the bare Sheet
            // from the outer setUp doesn't have.
            sheet.setPositions(new Positions(852, 552, 96, 24, new HashMap<>(), new HashMap<>()));
            sheet.getPositions().generate(48, 24);
        }

        @Test
        void delayIsZeroByDefault() {
            assertEquals(0, sheet.getPositions().getMacroDelayMs());
        }

        @Test
        void setsDelayInMilliseconds() throws Exception {
            run("macroDelay 100");
            assertEquals(100, sheet.getPositions().getMacroDelayMs());
        }

        @Test
        void noArgumentResetsToInstant() throws Exception {
            run("macroDelay 100");
            run("macroDelay");
            assertEquals(0, sheet.getPositions().getMacroDelayMs());
        }

        @Test
        void outOfRangeDelayThrows() {
            Exception low = assertThrows(Exception.class, () -> run("macroDelay -5"));
            assertTrue(low.getMessage().contains("macroDelay"));
            Exception high = assertThrows(Exception.class, () -> run("macroDelay 999999"));
            assertTrue(high.getMessage().contains("macroDelay"));
        }

        @Test
        void nonNumericDelayThrows() {
            assertThrows(Exception.class, () -> run("macroDelay slow"));
        }
    }

    // ── Unknown commands ──

    @Nested
    class UnknownCommandTests {
        @Test
        void unknownCommandThrowsDoesntExist() {
            Command c = new Command("frobnicate", 1, 1);
            Exception e = assertThrows(Exception.class, () -> c.execute(sheet));
            assertTrue(e.getMessage().contains("doesn't exist"));
        }
    }

    // ── Canonical names and aliases ──

    @Nested
    class AliasTests {
        @Test
        void canonicalAndAliasRunTheSameCommand() throws Exception {
            run("boldText");
            assertEquals("bold", sheet.findFormatting(2, 3).getFontWeight());
            run("boldTxt"); // toggle back off via the alias
            assertNull(sheet.findFormatting(2, 3));

            run("italicText");
            assertEquals("italic", sheet.findFormatting(2, 3).getFontPosture());
            run("italicTxt");
            assertNull(sheet.findFormatting(2, 3));

            run("textColor crimson");
            assertArrayEquals(new short[]{220, 20, 60}, sheet.findFormatting(2, 3).getTxtColor());
        }

        @Test
        void helpAliasesAllOpenHelp() throws Exception {
            assertEquals(CommandResult.HELP, new Command("help", 2, 3).execute(sheet));
            assertEquals(CommandResult.HELP, new Command("h", 2, 3).execute(sheet));
            assertEquals(CommandResult.HELP, new Command("?", 2, 3).execute(sheet));
        }

        @Test
        void quitAliasesAllQuit() throws Exception {
            assertEquals(CommandResult.QUIT, new Command("quit", 2, 3).execute(sheet));
            assertEquals(CommandResult.QUIT, new Command("q", 2, 3).execute(sheet));
            // :writeQuit swallows the (fileless) write failure and still quits.
            assertEquals(CommandResult.QUIT, new Command("writeQuit", 2, 3).execute(sheet));
            assertEquals(CommandResult.QUIT, new Command("wq", 2, 3).execute(sheet));
        }

        @Test
        void resizeAliasesShareArityAndBehavior() throws Exception {
            sheet.setPositions(new Positions(852, 552, 96, 24, new HashMap<>(), new HashMap<>()));
            sheet.getPositions().generate(48, 24);
            run("resizeColumn 10");
            run("resCol -10");
            Exception e = assertThrows(Exception.class, () -> run("resizeColumn"));
            assertTrue(e.getMessage().contains("resizeColumn"));
        }
    }

    // ── Registry dispatch (issue #63 / #67) ──

    @Nested
    class RegistryTests {
        @Test
        void commandNamesAreDerivedFromRegistryWithoutDuplicates() {
            // Loading Command runs the registry's static init; a duplicate
            // name/alias would throw there before we ever get here.
            assertEquals(CommandRegistry.names(), Command.COMMAND_NAMES);
            assertEquals(Command.COMMAND_NAMES.size(),
                Set.copyOf(Command.COMMAND_NAMES).size(),
                "COMMAND_NAMES must not contain duplicate names or aliases");
        }

        @Test
        void canonicalNamesContainNoAliasesAndResolveToThemselves() {
            assertEquals(CommandRegistry.canonicalNames(), Command.CANONICAL_COMMAND_NAMES);
            assertTrue(Command.COMMAND_NAMES.containsAll(Command.CANONICAL_COMMAND_NAMES));
            for (String name : Command.CANONICAL_COMMAND_NAMES)
                assertEquals(name, CommandRegistry.lookup(name).name(),
                    "\"" + name + "\" should be a canonical name, not an alias");
        }

        @Test
        void everyUsageStringStartsWithItsCommandName() {
            for (String name : Command.COMMAND_NAMES) {
                CommandRegistry.CommandDef def = CommandRegistry.lookup(name);
                assertNotNull(def, "\"" + name + "\" has no registry entry");
                assertTrue(def.usage().startsWith(":" + def.name()),
                    "usage for \"" + def.name() + "\" should start with \":" + def.name()
                        + "\" but was: " + def.usage());
            }
        }

        @Test
        void missingRequiredArgumentReportsUsageInsteadOfCrashing() {
            Exception e = assertThrows(Exception.class, () -> run("e"));
            assertTrue(e.getMessage().contains("e"));
            Exception r = assertThrows(Exception.class, () -> run("resCol"));
            assertTrue(r.getMessage().contains("resCol"));
        }

        @Test
        void blankInputIsANoOp() throws Exception {
            assertEquals(CommandResult.NONE, new Command("", 2, 3).execute(sheet));
            assertEquals(CommandResult.NONE, new Command("   ", 2, 3).execute(sheet));
        }
    }
}

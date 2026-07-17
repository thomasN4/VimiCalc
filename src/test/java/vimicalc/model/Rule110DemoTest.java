package vimicalc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the blank-safe Rule 110 RPN formula and the committed demo assets.
 *
 * <p>RPN next-state formula (blank neighbors coerced to 0 via {@code 0 +}):
 * {@code k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod}
 * ≡ (C + R + C*R + L*C*R) mod 2, with L={@code kh}, C={@code k}, R={@code kl}.</p>
 *
 * <p>By default tests never write into {@code demos/}. They build sheets in
 * memory / {@code @TempDir} and assert against the committed JSON files
 * semantically. Set {@code -DupdateRule110Demos=true} to refresh the goldens
 * in {@code demos/} intentionally.</p>
 */
class Rule110DemoTest {

    static final String NEXT =
        "k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod";

    static final Path POSTER = Path.of("demos/rule110.json");
    static final Path FORMULAS = Path.of("demos/rule110-formulas.json");

    static int rule110(int L, int C, int R) {
        return (C + R + C * R + L * C * R) % 2;
    }

    @TempDir
    Path tempDir;

    @Test
    void formulaMatchesRule110TruthTable() throws Exception {
        for (int L = 0; L <= 1; L++) {
            for (int C = 0; C <= 1; C++) {
                for (int R = 0; R <= 1; R++) {
                    Sheet sheet = newSheet();
                    putNum(sheet, 1, 1, L);
                    putNum(sheet, 2, 1, C);
                    putNum(sheet, 3, 1, R);
                    Formula f = new Formula(NEXT, 2, 2);
                    double got = f.interpret(sheet);
                    assertEquals(rule110(L, C, R), (int) Math.round(got),
                        "LCR=" + L + C + R);
                }
            }
        }
    }

    @Test
    void formulaTreatsMissingEdgeNeighborsAsZero() throws Exception {
        Sheet sheet = newSheet();
        putNum(sheet, 1, 1, 1);
        Formula f = new Formula(NEXT, 1, 2);
        assertEquals(1, (int) Math.round(f.interpret(sheet)));
    }

    @Test
    void posterDemoMatchesPureCAAndCommittedFile() throws Exception {
        final int width = 48;
        final int gens = 36;
        final int seedCol = width;

        int[][] grid = pureGrid(width, gens, seedCol);

        // Formula engine vs pure CA on generation 2 (full width)
        Sheet check = newSheet();
        for (int x = 1; x <= width; x++) putNum(check, x, 1, grid[1][x]);
        for (int x = 1; x <= width; x++) {
            Formula f = new Formula(NEXT, x, 2);
            assertEquals(grid[2][x], (int) Math.round(f.interpret(check)),
                "gen2 col " + x);
        }

        Sheet sheet = buildPosterSheet(grid, width, gens);
        Path out = tempDir.resolve("rule110.json");
        assertThrows(Exception.class, () -> sheet.writeFile(out.toString()));
        assertTrue(Files.exists(out));

        if (Boolean.getBoolean("updateRule110Demos")) {
            assertThrows(Exception.class, () -> sheet.writeFile(POSTER.toString()));
        }

        assertTrue(Files.exists(POSTER), "committed poster demo missing: " + POSTER);
        Sheet committed = newSheet();
        committed.readFile(POSTER.toAbsolutePath().toString());
        assertPosterMatches(committed, grid, width, gens);

        int live = 0;
        for (int y = 1; y <= gens; y++)
            for (int x = 1; x <= width; x++)
                if (grid[y][x] == 1) live++;
        assertTrue(live > 80, "Rule 110 should produce a nontrivial pattern");
    }

    @Test
    void formulaStripMatchesEngineAndCommittedFile() throws Exception {
        final int width = 12;
        final int gens = 5;
        final int seedCol = width;

        int[][] grid = pureGrid(width, gens, seedCol);
        Sheet sheet = buildFormulaStrip(grid, width, gens);

        Path out = tempDir.resolve("rule110-formulas.json");
        assertThrows(Exception.class, () -> sheet.writeFile(out.toString()));
        assertTrue(Files.exists(out));

        if (Boolean.getBoolean("updateRule110Demos")) {
            assertThrows(Exception.class, () -> sheet.writeFile(FORMULAS.toString()));
        }

        assertTrue(Files.exists(FORMULAS), "committed formula strip missing: " + FORMULAS);
        Sheet committed = newSheet();
        committed.readFile(FORMULAS.toAbsolutePath().toString());
        assertFormulaStripMatches(committed, grid, width, gens);
    }

    private static int[][] pureGrid(int width, int gens, int seedCol) {
        int[][] grid = new int[gens + 1][width + 2];
        grid[1][seedCol] = 1;
        for (int y = 1; y < gens; y++) {
            for (int x = 1; x <= width; x++) {
                grid[y + 1][x] = rule110(grid[y][x - 1], grid[y][x], grid[y][x + 1]);
            }
        }
        return grid;
    }

    private static Sheet buildPosterSheet(int[][] grid, int width, int gens) {
        Sheet sheet = newSheet();

        Cell title = new Cell(1, gens + 2);
        title.setTxt("Rule 110 — elementary cellular automaton");
        sheet.addCell(title);

        Cell help = new Cell(1, gens + 3);
        help.setTxt("RPN: " + NEXT);
        sheet.addCell(help);

        Cell note = new Cell(1, gens + 4);
        note.setTxt("Seed: single 1 on the right edge of row 1. Each later row is the next generation.");
        sheet.addCell(note);

        for (int y = 1; y <= gens; y++) {
            for (int x = 1; x <= width; x++) {
                putNum(sheet, x, y, grid[y][x]);
            }
        }

        for (int x = 1; x <= width; x++) {
            sheet.getPositions().getxOffsets().put(x, -72);
        }
        for (int y = 1; y <= gens; y++) {
            sheet.getPositions().getyOffsets().put(y, -6);
        }

        for (int y = 1; y <= gens; y++) {
            for (int x = 1; x <= width; x++) {
                boolean live = grid[y][x] == 1;
                Formatting fmt = live
                    ? new Formatting(
                        new short[]{40, 160, 90},
                        new short[]{255, 255, 255},
                        "center", "center", "bold", "regular")
                    : new Formatting(
                        new short[]{18, 18, 24},
                        new short[]{70, 70, 80},
                        "center", "center", "normal", "regular");
                sheet.addFormatting(x, y, fmt);
            }
        }
        return sheet;
    }

    private static Sheet buildFormulaStrip(int[][] grid, int width, int gens) throws Exception {
        Sheet sheet = newSheet();

        Cell title = new Cell(1, 1);
        title.setTxt("Rule 110 live-formula strip");
        sheet.addCell(title);
        Cell help = new Cell(1, 2);
        help.setTxt("RPN: " + NEXT);
        sheet.addCell(help);

        for (int x = 1; x <= width; x++) {
            putNum(sheet, x, 4, grid[1][x]);
        }
        for (int y = 2; y <= gens; y++) {
            int row = 3 + y;
            for (int x = 1; x <= width; x++) {
                Formula f = new Formula(NEXT, x, row);
                Cell c = new Cell(x, row);
                c.setFormulaResult(f.interpret(sheet), f);
                sheet.addCell(c);
            }
        }
        return sheet;
    }

    private static void assertPosterMatches(Sheet committed, int[][] grid, int width, int gens) {
        for (int y = 1; y <= gens; y++) {
            for (int x = 1; x <= width; x++) {
                Cell c = committed.simplyFindCell(x, y);
                assertNotNull(c, "missing cell " + x + "," + y);
                assertNull(c.formula(), "poster stores values only at " + x + "," + y);
                assertEquals(Integer.toString(grid[y][x]), c.txt(),
                    "poster value mismatch at " + x + "," + y);
            }
        }
        Formatting liveFmt = committed.findFormatting(width, 1);
        assertNotNull(liveFmt, "seed live cell should be formatted");
        assertEquals(40, liveFmt.getCellColor()[0]);
        Formatting deadFmt = committed.findFormatting(1, 1);
        assertNotNull(deadFmt, "dead cell should be formatted");
        assertEquals(18, deadFmt.getCellColor()[0]);
    }

    private static void assertFormulaStripMatches(Sheet committed, int[][] grid, int width, int gens)
            throws Exception {
        for (int x = 1; x <= width; x++) {
            Cell seed = committed.simplyFindCell(x, 4);
            assertNotNull(seed);
            assertEquals(Integer.toString(grid[1][x]), seed.txt(), "seed col " + x);
        }
        // readFile re-evaluates formula cells in HashMap order, which is not
        // generation order. Re-run top-to-bottom so each gen sees the previous.
        for (int y = 2; y <= gens; y++) {
            int row = 3 + y;
            for (int x = 1; x <= width; x++) {
                Cell c = committed.simplyFindCell(x, row);
                assertNotNull(c, "missing formula cell " + x + "," + row);
                assertNotNull(c.formula(), "expected formula at " + x + "," + row);
                assertEquals(NEXT, c.formula().getTxt());
                c.setFormulaResult(c.formula().interpret(committed), c.formula());
                assertEquals(Integer.toString(grid[y][x]), c.txt(),
                    "formula result at " + x + "," + row);
            }
        }
    }

    private static Sheet newSheet() {
        Sheet sheet = new Sheet();
        sheet.setPositions(new Positions(
            852, 552, 96, 24, new HashMap<>(), new HashMap<>()));
        return sheet;
    }

    private static void putNum(Sheet sheet, int x, int y, int v) {
        Cell c = new Cell(x, y);
        c.setTxt(Integer.toString(v));
        c.correctTxt(c.txt());
        sheet.addCell(c);
    }
}

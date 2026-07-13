package vimicalc.model;

import org.junit.jupiter.api.Test;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Builds {@code demos/rule110.json}: Rule 110 elementary CA as a spreadsheet.
 *
 * <p>RPN next-state formula (blank neighbors coerced to 0 via {@code 0 +}):
 * {@code k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod}
 * ≡ (C + R + C*R + L*C*R) mod 2, with L={@code kh}, C={@code k}, R={@code kl}.</p>
 *
 * <p>The saved demo stores computed 0/1 values (not live formulas) so
 * {@code :e demos/rule110.json} opens instantly; the formula is documented in
 * the sheet legend and in {@code demos/rule110.txt}.</p>
 */
class Rule110DemoTest {

    static final String NEXT =
        "k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod";

    static int rule110(int L, int C, int R) {
        return (C + R + C * R + L * C * R) % 2;
    }

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
    void buildsDemoSheet() throws Exception {
        final int width = 48;
        final int gens = 36;
        final int seedCol = width;

        Sheet sheet = newSheet();

        Cell title = new Cell(1, gens + 2);
        title.setTxt("Rule 110 — elementary cellular automaton");
        sheet.addCell(title);

        Cell help = new Cell(1, gens + 3);
        help.setTxt("RPN: k 0 + kl 0 + + k 0 + kl 0 + * + kh 0 + k 0 + * kl 0 + * + 2 mod");
        sheet.addCell(help);

        Cell note = new Cell(1, gens + 4);
        note.setTxt("Seed: single 1 on the right edge of row 1. Each later row is the next generation.");
        sheet.addCell(note);

        int[][] grid = new int[gens + 1][width + 2];
        grid[1][seedCol] = 1;
        for (int y = 1; y < gens; y++) {
            for (int x = 1; x <= width; x++) {
                grid[y + 1][x] = rule110(grid[y][x - 1], grid[y][x], grid[y][x + 1]);
            }
        }

        // Spot-check engine vs pure CA on gen 2
        Sheet check = newSheet();
        for (int x = 1; x <= width; x++) putNum(check, x, 1, grid[1][x]);
        for (int x = 1; x <= width; x++) {
            Formula f = new Formula(NEXT, x, 2);
            assertEquals(grid[2][x], (int) Math.round(f.interpret(check)),
                "gen2 col " + x);
        }

        // Persist values only (fast :e); formula is in the legend / rule110.txt
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

        Path out = Path.of("demos/rule110.json").toAbsolutePath();
        try {
            sheet.writeFile(out.toString());
        } catch (Exception e) {
            if (e.getMessage() == null || !e.getMessage().contains("has been saved"))
                throw e;
        }
        assertTrue(out.toFile().exists());
        System.out.println("Wrote " + out + " (" + out.toFile().length() + " bytes)");

        int live = 0;
        for (int y = 1; y <= gens; y++)
            for (int x = 1; x <= width; x++)
                if (grid[y][x] == 1) live++;
        System.out.println("Live cells: " + live);
        assertTrue(live > 80, "Rule 110 should produce a nontrivial pattern");
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

    @Test
    void buildsHandbuiltStrip() throws Exception {
        final int width = 12;
        final int gens = 5;
        final int seedCol = width;
        Sheet sheet = newSheet();

        Cell title = new Cell(1, 1);
        title.setTxt("Rule 110 hand-built strip");
        sheet.addCell(title);
        Cell help = new Cell(1, 2);
        help.setTxt("RPN: " + NEXT);
        sheet.addCell(help);

        int[][] grid = new int[gens + 1][width + 2];
        grid[1][seedCol] = 1;
        for (int y = 1; y < gens; y++) {
            for (int x = 1; x <= width; x++) {
                grid[y + 1][x] = rule110(grid[y][x - 1], grid[y][x], grid[y][x + 1]);
            }
        }

        for (int x = 1; x <= width; x++) {
            putNum(sheet, x, 4, grid[1][x]);
        }
        for (int y = 2; y <= gens; y++) {
            int row = 3 + y;
            for (int x = 1; x <= width; x++) {
                Formula f = new Formula(NEXT, x, row);
                Cell c = new Cell(x, row);
                c.setFormulaResult(grid[y][x], f);
                sheet.addCell(c);
            }
        }

        Path out = Path.of("demos/rule110-handbuilt.json").toAbsolutePath();
        try {
            sheet.writeFile(out.toString());
        } catch (Exception e) {
            if (e.getMessage() == null || !e.getMessage().contains("has been saved"))
                throw e;
        }
        assertTrue(out.toFile().exists());
        System.out.println("handbuilt strip: " + out);
    }

}

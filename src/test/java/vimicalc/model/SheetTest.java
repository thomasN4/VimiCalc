package vimicalc.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SheetTest {

    private Sheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Sheet();
        Positions positions = new Positions(
            0, 0, 2000, 2000, 100, 30,
            new HashMap<>(), new HashMap<>()
        );
        positions.generate(0, 0);
        sheet.setPositions(positions);
    }

    // ── Cell lookup ──

    @Nested
    class FindCellTests {
        @Test
        void returnsExistingCell() {
            Cell c = new Cell(2, 3, "hello");
            sheet.simplyAddCell(c);
            Cell found = sheet.findCell(2, 3);
            assertEquals("hello", found.txt());
        }

        @Test
        void returnsEmptyCellForMissingCoords() {
            Cell found = sheet.findCell(99, 99);
            assertNull(found.txt());
            assertEquals(99, found.xCoord());
            assertEquals(99, found.yCoord());
        }

        @Test
        void findByStringCoords() {
            sheet.simplyAddCell(new Cell(2, 3, "test"));
            Cell found = sheet.findCell("B3");
            assertEquals("test", found.txt());
        }

        @Test
        void redirectsToMergeStart() {
            Cell start = new Cell(1, 1, "merged");
            Cell end = new Cell(2, 2);
            start.setMergeStart(true);
            start.mergeWith(end);

            Cell mid = new Cell(2, 1);
            mid.mergeWith(start); // intermediate cell points to merge start

            sheet.simplyAddCell(start);
            sheet.simplyAddCell(mid);

            // findCell for (2,1) should redirect to merge start
            Cell found = sheet.findCell(2, 1);
            assertEquals(1, found.xCoord());
            assertEquals(1, found.yCoord());
        }

        @Test
        void simplyFindCellDoesNotRedirect() {
            Cell start = new Cell(1, 1, "merged");
            Cell mid = new Cell(2, 1);
            mid.mergeWith(start);

            sheet.simplyAddCell(start);
            sheet.simplyAddCell(mid);

            Cell found = sheet.simplyFindCell(2, 1);
            assertEquals(2, found.xCoord());
        }
    }

    // ── Add and delete ──

    @Nested
    class AddDeleteTests {
        @Test
        void addThenFind() {
            sheet.simplyAddCell(new Cell(5, 5, "added"));
            assertEquals("added", sheet.findCell(5, 5).txt());
        }

        @Test
        void addOverwritesExisting() {
            sheet.simplyAddCell(new Cell(1, 1, "first"));
            sheet.simplyAddCell(new Cell(1, 1, "second"));
            assertEquals("second", sheet.findCell(1, 1).txt());
            // Should only be one cell at that position
            long count = sheet.getCells().stream()
                .filter(c -> c.xCoord() == 1 && c.yCoord() == 1)
                .count();
            assertEquals(1, count);
        }

        @Test
        void deleteRemovesCell() {
            sheet.simplyAddCell(new Cell(3, 3, "deleteme"));
            sheet.deleteCell(3, 3);
            assertNull(sheet.findCell(3, 3).txt());
        }

        @Test
        void deleteMergeStartPreservesMerge() {
            Cell start = new Cell(1, 1, "content");
            Cell end = new Cell(2, 2);
            start.setMergeStart(true);
            start.mergeWith(end);
            sheet.simplyAddCell(start);

            sheet.deleteCell(1, 1);

            Cell found = sheet.findCell(1, 1);
            // Content should be cleared but merge structure preserved
            assertTrue(found.isMergeStart());
            assertNull(found.txt());
        }
    }

    // ── Grid bounds ──

    @Nested
    class GridBoundsTests {
        @Test
        void addCellExpandsMaxXC() {
            assertEquals(0, sheet.getPositions().getMaxXC());
            sheet.simplyAddCell(new Cell(10, 1, "far"));
            assertEquals(10, sheet.getPositions().getMaxXC());
        }

        @Test
        void addCellExpandsMaxYC() {
            assertEquals(0, sheet.getPositions().getMaxYC());
            sheet.simplyAddCell(new Cell(1, 20, "far"));
            assertEquals(20, sheet.getPositions().getMaxYC());
        }
    }

    // ── Formatting ──

    @Nested
    class FormattingTests {
        @Test
        void noFormattingByDefault() {
            assertNull(sheet.findFormatting(1, 1));
        }

        @Test
        void addAndFindFormatting() {
            var f = new vimicalc.view.Formatting();
            sheet.addFormatting(2, 3, f);
            assertSame(f, sheet.findFormatting(2, 3));
        }

        @Test
        void deleteFormatting() {
            sheet.addFormatting(1, 1, new vimicalc.view.Formatting());
            sheet.deleteFormatting(1, 1);
            assertNull(sheet.findFormatting(1, 1));
        }
    }

    // ── Dependency graph ──

    @Nested
    class DependencyTests {
        @Test
        void addDependentCreatesNode() {
            sheet.addDependent(1, 1);
            assertNotNull(sheet.findDependency(1, 1));
        }

        @Test
        void noDuplicateDependents() {
            sheet.addDependent(1, 1);
            sheet.addDependent(1, 1);
            // findDependency should return the same node
            assertNotNull(sheet.findDependency(1, 1));
        }

        @Test
        void addDependedLinksTwoNodes() throws Exception {
            sheet.addDependent(2, 1); // B1 depends on something
            var dependent = sheet.findDependency(2, 1);
            sheet.addDepended(1, 1, dependent); // B1 depends on A1

            var depended = sheet.findDependency(1, 1);
            assertNotNull(depended);
            assertTrue(depended.getDependents().contains(dependent));
            assertTrue(dependent.getDependeds().contains(depended));
        }

        @Test
        void cyclicDependencyThrows() {
            sheet.addDependent(1, 1);
            sheet.addDependent(2, 1);
            var dep1 = sheet.findDependency(1, 1);
            var dep2 = sheet.findDependency(2, 1);

            // A1 depends on B1
            assertDoesNotThrow(() -> sheet.addDepended(2, 1, dep1));
            // B1 depends on A1 → cycle
            assertThrows(Exception.class, () -> sheet.addDepended(1, 1, dep2));
        }

        @Test
        void deleteDependencyRemovesNode() {
            sheet.addDependent(1, 1);
            assertNotNull(sheet.findDependency(1, 1));
            sheet.deleteDependency(1, 1);
            assertNull(sheet.findDependency(1, 1));
        }

        @Test
        void purgeDependencies() {
            sheet.addDependent(1, 1);
            sheet.addDependent(2, 2);
            sheet.purgeDependencies();
            assertNull(sheet.findDependency(1, 1));
            assertNull(sheet.findDependency(2, 2));
        }
    }

    // ── Cell merging ──

    @Nested
    class MergeTests {
        @Test
        void unmergeCellsFromStart() {
            Cell start = new Cell(1, 1, "content");
            Cell end = new Cell(2, 2);
            start.setMergeStart(true);
            start.mergeWith(end);
            end.mergeWith(start);

            // Add intermediate cells
            Cell mid1 = new Cell(2, 1);
            mid1.mergeWith(start);
            Cell mid2 = new Cell(1, 2);
            mid2.mergeWith(start);

            sheet.simplyAddCell(start);
            sheet.simplyAddCell(end);
            sheet.simplyAddCell(mid1);
            sheet.simplyAddCell(mid2);

            sheet.unmergeCells(start);

            assertFalse(start.isMergeStart());
            assertNull(start.getMergeDelimiter());
            assertNull(sheet.simplyFindCell(2, 1).getMergeDelimiter());
            assertNull(sheet.simplyFindCell(1, 2).getMergeDelimiter());
        }
    }

    // ── Dependency re-evaluation ──

    @Nested
    class DependencyEvalTests {
        @Test
        void changingCellReEvaluatesDependents() throws Exception {
            // A1 = 10
            sheet.addCell(new Cell(1, 1, 10.0));

            // B1 = A1 (formula referencing A1)
            Formula f = new Formula("A1", 2, 1);
            Cell b1 = new Cell(2, 1, f.interpret(sheet), f);
            sheet.addCell(b1);
            assertEquals(10.0, sheet.findCell(2, 1).value());

            // Change A1 to 20 — B1 should re-evaluate
            sheet.addCell(new Cell(1, 1, 20.0));
            assertEquals(20.0, sheet.findCell(2, 1).value());
        }
    }

    // ── JSON file I/O ──

    @Nested
    class JsonFileIOTests {

        @TempDir
        Path tempDir;

        private Sheet loadSheet;

        @BeforeEach
        void setUpLoadSheet() {
            loadSheet = new Sheet();
            Positions p = new Positions(
                0, 0, 2000, 2000, 100, 30,
                new HashMap<>(), new HashMap<>()
            );
            p.generate(0, 0);
            loadSheet.setPositions(p);
        }

        @Test
        void rejectNonJsonExtension() {
            assertThrows(Exception.class, () -> sheet.readFile("foo.wss"));
        }

        @Test
        void newFileForMissingPath() {
            String path = tempDir.resolve("nonexistent.json").toString();
            Exception e = assertThrows(Exception.class, () -> sheet.readFile(path));
            assertEquals("New file", e.getMessage());
        }

        @Test
        void roundTripTextCell() throws Exception {
            sheet.simplyAddCell(new Cell(1, 1, "hello"));
            String path = tempDir.resolve("test.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path)); // success message

            loadSheet.readFile(path);
            assertEquals("hello", loadSheet.findCell(1, 1).txt());
        }

        @Test
        void roundTripNumericCell() throws Exception {
            sheet.simplyAddCell(new Cell(3, 2, 42.0));
            String path = tempDir.resolve("num.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            assertEquals(42.0, loadSheet.findCell(3, 2).value());
        }

        @Test
        void roundTripFormulaCell() throws Exception {
            // A1 = 10, B1 = A1 2 *
            sheet.simplyAddCell(new Cell(1, 1, 10.0));
            Formula f = new Formula("A1 2 *", 2, 1);
            Cell b1 = new Cell(2, 1, f.interpret(sheet), f);
            sheet.addCell(b1);

            String path = tempDir.resolve("formula.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            // Formula should have been re-evaluated on load
            assertEquals(20.0, loadSheet.findCell(2, 1).value());
            assertNotNull(loadSheet.findCell(2, 1).formula());
            assertEquals("A1 2 *", loadSheet.findCell(2, 1).formula().getTxt());
        }

        @Test
        void roundTripMergedCells() throws Exception {
            Cell start = new Cell(1, 1, "merged");
            Cell end = new Cell(3, 3);
            start.setMergeStart(true);
            start.mergeWith(end);
            end.mergeWith(start);
            sheet.simplyAddCell(start);
            sheet.simplyAddCell(end);

            String path = tempDir.resolve("merge.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            Cell loadedStart = loadSheet.simplyFindCell(1, 1);
            assertTrue(loadedStart.isMergeStart());
            assertNotNull(loadedStart.getMergeDelimiter());
            assertEquals(3, loadedStart.getMergeDelimiter().xCoord());
            assertEquals(3, loadedStart.getMergeDelimiter().yCoord());
            // Merge end should point back to start
            Cell loadedEnd = loadSheet.simplyFindCell(3, 3);
            assertNotNull(loadedEnd.getMergeDelimiter());
            assertEquals(1, loadedEnd.getMergeDelimiter().xCoord());
            assertEquals(1, loadedEnd.getMergeDelimiter().yCoord());
        }

        @Test
        void roundTripFormatting() throws Exception {
            Formatting f = new Formatting(
                new short[]{255, 0, 0},
                new short[]{0, 0, 0},
                "center", "left", "bold", "regular"
            );
            sheet.addFormatting(2, 3, f);

            String path = tempDir.resolve("fmt.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            Formatting loaded = loadSheet.findFormatting(2, 3);
            assertNotNull(loaded);
            assertArrayEquals(new short[]{255, 0, 0}, loaded.getCellColor());
            assertEquals("bold", loaded.getFontWeight());
            assertEquals("left", loaded.getAlignment());
        }

        @Test
        void roundTripColumnWidths() throws Exception {
            sheet.getPositions().getxOffsets().put(3, 120);

            String path = tempDir.resolve("widths.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            assertEquals(120, loadSheet.getPositions().getxOffsets().get(3));
        }

        @Test
        void appendsJsonExtension() throws Exception {
            sheet.simplyAddCell(new Cell(1, 1, "ext"));
            String pathNoExt = tempDir.resolve("auto").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(pathNoExt));

            // File should have been created with .json appended
            assertTrue(new File(pathNoExt + ".json").exists());

            loadSheet.readFile(pathNoExt + ".json");
            assertEquals("ext", loadSheet.findCell(1, 1).txt());
        }

        @Test
        void emptySheetRoundTrip() throws Exception {
            String path = tempDir.resolve("empty.json").toString();
            assertThrows(Exception.class, () -> sheet.writeFile(path));

            loadSheet.readFile(path);
            assertTrue(loadSheet.getCells().isEmpty());
        }
    }
}

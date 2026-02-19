package vimicalc.model;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;
import vimicalc.view.Formatting;
import vimicalc.view.Positions;

import java.io.*;
import java.util.*;

import static vimicalc.controller.Controller.macros;
import static vimicalc.utils.Conversions.*;

/**
 * The core data model representing an entire spreadsheet.
 *
 * <p>Holds the list of {@link Cell}s, the {@link Dependency} graph for formula
 * re-evaluation, per-cell {@link Formatting}, and the current viewport
 * {@link Positions}. Also handles file I/O — serializing/deserializing all
 * state to/from {@code .wss} files using Java object serialization.</p>
 */
public class Sheet {
    private ArrayList<Cell> cells;
    private File file;
    private ArrayList<Dependency> dependencies;
    private Positions positions;
    private HashMap<List<Integer>, Formatting> cellsFormatting;

    /** Creates an empty sheet with no cells, dependencies, or formatting. */
    public Sheet() {
        cells = new ArrayList<>();
        dependencies = new ArrayList<>();
        file = new File("");
        cellsFormatting = new HashMap<>();
    }

    /** @return the viewport positions metadata */
    public Positions getPositions() {
        return positions;
    }

    /** @param picPositions the new positions metadata */
    public void setPositions(Positions picPositions) {
        this.positions = picPositions;
    }

    /** @return the list of all cells in the sheet */
    public ArrayList<Cell> getCells() {
        return cells;
    }

    /** @return the per-cell formatting map */
    public HashMap<List<Integer>, Formatting> getCellsFormatting() {
        return cellsFormatting;
    }

    /**
     * Finds the formatting for a cell at the given position.
     *
     * @param xC the column
     * @param yC the row
     * @return the formatting, or {@code null} if none
     */
    public Formatting findFormatting(int xC, int yC) {
        return cellsFormatting.get(List.of(xC, yC));
    }

    /**
     * Removes formatting for the cell at the given position.
     *
     * @param xC the column
     * @param yC the row
     */
    public void deleteFormatting(int xC, int yC) {
        cellsFormatting.remove(List.of(xC, yC));
    }

    /**
     * Adds formatting for the cell at the given position.
     *
     * @param xC the column
     * @param yC the row
     * @param f  the formatting to apply
     */
    public void addFormatting(int xC, int yC, Formatting f) {
        cellsFormatting.put(List.of(xC, yC), f);
    }

    /** Removes all formula dependencies from the sheet. */
    public void purgeDependencies() {
        dependencies = new ArrayList<>();
    }

    /**
     * Deletes the content of a cell at the given coordinates.
     * If the cell is a merge-start, preserves the merge structure but clears the content.
     * Also removes any associated dependency.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     */
    public void deleteCell(int xCoord, int yCoord) {
        System.out.println("Deleting a cell...");
        Cell c = findCell(xCoord, yCoord);
        if (c.isMergeStart()) {
            Cell mergeEnd = c.getMergeDelimiter();
            c = new Cell(xCoord, yCoord);
            c.setMergeStart(true);
            c.mergeWith(mergeEnd);
            int finalCxC = c.xCoord(), finalCyC = c.yCoord();
            cells.removeIf(b -> b.xCoord() == finalCxC && b.yCoord() == finalCyC);
            cells.add(c);
        }
        else cells.remove(c);
        deleteDependency(xCoord, yCoord);
    }

    /**
     * Removes the dependency at the given coordinates. If the dependency still
     * has dependents (cells that reference it), triggers re-evaluation instead
     * of full removal.
     *
     * @param xCoord the column of the dependency to remove
     * @param yCoord the row of the dependency to remove
     */
    public void deleteDependency(int xCoord, int yCoord) {
        Dependency d = findDependency(xCoord, yCoord);
        if (d != null) {
            System.out.println("Dependency found");
            d.setDependeds(new ArrayList<>());
            if (d.getDependents().size() == 0) {
                System.out.println("Removing dependency at: " + coordsIntsToStr(d.getxCoord(), d.getyCoord()));
                dependencies.remove(d);
            } else evalDependency(d);
        }
    }

    /**
     * Finds a cell by its string coordinates (e.g. "B3").
     * Delegates to {@link #findCell(int, int)} after parsing.
     *
     * @param coords the cell reference string
     * @return the cell, or a new empty cell if none exists
     */
    public Cell findCell(@NotNull String coords) {
        int[] coordsInt = coordsStrToInts(coords);
        return findCell(coordsInt[0], coordsInt[1]);
    }
    /**
     * Finds a cell by numeric coordinates. If the cell is part of a merged
     * range (but not the merge-start), returns the merge-start cell instead.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     * @return the cell, or a new empty cell if none exists at the position
     */
    public Cell findCell(int xCoord, int yCoord) {
        for (Cell c : getCells()) {
            if (c.xCoord() == xCoord && c.yCoord() == yCoord) {
                if (c.getMergeDelimiter() != null && !c.isMergeStart())
                    return c.getMergeDelimiter();
                else
                    return c;
            }
        }
        return new Cell(xCoord, yCoord);
    }
    /**
     * Finds a cell by coordinates without following merge redirections.
     * Returns the exact cell at the position, or a new empty cell.
     *
     * @param xCoord the one-based column index
     * @param yCoord the row number
     * @return the cell at the position
     */
    public Cell simplyFindCell(int xCoord, int yCoord) {
        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                return c;
        return new Cell(xCoord, yCoord);
    }

    /**
     * Unmerges all cells in the merge group that the given cell belongs to.
     * Works whether the cell is the merge-start or any cell within the range.
     *
     * @param c any cell in the merge group
     */
    public void unmergeCells(@NotNull Cell c) {
        if (c.isMergeStart())
            unmergeCells(c, c.getMergeDelimiter());
        else if (c.getMergeDelimiter() != null)
            unmergeCells(c.getMergeDelimiter(), c.getMergeDelimiter().getMergeDelimiter());
    }
    private void unmergeCells(@NotNull Cell mergeStart, @NotNull Cell mergeEnd) {
        System.out.println("Unmerging cells...");
        Cell c;
        for (int i = mergeStart.xCoord(); i <= mergeEnd.xCoord(); ++i) {
            for (int j = mergeStart.yCoord(); j <= mergeEnd.yCoord(); ++j) {
                c = simplyFindCell(i, j);
                System.out.println("i = " + i + ", j = " + j);
                System.out.println("Unmerging: " + c);
                if (!c.isMergeStart())
                    c.mergeWith(null);
                simplyAddCell(c);
            }
        }
        mergeStart.mergeWith(null);
        mergeStart.setMergeStart(false);
        cells.removeIf(Cell::isEmpty);
    }

    /**
     * Finds the dependency node at the given coordinates.
     *
     * @param x the column
     * @param y the row
     * @return the dependency, or {@code null} if none exists
     */
    public Dependency findDependency(int x, int y) {
        for (Dependency d : dependencies)
            if (d.getxCoord() == x && d.getyCoord() == y)
                return d;
        return null;
    }

    /**
     * Registers a dependency node at the given coordinates if one doesn't exist.
     *
     * @param xCoord the column
     * @param yCoord the row
     */
    public void addDependent(int xCoord, int yCoord) {
        if (findDependency(xCoord, yCoord) == null)
            dependencies.add(new Dependency(xCoord, yCoord));
    }

    private void checkForDependencyCycle(@NotNull Dependency a, Dependency b, Dependency dependent) throws Exception {
        for (Dependency c : a.getDependeds()) {
            if (c.getxCoord() == b.getxCoord() && c.getyCoord() == b.getyCoord()) {
                dependencies.remove(dependent);
                throw new Exception("Dependency cycle detected");
            } else checkForDependencyCycle(c, b, dependent);
        }
    }

    /**
     * Registers a "depended" relationship: the cell at ({@code xCoord}, {@code yCoord})
     * is referenced by the given {@code dependent}. Also checks for circular
     * dependency cycles and throws if one is detected.
     *
     * @param xCoord    the column of the cell being referenced
     * @param yCoord    the row of the cell being referenced
     * @param dependent the dependency node that references this cell
     * @throws Exception if a circular dependency is detected
     */
    public void addDepended(int xCoord, int yCoord, Dependency dependent) throws Exception {
        Dependency depended = findDependency(xCoord, yCoord);
        if (depended == null) {
            System.out.println("Depended not found");
            depended = new Dependency(xCoord, yCoord);
            depended.getDependents().add(dependent);
            dependent.getDependeds().add(depended);
            dependencies.add(depended);
        }
        else if (!dependedAlreadyAdded(dependent, depended)) {
            System.out.println("Adding depended...");
            depended.getDependents().add(dependent);
            dependent.getDependeds().add(depended);
            System.out.println("Checking for dependency cycles...");
            checkForDependencyCycle(depended, depended, dependent);
            dependencies.add(depended);
        } else System.out.println("Depended found but already added");
    }
    /**
     * Checks whether the depended is already in the dependent's list.
     *
     * @param dependent the dependency that references others
     * @param depended  the dependency being checked
     * @return {@code true} if already added
     */
    public boolean dependedAlreadyAdded(@NotNull Dependency dependent, Dependency depended) {
        for (Dependency d : dependent.getDependeds()) {
            if (d.getxCoord() == depended.getxCoord() && d.getyCoord() == depended.getyCoord()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds or replaces a cell in the sheet and triggers re-evaluation of
     * any cells that depend on this position.
     *
     * @param cell the cell to add
     */
    public void addCell(Cell cell) {
        simplyAddCell(cell);
        Dependency d = findDependency(cell.xCoord(), cell.yCoord());
        if (d != null && d.getDependents().size() != 0)
            evalDependency(d);
    }
    /**
     * Adds or replaces a cell without triggering dependency re-evaluation.
     * Expands the grid bounds if necessary.
     *
     * @param cell the cell to add
     */
    public void simplyAddCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        if (cell.xCoord() > positions.getMaxXC() || cell.yCoord() > positions.getMaxYC()) {
            if (cell.xCoord() > positions.getMaxXC()) positions.setMaxXC(cell.xCoord());
            if (cell.yCoord() > positions.getMaxYC()) positions.setMaxYC(cell.yCoord());
            positions.generate(positions.getCamAbsX(), positions.getCamAbsY());
        }
    }

    /**
     * Triggers re-evaluation of the given dependency and all its transitive
     * dependents (cells whose formulas reference this cell).
     *
     * @param d the dependency to re-evaluate
     */
    public void evalDependency(Dependency d) {
        evalDependents(d);
        cells.removeIf(Cell::isEmpty);
        System.out.println("All of the dependencies (result):");
//        dependencies.forEach(e -> System.out.println(e.log()));
    }
    private void evalDependents(@NotNull Dependency d) {
        System.out.println("Evaluating dependencies...");
        d.setToBeEvaluated();
        System.out.println(d.log());
        if (d.isReadyToBeEvaluated())
            d.evaluate(this);
        for (Dependency e : d.getDependents())
            evalDependents(e);
    }

    /**
     * Writes the sheet to the previously used file path.
     *
     * @throws Exception with a success message for display in the info bar
     */
    public void writeFile() throws Exception {
        writeFile(file.getPath());
    }

    /**
     * Serializes the entire spreadsheet state to a {@code .wss} file using
     * Java object serialization. Saves cells, dependencies, column/row offsets,
     * macros, and formatting.
     *
     * @param path the file path (appends {@code .wss} if missing)
     * @throws Exception always thrown with a success message for display in the info bar
     */
    public void writeFile(@NotNull String path) throws Exception {
        if (path.isEmpty()) return;
        if (!path.endsWith(".wss")) path += ".wss";
        System.out.println("Saving file " + path + "...");
        ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(path));

        oStream.writeUTF("\n====Cells====\n"); oStream.flush();
        oStream.writeObject(cells); oStream.flush();

        oStream.writeUTF("\n\n====Dependencies====\n"); oStream.flush();
        oStream.writeObject(dependencies); oStream.flush();

        oStream.writeUTF("\n\n====xOffsets====\n"); oStream.flush();
        oStream.writeObject(positions.getxOffsets()); oStream.flush();

        oStream.writeUTF("\n\n====yOffsets====\n"); oStream.flush();
        oStream.writeObject(positions.getyOffsets()); oStream.flush();

        oStream.writeUTF("\n\n====Macros====\n"); oStream.flush();
        oStream.writeObject(macros); oStream.flush();

        oStream.writeUTF("\n\n====Formatting====\n"); oStream.flush();
        oStream.writeObject(cellsFormatting); oStream.flush();

        file = new File(path);
        Controller.statusBar.setFilename(file.getName());
        oStream.close();

        throw new Exception("File " + path + " has been saved.");
    }

    /**
     * Deserializes a spreadsheet from a {@code .wss} file, restoring cells,
     * dependencies, column/row offsets, macros, and formatting.
     * If the file doesn't exist, initializes a new empty sheet for that path.
     *
     * @param path the file path (must end in {@code .wss})
     * @throws Exception with a message to display in the info bar
     */
    public void readFile(@NotNull String path) throws Exception {
        if (!path.endsWith(".wss")) {
            /* On va devoir afficher cela dans l'infobar, de manière optimale.
             * Je suggère d'avoir une variable statique pour le texte d'infobar, au lieu
             * de rendre tout l'infobar static. */
            String errorMessage = "File is not of .wss format";
            throw new Exception(errorMessage);
        }

        try {
            ObjectInputStream iStream = new ObjectInputStream(new FileInputStream(path));

            HashMap<Integer, Integer> newXOffsets = null;
            HashMap<Integer, Integer> newYOffsets = null;
            cells = null;
            dependencies = null;
            macros = null;
            try {
                iStream.readUTF();
                cells = (ArrayList<Cell>) iStream.readObject();
                iStream.readUTF();
                dependencies = (ArrayList<Dependency>) iStream.readObject();
                iStream.readUTF();
                newXOffsets = (HashMap<Integer, Integer>) iStream.readObject();
                iStream.readUTF();
                newYOffsets = (HashMap<Integer, Integer>) iStream.readObject();
                iStream.readUTF();
                macros = (HashMap<Character, LinkedList<KeyEvent>>) iStream.readObject();
                iStream.readUTF();
                cellsFormatting = (HashMap<List<Integer>, Formatting>) iStream.readObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println(Arrays.toString(e.getStackTrace()));
            }

            newXOffsets = (newXOffsets == null) ? new HashMap<>() : newXOffsets;
            newYOffsets = (newYOffsets == null) ? new HashMap<>() : newYOffsets;
            positions.setxOffsets(newXOffsets);
            positions.setyOffsets(newYOffsets);

            if (cells == null) cells = new ArrayList<>();
            if (dependencies == null) dependencies = new ArrayList<>();
            if (macros == null) macros = new HashMap<>();
            Controller.reset();

            iStream.close();
            file = new File(path);
            Controller.statusBar.setFilename(file.getName());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            cells = new ArrayList<>();
            dependencies = new ArrayList<>();
            macros = new HashMap<>();
            Controller.reset();
            file = new File(path);
            Controller.statusBar.setFilename(file.getName());
            throw new Exception("New file");
        }
    }
}

/**
 * A node in the formula dependency graph.
 *
 * <p>Each {@code Dependency} corresponds to a cell coordinate and tracks two
 * sets of relationships:</p>
 * <ul>
 *   <li><b>dependents</b> — cells whose formulas reference this cell
 *       (i.e. cells that need re-evaluation when this cell changes)</li>
 *   <li><b>dependeds</b> — cells that this cell's formula references
 *       (i.e. cells that must be evaluated before this one)</li>
 * </ul>
 *
 * <p>Used by {@link Sheet} to propagate formula re-evaluation in topological
 * order and to detect circular references.</p>
 */
class Dependency implements Serializable {
    final int xCoord, yCoord;
    /** Flag indicating this dependency needs re-evaluation in the current propagation pass. */
    boolean toBeEvaluated;
    /** Cells that depend on this cell (downstream in the dependency graph). */
    final ArrayList<Dependency> dependents;
    /** Cells that this cell depends on (upstream in the dependency graph). */
    ArrayList<Dependency> dependeds;

    Dependency(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        toBeEvaluated = false;
        dependents = new ArrayList<>();
        dependeds = new ArrayList<>();
    }

    int getxCoord() {
        return xCoord;
    }

    int getyCoord() {
        return yCoord;
    }

    ArrayList<Dependency> getDependeds() {
        return dependeds;
    }

    boolean isToBeEvaluated() {
        return toBeEvaluated;
    }

    ArrayList<Dependency> getDependents() {
        return dependents;
    }

    void setDependeds(ArrayList<Dependency> dependeds) {
        this.dependeds = dependeds;
    }

    void setToBeEvaluated() {
        this.toBeEvaluated = true;
    }

    /**
     * Returns {@code true} if this node is marked for evaluation and all of its
     * upstream dependencies have already been evaluated (none are still pending).
     */
    boolean isReadyToBeEvaluated() {
        boolean b = true;
        for (Dependency d : dependeds) {
            if (d.isToBeEvaluated()) {
                b = false;
                break;
            }
        }
        return toBeEvaluated & b;
    }

    /**
     * Re-evaluates the formula of the cell at this dependency's coordinates.
     * If the cell has no formula or text, it is reset to an empty cell.
     */
    void evaluate(@NotNull Sheet sheet) {
        Cell c = sheet.findCell(xCoord, yCoord);
        if (c.formula() != null) {
            try {
                c.setFormulaResult(c.formula().interpret(sheet), c.formula());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Something went very wrong.");
            }
            sheet.simplyAddCell(c);
        }
        else if (c.txt() == null) {
            sheet.getCells().removeIf(b -> b.xCoord() == c.xCoord() && b.yCoord() == c.yCoord());
            sheet.getCells().add(new Cell(xCoord, yCoord));
        }
        toBeEvaluated = false;
        System.out.println("Evaluating dependency at: " + xCoord + ", " + yCoord + "...");
    }

    String log() {
        return "Dependency: " + xCoord + ", " + yCoord + " {\n" +
               "\ttoBeEvaluated = " + toBeEvaluated + "\n" +
               "\treadyToBeEvaluated = " + isReadyToBeEvaluated() + "\n" +
               "\tdependeds: {\n" +
               "\t\t" + dependeds + "\n" +
               "\t}\n" +
               "\tdependents: {\n" +
               "\t\t" + dependents + "\n" +
               "\t}\n" +
               "}";
    }
}

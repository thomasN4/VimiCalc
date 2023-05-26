package vimicalc.model;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;
import vimicalc.view.Positions;

import java.io.*;
import java.util.*;

import static vimicalc.controller.Controller.macros;
import static vimicalc.utils.Conversions.*;

public class Sheet {
    private ArrayList<Cell> cells;
    private File file;
    private ArrayList<Dependency> dependencies;
    private Positions picPositions;

    public Sheet() {
        cells = new ArrayList<>();
        dependencies = new ArrayList<>();
        file = new File("");
    }

    public Positions getPicMetadata() {
        return picPositions;
    }

    public void setPicMetadata(Positions picPositions) {
        this.picPositions = picPositions;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void purgeDependencies() {
        dependencies = new ArrayList<>();
    }

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

    public Cell findCell(@NotNull String coords) {
        int[] coordsInt = coordsStrToInts(coords);
        return findCell(coordsInt[0], coordsInt[1]);
    }
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
    public Cell simplyFindCell(int xCoord, int yCoord) {
        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                return c;
        return new Cell(xCoord, yCoord);
    }

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

    public Dependency findDependency(int x, int y) {
        for (Dependency d : dependencies)
            if (d.getxCoord() == x && d.getyCoord() == y)
                return d;
        return null;
    }

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
    public boolean dependedAlreadyAdded(@NotNull Dependency dependent, Dependency depended) {
        for (Dependency d : dependent.getDependeds()) {
            if (d.getxCoord() == depended.getxCoord() && d.getyCoord() == depended.getyCoord()) {
                return true;
            }
        }
        return false;
    }

    public void addCell(Cell cell) {
        simplyAddCell(cell);
        Dependency d = findDependency(cell.xCoord(), cell.yCoord());
        if (d != null && d.getDependents().size() != 0)
            evalDependency(d);
    }
    public void simplyAddCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        if (cell.xCoord() > picPositions.getMaxXC() || cell.yCoord() > picPositions.getMaxYC()) {
            if (cell.xCoord() > picPositions.getMaxYC()) picPositions.setMaxXC(cell.xCoord());
            if (cell.yCoord() > picPositions.getMaxYC()) picPositions.setMaxYC(cell.yCoord());
            picPositions.generate(picPositions.getCamAbsX(), picPositions.getCamAbsY());
        }
    }

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

    public void writeFile() throws IOException {
        writeFile(file.getPath());
    }

    public void writeFile(@NotNull String path) throws IOException {
        if (path.isEmpty()) return;
        if (!path.endsWith(".wss")) path += ".wss";
        System.out.println("Saving file " + path + "...");
        ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(path));

        try {
            oStream.writeUTF("\n====Cells====\n"); oStream.flush();
            oStream.writeObject(cells); oStream.flush();

            oStream.writeUTF("\n\n====Dependencies====\n"); oStream.flush();
            oStream.writeObject(dependencies); oStream.flush();

            oStream.writeUTF("\n\n====xOffsets====\n"); oStream.flush();
            oStream.writeObject(picPositions.getxOffsets()); oStream.flush();

            oStream.writeUTF("\n\n====yOffsets====\n"); oStream.flush();
            oStream.writeObject(picPositions.getyOffsets()); oStream.flush();

            oStream.writeUTF("\n\n====Macros====\n"); oStream.flush();
            oStream.writeObject(macros); oStream.flush();

            file = new File(path);
            Controller.statusBar.setFilename(file.getName());
        } catch (Exception e) {
            System.out.println("Error while saving file: " + Arrays.toString(e.getStackTrace()));
        }
        oStream.close();
    }

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
            try {
                iStream.readUTF();
                cells = null;
                cells = (ArrayList<Cell>) iStream.readObject();
                iStream.readUTF();
                dependencies = null;
                dependencies = (ArrayList<Dependency>) iStream.readObject();
                iStream.readUTF();
                newXOffsets = (HashMap<Integer, Integer>) iStream.readObject();
                iStream.readUTF();
                newYOffsets = (HashMap<Integer, Integer>) iStream.readObject();
                iStream.readUTF();
                macros = null;
                macros = (HashMap<Character, LinkedList<KeyEvent>>) iStream.readObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println(Arrays.toString(e.getStackTrace()));
            }

            newXOffsets = (newXOffsets == null) ? new HashMap<>() : newXOffsets;
            newYOffsets = (newYOffsets == null) ? new HashMap<>() : newYOffsets;
            Controller.reset(newXOffsets, newYOffsets);
            if (macros == null) macros = new HashMap<>();
            iStream.close();
            file = new File(path);
            Controller.statusBar.setFilename(file.getName());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            Controller.reset(new HashMap<>(), new HashMap<>());
            file = new File(path);
            Controller.statusBar.setFilename(file.getName());
            throw new Exception("New file");
        }
    }
}

class Dependency implements Serializable {
    final int xCoord, yCoord;
    boolean toBeEvaluated;
    final ArrayList<Dependency> dependents;
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

package vimicalc.model;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;
import vimicalc.view.Metadata;

import java.io.*;
import java.util.*;

import static vimicalc.controller.Controller.macros;
import static vimicalc.utils.Conversions.*;

public class Sheet {
    private ArrayList<Cell> cells;
    private File file;
    private ArrayList<Dependency> dependencies;
    private Metadata picMetadata;

    public Sheet() {
        cells = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public Metadata getPicMetadata() {
        return picMetadata;
    }

    public void setPicMetadata(Metadata picMetadata) {
        this.picMetadata = picMetadata;
    }

    public ArrayList<Cell> getCells() {
        return cells;
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
        checkForDependents(xCoord, yCoord);
        Dependency d = findDependency(xCoord, yCoord);
        if (d != null)
            d.setDependeds(new ArrayList<>());
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

    public void addDepended(int xCoord, int yCoord, Dependency dependent) {
        Dependency depended = findDependency(xCoord, yCoord);
        if (depended == null) {
            depended = new Dependency(xCoord, yCoord);
            depended.getDependents().add(dependent);
            dependent.getDependeds().add(depended);
            dependencies.add(depended);
        }
        else if (!dependedAlreadyAdded(dependent, depended)) {
            depended.getDependents().add(dependent);
            dependent.getDependeds().add(depended);
            dependencies.add(depended);
        }
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
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        checkForDependents(cell.xCoord(), cell.yCoord());
    }
    public void simplyAddCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
    }

    public void checkForDependents(int xCoord, int yCoord) {
        System.out.println("Checking for dependents...");
        boolean needsEvaluating = false;
        for (Dependency d : dependencies) {
            if (d.getxCoord() == xCoord && d.getyCoord() == yCoord &&
                d.getDependents().size() != 0) {
                dependencies.forEach(e -> System.out.println(e.log()));
                evalDependencies(d);
                needsEvaluating = true;
                break;
            }
        }

        if (needsEvaluating) {
            cells.removeIf(Cell::isEmpty);
            System.out.println("All of the dependencies (result):");
            dependencies.forEach(d -> System.out.println(d.log()));
        }
        else
            System.out.println("No evaluations done.");
    }

    public void evalDependencies(@NotNull Dependency d) {
        System.out.println("Evaluating dependencies...");
        d.setToBeEvaluated();
        System.out.println(d.log());
        if (d.isReadyToBeEvaluated())
            d.evaluate(this);
        for (Dependency e : d.getDependents())
            evalDependencies(e);
    }

    public void writeFile() throws IOException {
        writeFile(file.getPath());
    }

    public void writeFile(@NotNull String path) throws IOException {
        if (!path.endsWith(".wss")) path += ".wss";
        System.out.println("Saving file " + path + "...");
        ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(path));

        try {
            oStream.writeUTF("\n====Cells====\n"); oStream.flush();
            oStream.writeObject(cells); oStream.flush();

            oStream.writeUTF("\n\n====Dependencies====\n"); oStream.flush();
            oStream.writeObject(dependencies); oStream.flush();

            oStream.writeUTF("\n\n====xOffsets====\n"); oStream.flush();
            oStream.writeObject(picMetadata.getxOffsets()); oStream.flush();

            oStream.writeUTF("\n\n====yOffsets====\n"); oStream.flush();
            oStream.writeObject(picMetadata.getyOffsets()); oStream.flush();

            oStream.writeUTF("\n\n====Macros====\n"); oStream.flush();
            oStream.writeObject(macros); oStream.flush();
        } catch (Exception e) {
            System.out.println("Error while saving file: " + Arrays.toString(e.getStackTrace()));
        }

        oStream.close();
        file = new File(path);
        Controller.statusBar.setFilename(file.getName());
    }

    public void readFile(@NotNull String path) throws Exception {
        if (!path.endsWith(".wss")) {
            /* On va devoir afficher cela dans le infobar, de manière optimale.
             * Je suggère d'avoir une variable statique pour le texte d'infobar, au lieu
             * de rendre tout le infobar static. */
            String errorMessage = "File is not of .wss format";
            throw new Exception(errorMessage);
        }
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
        iStream.close();
        file = new File(path);
        Controller.statusBar.setFilename(file.getName());
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
            c.setFormulaResult(c.formula().interpret(sheet), c.formula());
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
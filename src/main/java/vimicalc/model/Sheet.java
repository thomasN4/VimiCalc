package vimicalc.model;

import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static vimicalc.utils.Conversions.*;

public class Sheet {

    private ArrayList<Cell> cells;
    private File file;
    private final ArrayList<Dependency> dependencies;
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


    public void deleteCell(@NotNull String coords) {
        int[] coordsInt = coordsStrToInt(coords);
        deleteCell(coordsInt[0], coordsInt[1]);
    }

    public void deleteCell(int xCoord, int yCoord) {
        cells.remove(findCell(xCoord, yCoord));
        checkForDependents(xCoord, yCoord);
        Dependency d = findDependency(xCoord, yCoord);
        if (d != null)
            d.setDependeds(new ArrayList<>());
    }

    public void purgeEmptyCells() {
        cells.removeIf(c -> c.txt() == null && c.getMergedWith() == null);
    }

    public Cell findCell(@NotNull String coords) {
        int[] coordsInt = coordsStrToInt(coords);
        return findCell(coordsInt[0], coordsInt[1]);
    }

    public Cell findCell(int xCoord, int yCoord) {
        Cell found = new Cell(xCoord, yCoord);

        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                found = c;

        System.out.println("Found cell: "+ found);
        return found;
    }

    public Dependency findDependency(int x, int y) {
        for (Dependency d : dependencies) {
            if (d.getxCoord() == x && d.getyCoord() == y)
                return d;
        }
        return null;
    }

    public void addDependent(int xCoord, int yCoord) {
        if (findDependency(xCoord, yCoord) == null)
            dependencies.add(new Dependency(xCoord, yCoord));
    }

    public boolean dependedAlreadyAdded(Dependency dependent, Dependency depended) {
        for (Dependency d : dependent.getDependeds()) {
            if (d.getxCoord() == depended.getxCoord() && d.getyCoord() == depended.getyCoord()) {
                return true;
            }
        }
        return false;
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

    public void addCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        checkForDependents(cell.xCoord(), cell.yCoord());
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
            purgeEmptyCells();
            System.out.println("All of the dependencies (result):");
            dependencies.forEach(d -> System.out.println(d.log()));
        }
        else
            System.out.println("No evaluations done.");
    }

    public void evalDependencies(@NotNull Dependency d) {
        System.out.println("Evaluating dependencies...");
        d.setToBeEvaluated(true);
        System.out.println(d.log());
        if (d.isReadyToBeEvaluated())
            d.evaluate(this);
        for (Dependency e : d.getDependents())
            evalDependencies(e);
    }

    public void writeFile() throws IOException {
        writeFile(file.getPath());
    }

    public void writeFile(String path) throws IOException {
        BufferedWriter fW = new BufferedWriter(new FileWriter(path));
        fW.write("xCoord,yCoord,data,formula\n");
        fW.flush();
        cells.forEach(c -> {
            try {
                String data;
                if (c.value() != 0) data = String.valueOf(c.value());
                else data = c.txt();
                String frmlTxt = "null";
                if (c.formula() != null) frmlTxt = c.formula().getTxt();
                fW.write(
                   c.xCoord() + "," +
                    c.yCoord() + "," +
                    data + "," +
                    frmlTxt + "\n"
                );
                fW.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        fW.close();
        file = new File(path);
        Controller.statusBar.setFilename(file.getName());
    }

    public void readFile(String path) throws IOException {
        cells = new ArrayList<>();
        BufferedReader fR = new BufferedReader(new FileReader(path));

        int b;
        char c = '\0';
        String[] cellItems = new String[4];
        for (byte i = 0; i < cellItems.length; i++) cellItems[i] = "";
        byte pos = 0;

        while (c != '\n') {
            c = (char) fR.read();
        }

        while (true) {
            b = fR.read();
            if (b == -1) break;
            else c = (char) b;

            if (c == ',') pos++;
            else if (c == '\n') {
                System.out.println("Cell items: " + Arrays.toString(cellItems));
                pos = 0;
                int xCoord = Integer.parseInt(cellItems[0]);
                int yCoord = Integer.parseInt(cellItems[1]);
                Formula f = new Formula(cellItems[3], xCoord, yCoord);
                if (!cellItems[3].equals("null"))
                    addCell(new Cell(
                        xCoord,
                        yCoord,
                        f.interpret(this),
                        f
                ));
                else addCell(new Cell(
                    xCoord,
                    yCoord,
                    cellItems[2]
                ));
                cellItems = new String[4];
                for (byte i = 0; i < cellItems.length; i++) cellItems[i] = "";
            } else cellItems[pos] += c;
        }

        fR.close();
        Controller.reset();
        file = new File(path);
        Controller.statusBar.setFilename(file.getName());
    }
}

class Dependency {
    private final int xCoord, yCoord;
    private boolean toBeEvaluated;
    private final ArrayList<Dependency> dependents;
    private ArrayList<Dependency> dependeds;

    public Dependency(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        toBeEvaluated = false;
        dependents = new ArrayList<>();
        dependeds = new ArrayList<>();
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public ArrayList<Dependency> getDependeds() {
        return dependeds;
    }

    public boolean isToBeEvaluated() {
        return toBeEvaluated;
    }

    public ArrayList<Dependency> getDependents() {
        return dependents;
    }

    public void setDependeds(ArrayList<Dependency> dependeds) {
        this.dependeds = dependeds;
    }

    public void setToBeEvaluated(boolean toBeEvaluated) {
        this.toBeEvaluated = toBeEvaluated;
    }

    public boolean isReadyToBeEvaluated() {
        boolean b = true;
        for (Dependency d : dependeds) {
            if (d.isToBeEvaluated()) {
                b = false;
                break;
            }
        }
        return toBeEvaluated & b;
    }

    public void evaluate(@NotNull Sheet sheet) {
        Cell c = sheet.findCell(xCoord, yCoord);
        if (c.formula() != null) {
            sheet.getCells().removeIf(b -> b.xCoord() == c.xCoord() && b.yCoord() == c.yCoord());
            sheet.getCells().add(new Cell(
                xCoord,
                yCoord,
                c.formula().interpret(sheet),
                c.formula()
            ));
        }
        else if (c.txt() == null) {
            sheet.getCells().removeIf(b -> b.xCoord() == c.xCoord() && b.yCoord() == c.yCoord());
            sheet.getCells().add(new Cell(xCoord, yCoord));
        }
        toBeEvaluated = false;
        System.out.println("Evaluating dependency at: " + xCoord + ", " + yCoord + "...");
    }

    public String log() {
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
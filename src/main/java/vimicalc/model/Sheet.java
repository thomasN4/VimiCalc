package vimicalc.model;

import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static vimicalc.utils.Conversions.fromAlpha;

public class Sheet {

    private ArrayList<Cell> cells;
    private File file;
    private final ArrayList<DependencyRelation> dependencies;

    public Sheet() {
        cells = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(String coords) {
        cells.remove(findCell(coords));

        StringBuilder coordX = new StringBuilder(),
                coordY = new StringBuilder();

        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) > 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }

        int xCoord, yCoord;
        try {
            xCoord = fromAlpha(coordX.toString());
            yCoord = Integer.parseInt(coordY.toString());
        } catch (Exception ignored) {
            xCoord = 0;
            yCoord = 0;
        }
        checkForDependencies(xCoord, yCoord);
    }

    public Cell findCell(@NotNull String coords) {
        StringBuilder coordX = new StringBuilder(),
                coordY = new StringBuilder();

        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) > 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }

        int xCoord, yCoord;
        try {
            xCoord = fromAlpha(coordX.toString());
            yCoord = Integer.parseInt(coordY.toString());
        } catch (Exception ignored) {
            xCoord = 0;
            yCoord = 0;
        }

        Cell found = new Cell(xCoord, yCoord);

        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                found = c;

        System.out.println("Found cell: "+ found);
        return found;
    }

    public Cell findCell(int xCoord, int yCoord) {
        Cell found = new Cell(xCoord, yCoord);

        for (Cell c : getCells())
            if (c.xCoord() == xCoord && c.yCoord() == yCoord)
                found = c;

        System.out.println("Found cell: "+ found);
        return found;
    }

    public DependencyRelation findDependency(int x, int y) {
        for (DependencyRelation d : dependencies) {
            if (d.getxCoord() == x && d.getyCoord() == y)
                return d;
        }
        return null;
    }

    public void addDependent(int xCoord, int yCoord) {
        if (findDependency(xCoord, yCoord) == null)
            dependencies.add(new DependencyRelation(xCoord, yCoord));
        else
            System.out.println("DependencyRelation already added.");
        dependencies.forEach(d -> System.out.println(d.log()));
    }

    public void addDepended(int xCoord, int yCoord, DependencyRelation dependent) {
        if (findDependency(xCoord, yCoord) == null) {
            DependencyRelation depended = new DependencyRelation(xCoord, yCoord);
            dependent.getDependeds().add(depended);
            depended.getDependents().add(dependent);
            dependencies.add(depended);
        }
        else
            System.out.println("DependencyRelation already added.");
        dependencies.forEach(d -> System.out.println(d.log()));
    }

    public void addCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        checkForDependencies(cell.xCoord(), cell.yCoord());
    }

    public void checkForDependencies(int xCoord, int yCoord) {
        System.out.println("Checking for dependencies...");
        dependencies.forEach(d -> System.out.println(d.log()));
        for (DependencyRelation d : dependencies) {
            if (d.getxCoord() == xCoord && d.getyCoord() == yCoord) {
                evalDependencies(d);
                break;
            }
        }
        cells.removeIf(c -> c.txt().equals("t3mp"));
        dependencies.forEach(d -> System.out.println(d.log()));
    }

    public void evalDependencies(@NotNull DependencyRelation d) {
        System.out.println("Evaluating dependencies...");
        d.setToBeEvaluated(true);
        System.out.println(d.log());
        for (DependencyRelation e : d.getDependents()) {
            if (e.isToBeEvaluated())
                evalDependencies(e);
        }
        for (DependencyRelation e : dependencies) {
            if (e.isReadyToBeEvaluated())
                d.evaluate(this);
        }
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

class DependencyRelation {
    private final int xCoord, yCoord;
    private boolean toBeEvaluated;
    private final ArrayList<DependencyRelation> dependents;
    private final ArrayList<DependencyRelation> dependeds;

    public DependencyRelation(int xCoord, int yCoord) {
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

    public ArrayList<DependencyRelation> getDependeds() {
        return dependeds;
    }

    public boolean isToBeEvaluated() {
        return toBeEvaluated;
    }

    public ArrayList<DependencyRelation> getDependents() {
        return dependents;
    }

    public void setToBeEvaluated(boolean toBeEvaluated) {
        this.toBeEvaluated = toBeEvaluated;
    }

    public boolean isReadyToBeEvaluated() {
        boolean b = true;
        for (DependencyRelation m : dependeds) {
            if (!m.isToBeEvaluated()) {
                b = false;
                break;
            }
        }
        return toBeEvaluated & b;
    }

    public void evaluate(@NotNull Sheet sheet) {
        Cell c = sheet.findCell(xCoord, yCoord);
        sheet.getCells().removeIf(b -> b.xCoord() == c.xCoord() && b.yCoord() == c.yCoord());
        if (c.formula() != null)
            sheet.getCells().add(new Cell(
                c.xCoord(),
                c.yCoord(),
                c.formula().interpret(sheet),
                c.formula()
            ));
        else
            sheet.getCells().add(new Cell(
                c.xCoord(),
                c.yCoord(),
                "t3mp"
            ));
        toBeEvaluated = false;
    }

    public String log() {
        return "DependencyRelation: " + xCoord + ", " + yCoord + " {\n" +
               "\ttoBeEvaluated = " + toBeEvaluated + "\n" +
               "\treadyToBeEvaluated = " + isReadyToBeEvaluated() + "\n" +
               "\tModifiers: {\n" +
               "\t\t" + dependeds + "\n" +
               "\t}\n" +
               "\tDependents: {\n" +
               "\t\t" + dependents + "\n" +
               "\t}\n" +
               "}";
    }
}
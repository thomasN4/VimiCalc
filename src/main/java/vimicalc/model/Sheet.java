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
    private final ArrayList<Dependency> dependencies;

    public Sheet() {
        cells = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(String coords) {
        cells.remove(findCell(coords));
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

    public Dependency findDependency(int x, int y) {
        for (Dependency d : dependencies) {
            if (d.getxCoord() == x && d.getyCoord() == y)
                return d;
        }
        return null;
    }

    public void addDependency(int xCoord, int yCoord) {
        if (findDependency(xCoord, yCoord) == null) {
            dependencies.add(new Dependency(xCoord, yCoord));
            dependencies.forEach(d -> System.out.println(d.log()));
        }
        else
            System.out.println("Dependency already added.");
    }

    public void addCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
        checkForDependencies(cell.xCoord(), cell.yCoord());
    }

    public void checkForDependencies(int x, int y) {
        System.out.println("Checking for dependencies...");
        for (Dependency d : dependencies) {
            if (d.getxCoord() == x && d.getyCoord() == y) {
                evalDependencies(d);
                break;
            }
        }
    }

    public void evalDependencies(Dependency d) {
        System.out.println("Evaluating dependencies...");
        d.setToBeEvaluated(true);
        System.out.println(d.log());
        for (Dependency e : d.getDependents()) {
            if (e.isToBeEvaluated())
                evalDependencies(e);
        }
        for (Dependency e : dependencies) {
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
                if (!cellItems[3].equals("null"))
                    addCell(new Cell(
                        xCoord,
                        yCoord,
                        Double.parseDouble(cellItems[2]),
                        new Formula(cellItems[3], xCoord, yCoord)
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
    private final ArrayList<Dependency> modifiers;

    public Dependency(int xCoord, int yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        toBeEvaluated = false;
        dependents = new ArrayList<>();
        modifiers = new ArrayList<>();
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public void addModifier(@NotNull Cell cell) {
        modifiers.removeIf(m ->
            m.getxCoord() == cell.xCoord() && m.getyCoord() == cell.yCoord()
        );
        Dependency modifier = new Dependency(cell.xCoord(), cell.yCoord());
        modifier.dependents.add(this);
        modifiers.add(modifier);
    }

    public boolean isToBeEvaluated() {
        return toBeEvaluated;
    }

    public ArrayList<Dependency> getDependents() {
        return dependents;
    }

    public void setToBeEvaluated(boolean toBeEvaluated) {
        this.toBeEvaluated = toBeEvaluated;
    }

    public boolean isReadyToBeEvaluated() {
        boolean b = true;
        for (Dependency m : modifiers) {
            if (!m.isToBeEvaluated()) {
                b = false;
                break;
            }
        }
        return toBeEvaluated & b;
    }

    public void evaluate(@NotNull Sheet sheet) {
        Cell c = sheet.findCell(xCoord, yCoord);
        sheet.addCell(new Cell(
            c.xCoord(),
            c.yCoord(),
            c.formula().interpret(sheet),
            c.formula()
        ));
        toBeEvaluated = false;
    }

    public String log() {
        return "Dependency: " + xCoord + ", " + yCoord + " {\n" +
                "toBeEvaluated = " + toBeEvaluated + "\n" +
                "readyToBeEvaluated = " + isReadyToBeEvaluated() + "\n" +
                "\tModifiers: {\n" +
                "\t\t" + modifiers + "\n" +
                "\t}\n" +
                "\tDependents: {\n" +
                "\t\t" + dependents + "\n" +
                "\t}\n" +
                "}";
    }
}
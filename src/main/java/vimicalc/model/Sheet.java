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
    private ArrayList<Dependency> dependencies;

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

    public void addCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
    }

    public void checkDependencies(int x, int y) {
        for (Dependency d : dependencies) {
            if (d.getxCoord() == x && d.getyCoord() == y) {
                evalDependencies(d);
                break;
            }
        }
    }

    public void evalDependencies(Dependency d) {
        boolean allDependentsEvaluated = true;
        for (Dependency e : d.modifiers) {
           if (e.isToBeEvaluated()) {
               allDependentsEvaluated = false;
               break;
           }
        }
        if (allDependentsEvaluated) {
            d.evaluate(this);
        }
        for (Dependency e : d.dependents) {
            if (e.isToBeEvaluated())
                evalDependencies(e);
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
                    cells.add(new Cell(
                        xCoord,
                        yCoord,
                        Double.parseDouble(cellItems[2]),
                        new Formula(cellItems[3], xCoord, yCoord)
                ));
                else cells.add(new Cell(
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
    private final Cell cell;
    private final int xCoord, yCoord;
    private boolean toBeEvaluated;
    ArrayList<Dependency> dependents;
    ArrayList<Dependency> modifiers;

    public Dependency(Cell cell) {
        this.cell = cell;
        xCoord = cell.xCoord();
        yCoord = cell.yCoord();
        toBeEvaluated = false;
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public void addDependent(Cell cell) {
        dependents.removeIf(d ->
            d.getxCoord() == cell.xCoord() && d.getyCoord() == cell.yCoord()
        );
        Dependency dependent = new Dependency(cell);
        dependent.modifiers.add(this);
        dependents.add(dependent);
    }

    public void addModifier(Cell cell) {
        modifiers.removeIf(m ->
            m.getxCoord() == cell.xCoord() && m.getyCoord() == cell.yCoord()
        );
        Dependency modifier = new Dependency(cell);
        modifier.dependents.add(this);
        modifiers.add(modifier);
    }

    public boolean isToBeEvaluated() {
        return toBeEvaluated;
    }

    public void setToBeEvaluated(boolean toBeEvaluated) {
        this.toBeEvaluated = toBeEvaluated;
    }

    public void evaluate(Sheet sheet) {
        cell.setValue(cell.formula().interpret(sheet));
        toBeEvaluated = true;
    }
}
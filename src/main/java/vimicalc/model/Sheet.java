package vimicalc.model;

import vimicalc.controller.Controller;

import java.io.*;
import java.util.ArrayList;

import static vimicalc.Main.fromAlpha;

public class Sheet {

    private ArrayList<Cell> cells;
    private File file;

    public Sheet() {
        cells = new ArrayList<>();
        file = new File("~/new_file.csv");
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void deleteCell(String coords) {
        cells.remove(findCell(coords));
    }

    public Cell findCell(String coords) {
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

    public void updateCells(ArrayList<Cell> modified) {
        modified.forEach(m -> cells.removeIf(
            c -> c.xCoord() == m.xCoord() && c.yCoord() == m.yCoord()
        ));
        cells.addAll(modified);
    }

    public void addCell(Cell cell) {
        cells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        cells.add(cell);
    }

    public void writeFile() throws IOException {
        writeFile(file.getPath());
    }

    public void writeFile(String path) throws IOException {
        FileWriter fW = new FileWriter(path);
        fW.write("xCoord, yCoord, txt, value, formula\n");
        cells.forEach(c -> {
            try {
                fW.write(
                   c.xCoord() + ',' +
                    c.yCoord() + ',' +
                    c.value() + ',' +
                    c.formula().getTxt() + "\n"
                );
                fW.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        fW.close();
        file = new File(path);
    }

    public void readFile(String path) throws IOException {
        Controller.reset();
        Controller.statusBar.setFilename(file.getName());
        cells = new ArrayList<>();
        FileReader fR = new FileReader(path);
        char c;
        String[] cellItems = new String[4];
        byte pos = 0;
        for ( ; ; ) {
            try {
                c = (char) fR.read();
            } catch (EOFException e) {
                break;
            }
            if (c != ',') cellItems[pos] += c;
            else pos++;
            if (c == '\n') {
                pos = 0;
                cells.add(new Cell(
                    Integer.parseInt(cellItems[0]),
                    Integer.parseInt(cellItems[1]),
                    cellItems[2],
                    new Formula(cellItems[3])
                ));
                cellItems = new String[4];
            }
        }
        fR.close();
    }
}
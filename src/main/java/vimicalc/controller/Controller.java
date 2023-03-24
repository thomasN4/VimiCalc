package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;
import vimicalc.model.Sheet;
import vimicalc.view.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static int CANVAS_W;
    private static int CANVAS_H;
    private static final int DEFAULT_CELL_H = 23;
    private static final int DEFAULT_CELL_W = DEFAULT_CELL_H * 4;
    private static final Color DEFAULT_CELL_C = Color.LIGHTGRAY;

    //CD
//    private static int MOUSE_X;
//    private static int MOUSE_Y;

    private static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};
    @FXML
    private Canvas canvas;
    public static LinkedList<Cell> recordedCell = new LinkedList<>();
    public static int dCounter = 1;
    private static Camera camera;
    private static Command command;
    private static CoordsCell coordsCell;
    private static FirstCol firstCol;
    private static FirstRow firstRow;
    private static GraphicsContext gc;
    private static InfoBar infoBar;
    private static CellSelector cellSelector;
    private static ArrayList<int[]> selectedCoords;
    private static Sheet sheet;
    public static StatusBar statusBar;

    /*CD arranger avec les classes moves car sinon cause des bugs en utilisant clavier
    public static void onMouseClicked(@NotNull MouseEvent mouseEvent) {
        MOUSE_X = (int) mouseEvent.getX() / DEFAULT_CELL_W;
        MOUSE_Y = (int) mouseEvent.getY() / DEFAULT_CELL_H - 1;

        System.out.print(" Mouse CLicked "+ MOUSE_X + " // " + MOUSE_Y);

        cellSelector.setX(MOUSE_X);
        cellSelector.setY(MOUSE_Y);

        System.out.print(" Selected Cell "+ cellSelector.getX() + " // " + cellSelector.getY());
    }
    */
    private static void moveLeft() {
        if (cellSelector.getXCoord() != 1)
            cellSelector.updateXCoord(-1);
        if (cellSelector.getX() != cellSelector.getW()) {
            cellSelector.updateX(-cellSelector.getW());
            if (cellSelector.getX() < cellSelector.getW()) {
                while (cellSelector.getX() != cellSelector.getW()) {
                    cellSelector.updateX(1);
                    camera.updateAbsX(-1);
                }
                firstRow.draw(gc, camera.getAbsX());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            infoBar.setErrorTxt("CAN'T GO LEFT");
            infoBar.setError(true);
            camera.updateAbsX(-cellSelector.getW());
            if (camera.getAbsX() < 0)
                while (camera.getAbsX() != 0)
                    camera.updateAbsX(1);
            firstRow.draw(gc, camera.getAbsX());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    private static void moveUp() {
        if (cellSelector.getYCoord() != 1)
            cellSelector.updateYCoord(-1);
        if (cellSelector.getY() != cellSelector.getH()) {
            cellSelector.updateY(-cellSelector.getH());
            if (cellSelector.getY() < cellSelector.getH()) {
                while (cellSelector.getY() != cellSelector.getH()) {
                    cellSelector.updateY(1);
                    camera.updateAbsY(-1);
                }
                firstCol.draw(gc, camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            infoBar.setErrorTxt("CAN'T GO UP");
            infoBar.setError(true);
            camera.updateAbsY(-cellSelector.getH());
            if (camera.getAbsY() < 0)
                while (camera.getAbsY() != 0)
                    camera.updateAbsY(1);
            firstCol.draw(gc, camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    private static void moveDown() {
        cellSelector.updateYCoord(1);
        if (cellSelector.getY() != camera.picture.getH()) {
            cellSelector.updateY(cellSelector.getH());
            if (cellSelector.getY() > camera.picture.getH()) {
                while (cellSelector.getY() != camera.picture.getH()) {
                    cellSelector.updateY(-1);
                    camera.updateAbsY(1);
                }
                firstCol.draw(gc, camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsY(cellSelector.getH());
            firstCol.draw(gc, camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    private static void moveRight() {
        cellSelector.updateXCoord(1);
        if (cellSelector.getX() != camera.picture.getW()) {
            cellSelector.updateX(cellSelector.getW());
            if (cellSelector.getX() > camera.picture.getW()) {
                while (cellSelector.getX() != camera.picture.getW()) {
                    cellSelector.updateX(-1);
                    camera.updateAbsX(1);
                }
                firstRow.draw(gc, camera.getAbsX());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsX(cellSelector.getW());
            firstRow.draw(gc, camera.getAbsX());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    public static void undo() {//the value is at zero when i record them, hence the errors
        goTo(recordedCell.get(recordedCell.size() - 1 - dCounter).xCoord(), recordedCell.get(recordedCell.size() - 1 - dCounter).yCoord());
        cellSelector.setSelectedCell(new Cell(
                cellSelector.getXCoord(),
                cellSelector.getYCoord(),
                cellSelector.getSelectedCell().txt()
        ));
        sheet.addCell(cellSelector.getSelectedCell());
        cellSelector.getSelectedCell().setTxt(recordedCell.get(recordedCell.size() - 1 - dCounter).txt());
        dCounter = dCounter + 2;
    }

    public static void redo() {
        dCounter = dCounter - 2;
        goTo(recordedCell.get(recordedCell.size() - dCounter).xCoord(), recordedCell.get(recordedCell.size() - dCounter).yCoord());
        cellSelector.setSelectedCell(new Cell(
                cellSelector.getXCoord(),
                cellSelector.getYCoord(),
                cellSelector.getSelectedCell().txt()
        ));
        sheet.addCell(cellSelector.getSelectedCell());
        cellSelector.getSelectedCell().setTxt(recordedCell.get(recordedCell.size() - dCounter).txt());
    }

    public static void goTo(int x, int y) {
        while ((x - cellSelector.getXCoord()) > 0) {
            moveRight();
        }
        while ((x - cellSelector.getXCoord()) < 0) {
            moveLeft();
        }
        while ((y - cellSelector.getYCoord()) > 0) {
            moveDown();
        }
        while ((y - cellSelector.getYCoord()) < 0) {
            moveUp();
        }
        firstRow.draw(gc, camera.getAbsX());
        firstCol.draw(gc, camera.getAbsY());
        coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        coordsCell.draw(gc);
        statusBar.draw(gc);
    }

    public static void onKeyPressed(@NotNull KeyEvent event) {
        infoBar.setError(false);
        switch (statusBar.getMode().charAt(1)) {
            case 'C' -> commandInput(event);
            case 'F' -> formulaInput(event);
            case 'I' -> textInput(event);
            case 'N' -> {
                switch (event.getCode()) {
                    case H, LEFT, BACK_SPACE -> moveLeft();
                    case J, DOWN, ENTER -> moveDown();
                    case K, UP -> moveUp();
                    case L, RIGHT, TAB, SPACE -> moveRight();
                    case D, DELETE -> {
                        if (cellSelector.getSelectedCell().txt().equals("")) {
                            infoBar.setErrorTxt("CAN'T DELETE RIGHT NOW");
                            infoBar.setError(true);
                        }
                        recordedCell.add(cellSelector.getSelectedCell());
                        sheet.deleteCell(coordsCell.getCoords());
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                        recordedCell.add(cellSelector.getSelectedCell());
                    }
                    case U -> {
                        if (!recordedCell.isEmpty() && !(dCounter >= recordedCell.size())) {
                            if (infoBar.isError()) {
                                infoBar.setError(false);
                            }
                            undo();
                        }
                        else {
                            infoBar.setErrorTxt("CAN'T UNDO RIGHT NOW");
                            infoBar.setError(true);
                        }
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    case R -> {
                        if (!recordedCell.isEmpty() && !(dCounter <= 1)) {
                            if (infoBar.isError()) {
                                infoBar.setError(false);
                            }
                            redo();
                        }
                        else {
                            infoBar.setErrorTxt("CAN'T REDO RIGHT NOW");
                            infoBar.setError(true);
                        }
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    case A, I -> {
                        cellSelector.setSelectedCell(new Cell(
                                cellSelector.getXCoord(),
                                cellSelector.getYCoord(),
                                cellSelector.getSelectedCell().txt()
                        ));
                        recordedCell.add(cellSelector.getSelectedCell());
                        cellSelector.readCell(camera.picture.data());
                        statusBar.setMode(MODE[2]);
                        if (cellSelector.getSelectedCell().value() != 0) {
                            cellSelector.getSelectedCell().setTxt(String.valueOf(
                                    cellSelector.getSelectedCell().value()
                            ));
                        }
                        cellSelector.draw(gc);
                    }
                    case ESCAPE -> statusBar.setMode(MODE[3]);
                    case EQUALS -> {
                        statusBar.setMode(MODE[1]);
                        infoBar.setEnteringFormula(true);
                        recordedCell.add(cellSelector.getSelectedCell());
                    }
                    case V -> {
                        statusBar.setMode(MODE[4]);
                        selectedCoords.add(new int[]{cellSelector.getXCoord(), cellSelector.getYCoord()});
                    }
                    case SEMICOLON -> {
                        statusBar.setMode(MODE[0]);
                        command = new Command("");
                        infoBar.setEnteringCommand(true);
                    }
                }
            }
            case 'V' -> visualSelection(event);
        }

        System.out.println("     sC.x: " + cellSelector.getX() + ", yCoord: " + cellSelector.getY());
        System.out.println("sC.xCoord: " + cellSelector.getXCoord() + ", sC.yCoord: " + cellSelector.getYCoord());
        System.out.println(" cam.absX: " + camera.getAbsX() + ", cam.absY: " + camera.getAbsY());
        System.out.println("    Cells: " + sheet.getCells());
        System.out.println("========================================");

        if (statusBar.getMode().equals(MODE[3])) {
            cellSelector.draw(gc);
            coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        } else if (statusBar.getMode().equals(MODE[4])) {
            System.out.println("Selected coords = {");
            selectedCoords.forEach(c -> {
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                System.out.println('\t' + Arrays.toString(c));
            });
            camera.ready();
            System.out.println('}');
        }
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        statusBar.draw(gc);
        infoBar.setKeyStroke(event.getCode().toString());
        infoBar.draw(gc, cellSelector.getSelectedCell());
        coordsCell.draw(gc);
    }

    private static void commandInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (infoBar.isEnteringCommandInVISUAL()) {
                    selectedCoords = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);
                }
                infoBar.setEnteringCommand(false);
                statusBar.setMode(MODE[3]);
                command = new Command("");
                infoBar.setCommandTxt("");
            }
            case ENTER -> {
                if (infoBar.isEnteringCommandInVISUAL()) {
                    selectedCoords = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);

                    StringBuilder destinationCoord = new StringBuilder();
                    int i;
                    for (i = command.getTxt().length() - 1; i > 0; i--) {
                        if (command.getTxt().charAt(i) == ' ') break;
                        destinationCoord.append(command.getTxt().charAt(i));
                    }
                    destinationCoord.reverse();

                    Cell c = sheet.findCell(destinationCoord.toString());
                    Formula f = new Formula(
                            coordsCell.getCoords() + ' ' + command.getTxt().substring(0, i),
                            c.xCoord(),
                            c.yCoord()
                    );
                    sheet.addCell(new Cell(
                            c.xCoord(),
                            c.yCoord(),
                            f.interpret(sheet),
                            f
                    ));
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                }
                infoBar.setEnteringCommand(false);
                statusBar.setMode(MODE[3]);
                command.interpret(sheet);
                command = new Command("");
                infoBar.setCommandTxt("");
                if (!command.commandExists) {
                    infoBar.setErrorTxt("COMMAND OR FILE DOES NOT EXIST");
                    infoBar.setError(true);
                }

            }
            case BACK_SPACE -> command.setTxt(
                    command.getTxt().substring(0, command.getTxt().length() - 1)
            );
            default -> command.setTxt(command.getTxt() + event.getText());
        }
        if (!statusBar.getMode().equals(MODE[3]))
            infoBar.setCommandTxt(command.getTxt());
    }

    private static void visualSelection(@NotNull KeyEvent event) {
        if (infoBar.isEnteringCommandInVISUAL()) {
            commandInput(event);
        } else {
            switch (event.getCode()) {
                case D -> {
                    selectedCoords.forEach(coord -> sheet.getCells().removeIf(
                            cell -> cell.xCoord() == coord[0] && cell.yCoord() == coord[1]
                    ));
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                }
                case ESCAPE -> {
                    statusBar.setMode(MODE[3]);
                    selectedCoords = new ArrayList<>();
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    cellSelector.draw(gc);
                }
                case SEMICOLON -> {
                    infoBar.setEnteringCommandInVISUAL(true);
                    command = new Command("");
                }
                default -> {
                    int originalXC = selectedCoords.get(0)[0];
                    int originalYC = selectedCoords.get(0)[1];

                    int prevXC = cellSelector.getXCoord();
                    int prevYC = cellSelector.getYCoord();

                    int maxXC;
                    int minXC;
                    int maxYC;
                    int minYC;
                    if (selectedCoords.size() > 1) {
                        maxXC = Integer.MIN_VALUE;
                        minXC = Integer.MAX_VALUE;
                        maxYC = Integer.MIN_VALUE;
                        minYC = Integer.MAX_VALUE;
                        for (int[] c : selectedCoords) {
                            if (c[0] > maxXC) maxXC = c[0];
                            if (c[0] < minXC) minXC = c[0];
                            if (c[1] > maxYC) maxYC = c[1];
                            if (c[1] < minYC) minYC = c[1];
                        }
                    } else {
                        maxXC = originalXC;
                        minXC = maxXC;
                        maxYC = originalYC;
                        minYC = maxYC;
                    }
                    System.out.println("maxXC: " + maxXC);
                    System.out.println("minXC: " + minXC);
                    System.out.println("maxYC: " + maxYC);
                    System.out.println("minYC: " + minYC);

                    switch (event.getCode()) {
                        case H, LEFT, BACK_SPACE -> moveLeft();
                        case J, DOWN, ENTER -> moveDown();
                        case K, UP -> moveUp();
                        case L, RIGHT, TAB, SPACE -> moveRight();
                    }
                    int currXC = cellSelector.getXCoord();
                    int currYC = cellSelector.getYCoord();
                    System.out.println("currXC: " + currXC);
                    System.out.println("currYC: " + currYC);

                    System.out.println("originalXC = " + originalXC);
                    System.out.println("originalYC = " + originalYC);

                    if (currXC >= originalXC && currYC >= originalYC) {
                        if (currXC > prevXC) {
                            addSCs(true, currXC, minYC, maxYC);
                            maxXC = currXC;
                        } else if (currXC < prevXC) {
                            purgeSCs(prevXC, -1);
                            maxXC = currXC;
                        } else if (currYC > prevYC) {
                            addSCs(false, currYC, minXC, maxXC);
                            maxYC = currYC;
                        } else if (currYC < prevYC) {
                            purgeSCs(-1, prevYC);
                            maxYC = currYC;
                        }
                    } else if (currXC >= originalXC) {
                        if (currXC > prevXC) {
                            addSCs(true, currXC, minYC, maxYC);
                            maxXC = currXC;
                        } else if (currXC < prevXC) {
                            purgeSCs(prevXC, -1);
                            maxXC = currXC;
                        } else if (currYC < prevYC) {
                            addSCs(false, currYC, minXC, maxXC);
                            minYC = currYC;
                        } else if (currYC > prevYC) {
                            purgeSCs(-1, prevYC);
                            minYC = currYC;
                        }
                    } else if (currYC >= originalYC) {
                        if (currXC < prevXC) {
                            addSCs(true, currXC, minYC, maxYC);
                            minXC = currXC;
                        } else if (currXC > prevXC) {
                            purgeSCs(prevXC, -1);
                            minXC = currXC;
                        } else if (currYC > prevYC) {
                            addSCs(false, currYC, minXC, maxXC);
                            maxYC = currYC;
                        } else if (currYC < prevYC) {
                            purgeSCs(-1, prevYC);
                            maxYC = currYC;
                        }
                    } else {
                        if (currXC < prevXC) {
                            addSCs(true, currXC, minYC, maxYC);
                            minXC = currXC;
                        } else if (currXC > prevXC) {
                            purgeSCs(prevXC, -1);
                            minXC = currXC;
                        } else if (currYC < prevYC) {
                            addSCs(false, currYC, minXC, maxXC);
                            minYC = currYC;
                        } else if (currYC > prevYC) {
                            purgeSCs(-1, prevYC);
                            minYC = currYC;
                        }
                    }

                    if (currXC == originalXC && currXC > prevXC)
                        purgeSCs(prevXC, -1);
                    else if (currYC == originalYC && currYC > prevYC)
                        purgeSCs(-1, prevYC);

                    coordsCell.setCoords(maxXC, minXC, maxYC, minYC);
                }
            }
        }
    }

    private static void addSCs(boolean isAddingCol, int currC, int minC, int maxC) {
        System.out.println("Adding cells to selectedCoords...");
        if (isAddingCol) for (int i = minC; i <= maxC; i++) {
            selectedCoords.add(new int[]{currC, i});
            System.out.println(currC + ", " + i);
        }
        else for (int i = minC; i <= maxC; i++) {
            selectedCoords.add(new int[]{i, currC});
            System.out.println(currC + ", " + i);
        }
    }

    private static void purgeSCs(int col, int row) {
        System.out.println("Removing cells from selectedCoords...");
        if (col != -1)
            selectedCoords.removeIf(c -> c[0] == col);
        else
            selectedCoords.removeIf(c -> c[1] == row);
    }

    private static void textInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.readCell(camera.picture.data());
                statusBar.setMode(MODE[3]);
            }
            case LEFT, DOWN, UP, RIGHT, ENTER, TAB -> {
                cellSelector.setSelectedCell(new Cell(
                        cellSelector.getXCoord(),
                        cellSelector.getYCoord(),
                        cellSelector.getSelectedCell().txt()
                ));
                sheet.addCell(cellSelector.getSelectedCell());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                recordedCell.add(cellSelector.getSelectedCell());
                switch (event.getCode()) {
                    case LEFT -> moveLeft();
                    case DOWN, ENTER -> moveDown();
                    case UP -> moveUp();
                    case RIGHT, TAB -> moveRight();
                }
                statusBar.setMode(MODE[3]);
            }
            case BACK_SPACE -> {
                if (cellSelector.getSelectedCell().txt().equals("")) {
                    infoBar.setErrorTxt("CELL IS EMPTY");
                    infoBar.setError(true);
                }
                else {
                    cellSelector.getSelectedCell().setTxt(
                            cellSelector.getSelectedCell().txt().substring(0,
                                    cellSelector.getSelectedCell().txt().length() - 1)
                    );
                    cellSelector.draw(gc);
                }
            }
            default -> cellSelector.draw(gc, event.getText());
        }
    }

    private static void formulaInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.readCell(camera.picture.data());
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
            }
            case ENTER -> {
                cellSelector.setSelectedCell(new Cell(
                        cellSelector.getXCoord(),
                        cellSelector.getYCoord(),
                        cellSelector.getSelectedCell().formula().interpret(sheet),
                        cellSelector.getSelectedCell().formula()
                ));
                sheet.addCell(cellSelector.getSelectedCell());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
                cellSelector.readCell(camera.picture.data());
                recordedCell.add(cellSelector.getSelectedCell());
            }
            case BACK_SPACE -> cellSelector.getSelectedCell().formula().setTxt(
                    cellSelector.getSelectedCell().formula().getTxt().substring(
                            0, cellSelector.getSelectedCell().formula().getTxt().length() - 1
                    )
            );
            default -> cellSelector.getSelectedCell().formula().setTxt(
                    cellSelector.getSelectedCell().formula().getTxt() +
                            event.getText()
            );
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        sheet = new Sheet();
        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();
        reset();
    }

    public static void reset() {
        camera = new Camera(
                DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                CANVAS_W - DEFAULT_CELL_W,
                CANVAS_H - 3 * DEFAULT_CELL_H - 4,
                DEFAULT_CELL_C,
                DEFAULT_CELL_W,
                DEFAULT_CELL_H
        );
        cellSelector = new CellSelector(
                2 * DEFAULT_CELL_W,
                2 * DEFAULT_CELL_H,
                DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                Color.DARKGRAY
        );
        coordsCell = new CoordsCell(
                0,
                0,
                DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                Color.GRAY
        );
        firstCol = new FirstCol(
                0,
                DEFAULT_CELL_H,
                DEFAULT_CELL_W,
                CANVAS_H - 2 * DEFAULT_CELL_H - 4,
                Color.SILVER
        );
        firstRow = new FirstRow(
                DEFAULT_CELL_W,
                0,
                CANVAS_W - DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                Color.SILVER
        );
        infoBar = new InfoBar(
                0,
                CANVAS_H - DEFAULT_CELL_H,
                CANVAS_W,
                DEFAULT_CELL_H,
                DEFAULT_CELL_C
        );
        statusBar = new StatusBar(
                0,
                CANVAS_H - 2 * DEFAULT_CELL_H - 4,
                CANVAS_W,
                DEFAULT_CELL_H + 4,
                Color.GRAY
        );

        command = new Command("");
        selectedCoords = new ArrayList<>();

        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        camera.ready();
        coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        coordsCell.draw(gc);
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        statusBar.draw(gc);
        cellSelector.readCell(camera.picture.data());
        cellSelector.draw(gc);
        infoBar.draw(gc, cellSelector.getSelectedCell());
    }
}
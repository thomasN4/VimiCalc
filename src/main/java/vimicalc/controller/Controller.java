package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;
import vimicalc.model.Sheet;
import vimicalc.view.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static int CANVAS_W;
    public static int CANVAS_H;
    public static final int DEFAULT_CELL_H = 23;
    public static final int DEFAULT_CELL_W = DEFAULT_CELL_H * 4;
    public static final Color DEFAULT_CELL_C = Color.LIGHTGRAY;
    public static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};

    @FXML private Canvas canvas;

    public static Camera camera;
    public static Command command;
    public static CoordsCell coordsCell;
    public static FirstCol firstCol;
    public static FirstRow firstRow;
    public static GraphicsContext gc;
    public static InfoBar infoBar;
    public static CellSelector cellSelector;
    public static ArrayList<CellSelector> cellSelectors;
    public static Sheet sheet;
    public static StatusBar statusBar;

    public static void moveLeft() {
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
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsX(-cellSelector.getW());
            if (camera.getAbsX() < 0)
                while (camera.getAbsX() != 0)
                    camera.updateAbsX(1);
            firstRow.draw(gc, camera.getAbsX());
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    public static void moveUp() {
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
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsY(-cellSelector.getH());
            if (camera.getAbsY() < 0)
                while (camera.getAbsY() != 0)
                    camera.updateAbsY(1);
            firstCol.draw(gc, camera.getAbsY());
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    public static void moveDown() {
        cellSelector.updateYCoord(1);
        if (cellSelector.getY() != camera.picture.getH()) {
            cellSelector.updateY(cellSelector.getH());
            if (cellSelector.getY() > camera.picture.getH()) {
                while (cellSelector.getY() != camera.picture.getH()) {
                    cellSelector.updateY(-1);
                    camera.updateAbsY(1);
                }
                firstCol.draw(gc, camera.getAbsY());
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsY(cellSelector.getH());
            firstCol.draw(gc, camera.getAbsY());
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    public static void moveRight() {
        cellSelector.updateXCoord(1);
        if (cellSelector.getX() != camera.picture.getW()) {
            cellSelector.updateX(cellSelector.getW());
            if (cellSelector.getX() > camera.picture.getW()) {
                while (cellSelector.getX() != camera.picture.getW()) {
                    cellSelector.updateX(-1);
                    camera.updateAbsX(1);
                }
                firstRow.draw(gc, camera.getAbsX());
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            }
        } else {
            camera.updateAbsX(cellSelector.getW());
            firstRow.draw(gc, camera.getAbsX());
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
    }

    public static void onKeyPressed(@NotNull KeyEvent event) {
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
                        sheet.deleteCell(coordsCell.getCoords());
                        camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.setSelectedCell(cellSelector.getEmptyCell());
                    }
                    case A, I -> statusBar.setMode(MODE[2]);
                    case ESCAPE -> statusBar.setMode(MODE[3]);
                    case EQUALS -> {
                        statusBar.setMode(MODE[1]);
                        infoBar.setEnteringFormula(true);
                        if (cellSelector.getSelectedCell().formula() == null)
                            cellSelector.getSelectedCell().setFormula(
                                    new Formula("")
                            );
                    }
                    case V -> {
                        statusBar.setMode(MODE[4]);
                        cellSelectors.add(cellSelector);
                    }
                    case SEMICOLON -> {
                        statusBar.setMode(MODE[0]);
                        infoBar.setEnteringCommand(true);
                    }
                }
            }
            case 'V' -> visualSelection(event);
        }

        System.out.println("     sC.x: "+ cellSelector.getX()     +", yCoord: "+ cellSelector.getY());
        System.out.println("sC.xCoord: "+ cellSelector.getXCoord()+", sC.yCoord: "+ cellSelector.getYCoord());
        System.out.println(" cam.absX: "+camera.getAbsX()         +", cam.absY: "+camera.getAbsY());
        System.out.println("    Cells: "+sheet.getCells());
        System.out.println("========================================");

        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        statusBar.draw(gc);
        infoBar.setKeyStroke(event.getCode().toString());
        infoBar.draw(gc, cellSelector.getSelectedCell());
        if (statusBar.getMode().equals(MODE[3])) {
            cellSelector.draw(gc);
            coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        }
        else if (statusBar.getMode().equals(MODE[4])) {
            System.out.println("Selected cells = {");
            cellSelectors.forEach(c -> {
                c.draw(gc);
                System.out.println("\txCoord, yCoord: " + c.getXCoord() + ", " + c.getYCoord());
            });
            System.out.println('}');
        }
        coordsCell.draw(gc);
    }

    private static void commandInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                command = new Command("");
                if (infoBar.isEnteringCommandInVISUAL()) {
                    cellSelectors = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);
                }
                infoBar.setEnteringCommand(false);
                statusBar.setMode(MODE[3]);
            }
            case ENTER -> {
                command.interpret(sheet);
                command = new Command("");
                if (infoBar.isEnteringCommandInVISUAL()) {
                    cellSelectors = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);
                }
                infoBar.setEnteringCommand(false);
                statusBar.setMode(MODE[3]);
            }
            case BACK_SPACE -> command.setTxt(
                    command.getTxt().substring(0, command.getTxt().length()-1)
                );
            default -> command.setTxt(command.getTxt() + event.getText());
        }
        infoBar.setCommandTxt(command.getTxt());
    }

    private static void visualSelection(@NotNull KeyEvent event) {
        if (infoBar.isEnteringCommandInVISUAL()) {
            commandInput(event);
        } else if (event.getCode() == KeyCode.ESCAPE) {
            statusBar.setMode(MODE[3]);
            cellSelectors = new ArrayList<>();
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            camera.ready();
            cellSelector.readCell(camera.picture.data());
            cellSelector.draw(gc);
        } else if (event.getCode() == KeyCode.SEMICOLON) {
            infoBar.setEnteringCommandInVISUAL(true);
            command = new Command("");
        } else {
            int prevXC = cellSelector.getXCoord();
            int prevYC = cellSelector.getYCoord();

            int maxXC;
            int minXC;
            int maxYC;
            int minYC;
            if (cellSelectors.size() > 1) {
                maxXC = Integer.MIN_VALUE;
                minXC = Integer.MAX_VALUE;
                maxYC = Integer.MIN_VALUE;
                minYC = Integer.MAX_VALUE;
                for (CellSelector c : cellSelectors) {
                    if (c.getXCoord() > maxXC) maxXC = c.getXCoord();
                    if (c.getXCoord() < minXC) minXC = c.getXCoord();
                    if (c.getYCoord() > maxYC) maxYC = c.getYCoord();
                    if (c.getYCoord() < minYC) minYC = c.getYCoord();
                }
            } else {
                maxXC = prevXC;
                minXC = maxXC;
                maxYC = prevYC;
                minYC = maxYC;
            }
            System.out.println("maxXC: "+ maxXC);
            System.out.println("minXC: "+ minXC);
            System.out.println("maxYC: "+ maxYC);
            System.out.println("minYC: "+ minYC);

            switch (event.getCode()) {
                case H, LEFT, BACK_SPACE -> moveLeft();
                case J, DOWN, ENTER -> moveDown();
                case K, UP -> moveUp();
                case L, RIGHT, TAB, SPACE -> moveRight();
            }
            int currXC = cellSelector.getXCoord();
            int currYC = cellSelector.getYCoord();
            System.out.println("currXC: "+ currXC);
            System.out.println("currYC: "+ currYC);

            int newMaxXC = maxXC;
            int newMinXC = minXC;
            int newMaxYC = maxYC;
            int newMinYC = minYC;

            if (currXC >= minXC && currYC >= minYC) {
                ArrayList<CellSelector> addedCSs = new ArrayList<>();
                if (currXC > maxXC) {
                    for (int i = minYC; i <= maxYC; i++) {
                        CellSelector c = new CellSelector(
                                currXC,
                                i,
                                cellSelector.getW(),
                                cellSelector.getH(),
                                cellSelector.getC(),
                                DEFAULT_CELL_W,
                                DEFAULT_CELL_H,
                                camera.picture.getW(),
                                camera.picture.getH(),
                                camera.picture.data()
                        );
                        System.out.println("c.getXCoord() = " + c.getXCoord());
                        System.out.println("c.getYCoord() = " + c.getYCoord());
                        addedCSs.add(c);
                    }
                    cellSelectors.addAll(addedCSs);
                    cellSelectors.forEach(System.out::println);
                    newMaxXC = currXC;
                } else if (currXC < prevXC) {
                    cellSelectors.removeIf(c -> c.getXCoord() > currXC);
                    newMaxXC = currXC;
                } else if (currYC > maxYC) {
                    for (int i = minXC; i <= maxXC; i++) {
                        addedCSs.add(new CellSelector(
                                i,
                                currYC,
                                cellSelector.getW(),
                                cellSelector.getH(),
                                cellSelector.getC(),
                                DEFAULT_CELL_W,
                                DEFAULT_CELL_H,
                                camera.picture.getW(),
                                camera.picture.getH(),
                                camera.picture.data()
                        ));
                    }
                    cellSelectors.addAll(addedCSs);
                    newMaxYC = currYC;
                } else if (currYC < prevYC) {
                    cellSelectors.removeIf(c -> c.getYCoord() > currYC);
                    newMaxYC = currYC;
                }
            } else {
                cellSelectors = new ArrayList<>();
                if (currXC != prevXC) {
                    for (int i = minYC; i <= maxYC; i++) {
                        cellSelectors.add(new CellSelector(
                                currXC,
                                i,
                                cellSelector.getW(),
                                cellSelector.getH(),
                                cellSelector.getC(),
                                DEFAULT_CELL_W,
                                DEFAULT_CELL_H,
                                camera.picture.getW(),
                                camera.picture.getH(),
                                camera.picture.data()
                        ));
                    }
                    newMaxXC = prevXC;
                    newMinXC = currXC;
                } else {
                    for (int i = minXC; i <= maxXC; i++) {
                        cellSelectors.add(new CellSelector(
                                i,
                                currYC,
                                cellSelector.getW(),
                                cellSelector.getH(),
                                cellSelector.getC(),
                                DEFAULT_CELL_W,
                                DEFAULT_CELL_H,
                                camera.picture.getW(),
                                camera.picture.getH(),
                                camera.picture.data()
                        ));
                    }
                    newMaxYC = prevYC;
                    newMinYC = currYC;
                }
            }
            coordsCell.setCoords(newMaxXC, newMinXC, newMaxYC, newMinYC);
        }
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
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
                switch (event.getCode()) {
                    case LEFT -> moveLeft();
                    case DOWN, ENTER -> moveDown();
                    case UP -> moveUp();
                    case RIGHT, TAB -> moveRight();
                }
                statusBar.setMode(MODE[3]);
            }
            case BACK_SPACE -> cellSelector.getSelectedCell().setTxt(
                    cellSelector.getSelectedCell().txt().substring(0,
                        cellSelector.getSelectedCell().txt().length() - 1)
                );
            default -> cellSelector.draw(gc, event.getText());
        }
    }

    private static void formulaInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.getSelectedCell().setFormula(new Formula(""));
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
                camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
                cellSelector.readCell(camera.picture.data());
            }
            case BACK_SPACE -> cellSelector.getSelectedCell().formula().setTxt(
                cellSelector.getSelectedCell().formula().getTxt().substring(
                    0, cellSelector.getSelectedCell().formula().getTxt().length()-1
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
        command = new Command("");

        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();

        reset();
    }

    public static void reset() {
        camera = new Camera(
                DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                CANVAS_W-DEFAULT_CELL_W,
                CANVAS_H-3*DEFAULT_CELL_H-4,
                DEFAULT_CELL_C
        );
        cellSelector = new CellSelector(
                2*DEFAULT_CELL_W,
                2*DEFAULT_CELL_H,
                DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                Color.DARKGRAY
        );
        cellSelectors = new ArrayList<>();
        coordsCell = new CoordsCell(0, 0, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.GRAY);
        firstCol = new FirstCol(
                0,
                DEFAULT_CELL_H,
                DEFAULT_CELL_W,
                CANVAS_H-2*DEFAULT_CELL_H-4,
                Color.SILVER
        );
        firstRow = new FirstRow(
                DEFAULT_CELL_W,
                0,
                CANVAS_W-DEFAULT_CELL_W,
                DEFAULT_CELL_H,
                Color.SILVER
        );
        infoBar = new InfoBar(
                0,
                CANVAS_H-DEFAULT_CELL_H,
                CANVAS_W,
                DEFAULT_CELL_H,
                DEFAULT_CELL_C
        );
        statusBar = new StatusBar(
                0,
                CANVAS_H-2*DEFAULT_CELL_H-4,
                CANVAS_W,
                DEFAULT_CELL_H+4,
                Color.GRAY
        );

        camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
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
package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import vimicalc.model.Formula;
import vimicalc.model.Sheet;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

import static vimicalc.Main.isNumber;

public class Controller implements Initializable {
    public static int CANVAS_W;
    public static int CANVAS_H;
    public static final int DEFAULT_CELL_H = 23;
    public static final int DEFAULT_CELL_W = DEFAULT_CELL_H *4;
    public static final Color DEFAULT_CELL_C = Color.LIGHTGRAY;
    public static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};

    @FXML private Canvas canvas;

    public static Camera camera;
    public static CoordsCell coordsCell;
    public static FirstCol firstCol;
    public static FirstRow firstRow;
    public static GraphicsContext gc;
    public static InfoBar infoBar;
    public static CellSelector cellSelector;
    public static Sheet sheet;
    public static StatusBar statusBar;

    public static void moveLeft() {
        if (cellSelector.getxCoord() != 1)
            cellSelector.updateXCoord(-1);
        if (cellSelector.getX() != DEFAULT_CELL_W) {
            cellSelector.updateX(-DEFAULT_CELL_W);
            if (cellSelector.getX() < DEFAULT_CELL_W) {
                while (cellSelector.getX() != DEFAULT_CELL_W) {
                    cellSelector.updateX(1);
                    camera.updateAbsX(-1);
                }
                firstRow.draw(gc, camera.getAbsX());
            }
        } else {
            camera.updateAbsX(-DEFAULT_CELL_W);
            if (camera.getAbsX() < 0)
                while (camera.getAbsX() != 0)
                    camera.updateAbsX(1);
            firstRow.draw(gc, camera.getAbsX());
        }
    }

    public static void moveUp() {
        if (cellSelector.getyCoord() != 1)
            cellSelector.updateYCoord(-1);
        if (cellSelector.getY() != DEFAULT_CELL_H) {
            cellSelector.updateY(-DEFAULT_CELL_H);
            if (cellSelector.getY() < DEFAULT_CELL_H) {
                while (cellSelector.getY() != DEFAULT_CELL_H) {
                    cellSelector.updateY(1);
                    camera.updateAbsY(-1);
                }
                firstCol.draw(gc, camera.getAbsY());
            }
        } else {
            camera.updateAbsY(-DEFAULT_CELL_H);
            if (camera.getAbsY() < 0)
                while (camera.getAbsY() != 0)
                    camera.updateAbsY(1);
            firstCol.draw(gc, camera.getAbsY());
        }
    }

    public static void moveDown() {
        cellSelector.updateYCoord(1);
        if (cellSelector.getY() != camera.picture.getH()) {
            cellSelector.updateY(DEFAULT_CELL_H);
            if (cellSelector.getY() > camera.picture.getH()) {
                while (cellSelector.getY() != camera.picture.getH()) {
                    cellSelector.updateY(-1);
                    camera.updateAbsY(1);
                }
                firstCol.draw(gc, camera.getAbsY());
            }
        } else {
            camera.updateAbsY(DEFAULT_CELL_H);
            firstCol.draw(gc, camera.getAbsY());
        }
    }

    public static void moveRight() {
        cellSelector.updateXCoord(1);
        if (cellSelector.getX() != camera.picture.getW()) {
            cellSelector.updateX(DEFAULT_CELL_W);
            if (cellSelector.getX() > camera.picture.getW()) {
                while (cellSelector.getX() != camera.picture.getW()) {
                    cellSelector.updateX(-1);
                    camera.updateAbsX(1);
                }
                firstRow.draw(gc, camera.getAbsX());
            }
        } else {
            camera.updateAbsX(DEFAULT_CELL_W);
            firstRow.draw(gc, camera.getAbsX());
        }
    }

    public static void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[1])) {
            formulaInput(event);
        } else if (statusBar.getMode().equals(MODE[3]) || statusBar.getMode().equals(MODE[4])) {
            switch (event.getCode()) {
                case H, LEFT, BACK_SPACE -> moveLeft();
                case J, DOWN, ENTER -> moveDown();
                case K, UP -> moveUp();
                case L, RIGHT, TAB, SPACE -> moveRight();
                case D, DELETE -> {
                    sheet.deleteCell(coordsCell.getCoords());
                    cellSelector.setInsertedTxt("");
                }
                case A, I -> statusBar.setMode(MODE[2]);
                case ESCAPE -> statusBar.setMode(MODE[3]);
                case EQUALS -> {
                    statusBar.setMode(MODE[1]);
                    infoBar.setEnteringFormula(true);
                    cellSelector.getSelectedCell().setFormula(new Formula(""));
                }
            }
        } else if (statusBar.getMode().equals(MODE[2])) {
            textInput(event);
        }

        // Temporaire:
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());

        System.out.println("     sC.x: "+ cellSelector.getX()     +", yCoord: "+ cellSelector.getY());
        System.out.println("sC.xCoord: "+ cellSelector.getxCoord()+", sC.yCoord: "+ cellSelector.getyCoord());
        System.out.println(" cam.absX: "+camera.getAbsX()         +", cam.absY: "+camera.getAbsY());
        System.out.println("    Cells: "+sheet.getCells());
        System.out.println("selectedC: "+ cellSelector.getSelectedCell().toString());
        System.out.println("========================================");
        if (statusBar.getMode().equals(MODE[3])) {
            camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
            coordsCell.setCoords(cellSelector.getxCoord(), cellSelector.getyCoord());
            coordsCell.draw(gc);
            cellSelector.readCell(camera.picture.getVisibleCells());
        }
        cellSelector.draw(gc);
        statusBar.draw(gc);
        infoBar.setKeyStroke(event.getCode().toString());
        infoBar.draw(gc, cellSelector.getSelectedCell());
    }

    private static void textInput(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.setInsertedTxt("");
                statusBar.setMode(MODE[3]);
            }
            case LEFT, DOWN, UP, RIGHT, ENTER, TAB -> {
//                System.out.println("\"The cell was empty\" == "+sheet.findCell(coordsCell.getCoords()).equals(cellSelector.getEmptyCell()));
//                System.out.println("\"The empty cell and the selected cell are identical for some fucking reason\" == "+
//                        (cellSelector.getSelectedCell().equals(cellSelector.getEmptyCell())));
//                System.out.println("Empty cell: "+ cellSelector.getEmptyCell());
//                System.out.println("Empty cell's text: "+ cellSelector.getEmptyCell().txt());
//                System.out.println("Selected cell: "+ cellSelector.getSelectedCell());
                sheet.updateCell(coordsCell.getCoords(), cellSelector.getSelectedCell());
                switch (event.getCode()) {
                    case LEFT -> moveLeft();
                    case DOWN, ENTER -> moveDown();
                    case UP -> moveUp();
                    case RIGHT, TAB -> moveRight();
                }
                statusBar.setMode(MODE[3]);
            }
            case BACK_SPACE -> cellSelector.delCharInTxt();
            default -> cellSelector.draw(gc, event.getText());
        }
    }

    private static void formulaInput(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.getSelectedCell().setFormula(new Formula(""));
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
            }
            case ENTER -> {
                String result = cellSelector.getSelectedCell().formula().interpret(sheet);
                System.out.println("Result: "+result);
                if (isNumber(result)) {
                    cellSelector.getSelectedCell().setTxt(result);
//                    sheet.updateCell(coordsCell.getCoords(), cellSelector.getSelectedCell());
                    // wtf:
                    sheet.modifyCellFormula(cellSelector.getxCoord()
                            , cellSelector.getyCoord()
                            , result
                            , cellSelector.getSelectedCell().formula());
//                    sheet.modifyCellFormula(cellSelector.getSelectedCell());
                    System.out.println("Saved cell: "+ cellSelector.getSelectedCell() +
                            "\nFormula: "+ cellSelector.getSelectedCell().formula().getTxt());
                }
                cellSelector.getSelectedCell().setFormula(new Formula("0"));
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
            }
            case BACK_SPACE -> cellSelector.getSelectedCell().formula().setTxt(
                    cellSelector.getSelectedCell().formula().getTxt().substring(
                            0, cellSelector.getSelectedCell().formula().getTxt().length()-1)
            );
            default -> cellSelector.getSelectedCell().updateFormula(event.getText());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        sheet = new Sheet();

        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();

        camera = new Camera(DEFAULT_CELL_W, DEFAULT_CELL_H, CANVAS_W-DEFAULT_CELL_W, CANVAS_H-3*DEFAULT_CELL_H-4, DEFAULT_CELL_C);
        coordsCell = new CoordsCell(0, 0, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.GRAY);
        firstCol = new FirstCol(0, DEFAULT_CELL_H, DEFAULT_CELL_W, CANVAS_H-2*DEFAULT_CELL_H-4, Color.SILVER);
        firstRow = new FirstRow(DEFAULT_CELL_W, 0, CANVAS_W-DEFAULT_CELL_W, DEFAULT_CELL_H, Color.SILVER);
        infoBar = new InfoBar(0, CANVAS_H-DEFAULT_CELL_H, CANVAS_W, DEFAULT_CELL_H, DEFAULT_CELL_C);
        statusBar = new StatusBar(0, CANVAS_H-2*DEFAULT_CELL_H-4, CANVAS_W, DEFAULT_CELL_H+4, Color.GRAY);
        cellSelector = new CellSelector(2*DEFAULT_CELL_W, 2*DEFAULT_CELL_H, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.DARKGRAY);

        camera.picture.take(gc, sheet, camera.getAbsX(), camera.getAbsY());
        coordsCell.setCoords(cellSelector.getxCoord(), cellSelector.getyCoord());
        coordsCell.draw(gc);
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        statusBar.draw(gc);
        cellSelector.readCell(camera.picture.getVisibleCells());
        cellSelector.draw(gc);
        infoBar.draw(gc, cellSelector.getSelectedCell());
    }
}
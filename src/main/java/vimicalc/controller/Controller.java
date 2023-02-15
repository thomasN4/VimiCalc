package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import vimicalc.model.Interpreter;
import vimicalc.model.Sheet;
import vimicalc.model.Cell;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

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
    public static Interpreter interpreter;
    public static SelectedCell selectedCell;
    public static Sheet sheet;
    public static StatusBar statusBar;

    public static void moveLeft() {
        if (selectedCell.getxCoord() != 1)
            selectedCell.updateXCoord(-1);
        if (selectedCell.getX() != DEFAULT_CELL_W) {
            selectedCell.updateX(-DEFAULT_CELL_W);
            if (selectedCell.getX() < DEFAULT_CELL_W) {
                while (selectedCell.getX() != DEFAULT_CELL_W) {
                    selectedCell.updateX(1);
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
        if (selectedCell.getyCoord() != 1)
            selectedCell.updateYCoord(-1);
        if (selectedCell.getY() != DEFAULT_CELL_H) {
            selectedCell.updateY(-DEFAULT_CELL_H);
            if (selectedCell.getY() < DEFAULT_CELL_H) {
                while (selectedCell.getY() != DEFAULT_CELL_H) {
                    selectedCell.updateY(1);
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
        selectedCell.updateYCoord(1);
        if (selectedCell.getY() != camera.picture.getH()) {
            selectedCell.updateY(DEFAULT_CELL_H);
            if (selectedCell.getY() > camera.picture.getH()) {
                while (selectedCell.getY() != camera.picture.getH()) {
                    selectedCell.updateY(-1);
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
        selectedCell.updateXCoord(1);
        if (selectedCell.getX() != camera.picture.getW()) {
            selectedCell.updateX(DEFAULT_CELL_W);
            if (selectedCell.getX() > camera.picture.getW()) {
                while (selectedCell.getX() != camera.picture.getW()) {
                    selectedCell.updateX(-1);
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
                case H, LEFT -> moveLeft();
                case J, ENTER, DOWN -> moveDown();
                case K, UP -> moveUp();
                case L, RIGHT -> moveRight();
                case D -> {
                    sheet.deleteCell(selectedCell.getxCoord(), selectedCell.getyCoord());
                    selectedCell.setInsertedTxt("");
                }
                case A, I -> statusBar.setMode(MODE[2]);
                case ESCAPE -> statusBar.setMode(MODE[3]);
                case EQUALS -> {
                    statusBar.setMode(MODE[1]);
                    infoBar.setEnteringFormula(true);
                }
            }
        } else if (statusBar.getMode().equals(MODE[2])) {
            switch (event.getCode()) {
                case ESCAPE -> {
                    statusBar.setMode(MODE[3]);
                    selectedCell.setInsertedTxt("");
                }
                case ENTER, LEFT, DOWN, UP, RIGHT -> {
                    statusBar.setMode(MODE[3]);
                    sheet.createCell(selectedCell.getxCoord(), selectedCell.getyCoord(), selectedCell.getInsertedTxt());
                    switch (event.getCode()) {
                        case LEFT -> moveLeft();
                        case DOWN, ENTER -> moveDown();
                        case UP -> moveUp();
                        case RIGHT -> moveRight();
                    }
                    System.out.println("New txt cell: " + sheet.cells);
                }
                case BACK_SPACE -> {
                    selectedCell.delete();
                    System.out.println("Deleting a character.");
                }
                default -> selectedCell.draw(gc, event.getText());
            }
        }

        // Temporaire:
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());

        System.out.println("     sC.x: "+selectedCell.getX()     +"   , yCoord: "+selectedCell.getY());
        System.out.println("sC.xCoord: "+selectedCell.getxCoord()+", sC.yCoord: "+selectedCell.getyCoord());
        System.out.println("camera.absX: "+camera.getAbsX()+", camera.absY: "+camera.getAbsY());
        System.out.println("========================================");
        coordsCell.setCoords(selectedCell.getxCoord(), selectedCell.getyCoord());
        coordsCell.draw(gc);
        camera.picture.take(gc, sheet.cells, camera.getAbsX(), camera.getAbsY());
        selectedCell.readCell(sheet.cells);
        selectedCell.draw(gc);
        statusBar.draw(gc);
        if (selectedCell.getX() < camera.picture.getW())
            infoBar.setC(DEFAULT_CELL_C);
        infoBar.setKeyStroke(event.getCode().toString());
        infoBar.draw(gc);
    }

    private static void formulaInput(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                infoBar.setFormula("");
                infoBar.setEnteringFormula(false);
                statusBar.setMode(MODE[3]);
            }
            case ENTER -> {
                interpreter.setRawFormula(infoBar.getFormula());
                interpreter.interpret();
                sheet.createCell(selectedCell.getxCoord(), selectedCell.getyCoord(), interpreter.getNumericResult(), interpreter.getLexedFormula());
                infoBar.setFormula("");
                statusBar.setMode(MODE[3]);
            }
            default -> {
                infoBar.updateFormula(event.getText());
                selectedCell.draw(gc, event.getText());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        interpreter = new Interpreter();
        sheet = new Sheet();

        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();

        camera = new Camera(DEFAULT_CELL_W, DEFAULT_CELL_H, CANVAS_W-DEFAULT_CELL_W, CANVAS_H-3*DEFAULT_CELL_H-4, DEFAULT_CELL_C);
        coordsCell = new CoordsCell(0, 0, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.GRAY);
        firstCol = new FirstCol(0, DEFAULT_CELL_H, DEFAULT_CELL_W, CANVAS_H-2*DEFAULT_CELL_H-4, Color.SILVER);
        firstRow = new FirstRow(DEFAULT_CELL_W, 0, CANVAS_W-DEFAULT_CELL_W, DEFAULT_CELL_H, Color.SILVER);
        infoBar = new InfoBar(0, CANVAS_H-DEFAULT_CELL_H, CANVAS_W, DEFAULT_CELL_H, DEFAULT_CELL_C);
        statusBar = new StatusBar(0, CANVAS_H-2*DEFAULT_CELL_H-4, CANVAS_W, DEFAULT_CELL_H+4, Color.GRAY);
        selectedCell = new SelectedCell(2*DEFAULT_CELL_W, 2*DEFAULT_CELL_H, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.DARKGRAY);

        camera.picture.take(gc, sheet.cells, camera.getAbsX(), camera.getAbsY());
        coordsCell.setCoords(selectedCell.getxCoord(), selectedCell.getyCoord());
        coordsCell.draw(gc);
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);
    }
}
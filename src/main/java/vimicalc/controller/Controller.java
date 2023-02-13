package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import vimicalc.model.Sheet;
import vimicalc.model.TextCell;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static int CANVAS_W;
    public static int CANVAS_H;
    public static final int DEFAULT_CELL_H = 23;
    public static final int DEFAULT_CELL_W = DEFAULT_CELL_H *4;
    public static final Color DEFAULT_CELL_C = Color.WHITE;
    public static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};

    @FXML private Canvas canvas;

    public static Camera camera;
    public static CoordsCell coordsCell;
    public static FirstCol firstCol;
    public static FirstRow firstRow;
    public static GraphicsContext gc;
    public static InfoBar infoBar;
    public static SelectedCell selectedCell;
    public static StatusBar statusBar;

    public static Sheet sheet;

    public static void moveLeft() {
        if (selectedCell.getxCoord() != 1)
            selectedCell.updateXCoord(-1);
        selectedCell.updateX(-DEFAULT_CELL_W);
        if (selectedCell.getX() < DEFAULT_CELL_W) {
            camera.updateTable_x(-DEFAULT_CELL_W);
            while (selectedCell.getX() != DEFAULT_CELL_W) {
                selectedCell.updateX(1);
                camera.updateTable_x(1);
            }
            firstRow.draw(gc, camera.getAbsX());
        }
    }

    public static void moveUp() {
        if (selectedCell.getyCoord() != 1)
            selectedCell.updateYCoord(-1);
        selectedCell.updateY(-DEFAULT_CELL_H);
        if (selectedCell.getY() < DEFAULT_CELL_H) {
            camera.updateTable_y(-DEFAULT_CELL_H);
            while (selectedCell.getY() != DEFAULT_CELL_H) {
                selectedCell.updateY(1);
                camera.updateTable_y(1);
            }
            firstCol.draw(gc, camera.getAbsY());
        }
    }

    public static void moveDown() {
        selectedCell.updateYCoord(1);
        selectedCell.updateY(DEFAULT_CELL_H);
        if (selectedCell.getY() > camera.picture.getH()) {
            camera.updateTable_y(DEFAULT_CELL_H);
            while (selectedCell.getY() != camera.picture.getH()) {
                selectedCell.updateY(-1);
                camera.updateTable_y(-1);
            }
            firstCol.draw(gc, camera.getAbsY());
        }
    }

    public static void moveRight() {
        selectedCell.updateXCoord(1);
        selectedCell.updateX(DEFAULT_CELL_W);
        if (selectedCell.getX() > camera.picture.getW()) {
            camera.updateTable_x(DEFAULT_CELL_W);
            while (selectedCell.getX() != camera.picture.getW()) {
                selectedCell.updateX(-1);
                camera.updateTable_x(-1);
            }
            firstCol.draw(gc, camera.getAbsX());
        }
    }

    public static void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[3]) || statusBar.getMode().equals(MODE[4])) {
            switch (event.getCode()) {
                case H, LEFT -> moveLeft();
                case J, ENTER, DOWN -> moveDown();
                case K, UP -> moveUp();
                case L, RIGHT -> moveRight();
                case A, I -> statusBar.setMode(MODE[2]);
                case ESCAPE -> statusBar.setMode(MODE[3]);
            }
        } else if (statusBar.getMode().equals(MODE[2])) {
            if (event.getCode() == KeyCode.ESCAPE) {
                statusBar.setMode(MODE[3]);
                selectedCell.setInsertedTxt("");
            } else if (event.getCode() == KeyCode.ENTER) {
                statusBar.setMode(MODE[3]);
                sheet.textCells.add(new TextCell(selectedCell.getxCoord(),
                        selectedCell.getyCoord(),
                        selectedCell.getInsertedTxt()));
                moveDown();
                System.out.println("New text cell: "+sheet.textCells);
            } else selectedCell.draw(gc, event.getText());
        }

        System.out.println("     sC.x: "+selectedCell.getX()     +"   , yCoord: "+selectedCell.getY());
        System.out.println("sC.xCoord: "+selectedCell.getxCoord()+", sC.yCoord: "+selectedCell.getyCoord());
        System.out.println("table_x: "+camera.getAbsX()+", table_y: "+camera.getAbsY());
        coordsCell.setCoords(selectedCell.getxCoord(), selectedCell.getyCoord());
        coordsCell.draw(gc);
        camera.picture.take(gc, sheet.textCells, camera.getAbsX(), camera.getAbsY());
        selectedCell.readCell(sheet.textCells);
        selectedCell.draw(gc);
        statusBar.draw(gc);
        infoBar.draw(gc);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();

        camera = new Camera(DEFAULT_CELL_W, DEFAULT_CELL_H, CANVAS_W-DEFAULT_CELL_W, CANVAS_H-3*DEFAULT_CELL_H-4, DEFAULT_CELL_C, 0, 0);
        coordsCell = new CoordsCell(0, 0, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.DARKGRAY, "B2");
        firstCol = new FirstCol(0, DEFAULT_CELL_H, DEFAULT_CELL_W, CANVAS_H-2*DEFAULT_CELL_H-4, Color.LIGHTGRAY);
        firstRow = new FirstRow(DEFAULT_CELL_W, 0, CANVAS_W, DEFAULT_CELL_H, Color.LIGHTGRAY);
        infoBar = new InfoBar(0, CANVAS_H-DEFAULT_CELL_H, CANVAS_W, DEFAULT_CELL_H, DEFAULT_CELL_C);
        statusBar = new StatusBar(0, CANVAS_H-2*DEFAULT_CELL_H-4, CANVAS_W, DEFAULT_CELL_H+4, Color.DARKGRAY);
        selectedCell = new SelectedCell(2*DEFAULT_CELL_W, 2*DEFAULT_CELL_H, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.LIGHTGREEN);

        camera.picture.draw(gc);
        coordsCell.draw(gc);
        firstCol.draw(gc, camera.getAbsY());
        firstRow.draw(gc, camera.getAbsX());
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);

        sheet = new Sheet();
    }
}

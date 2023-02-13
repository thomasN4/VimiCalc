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

    public static void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[3]) || statusBar.getMode().equals(MODE[4])) {
            switch (event.getCode()) {
                case H -> {
                    if (selectedCell.getX() != DEFAULT_CELL_W)
                        selectedCell.updateX(gc, -DEFAULT_CELL_W);
                    else if (camera.getTable_x() > 0) {
                        ;
                    }
                }
                case J, ENTER -> {
                    if (selectedCell.getY() > camera.picture.getH()+firstRow.getH()) {
                        camera.updateTable_x(statusBar.getY() - selectedCell.getY() - DEFAULT_CELL_H);
                        firstCol.draw(gc, camera.getTable_y());
                    } else selectedCell.updateY(gc, DEFAULT_CELL_H);
                }
                case K -> {
                    if (selectedCell.getY() != DEFAULT_CELL_H)
                        selectedCell.updateY(gc, -DEFAULT_CELL_H);
                }
                case L -> {
                    if (selectedCell.getX() > CANVAS_W-DEFAULT_CELL_W) {
                        camera.updateTable_x(CANVAS_W - selectedCell.getX() - DEFAULT_CELL_W);
                        firstRow.draw(gc, camera.getTable_x());
                    } else selectedCell.updateX(gc, DEFAULT_CELL_W);
                }
                case I -> {
                    statusBar.setMode(MODE[2]);
                    statusBar.draw(gc);
                }
                case ESCAPE -> {
                    statusBar.setMode(MODE[3]);
                    statusBar.draw(gc);
                }
            }
        } else if (statusBar.getMode().equals(MODE[2])) {
            for (int i = 0; i < sheet.textCells.size(); i++) {
                if ((((sheet.textCells.get(i)).xCoord())) == selectedCell.getxCoord()
                    && (((sheet.textCells.get(i)).yCoord())) == selectedCell.getyCoord())
                    selectedCell.setInsertedTxt(sheet.textCells.get(i).text());
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                statusBar.setMode(MODE[3]);
                selectedCell.draw(gc);
            } else if (event.getCode() == KeyCode.ENTER) {
                statusBar.setMode(MODE[3]);
                sheet.textCells.add(new TextCell(selectedCell.getxCoord(),
                        selectedCell.getyCoord(),
                        selectedCell.getInsertedTxt()));
                System.out.println(sheet.textCells);
                selectedCell.updateY(gc, DEFAULT_CELL_H);
            } else selectedCell.draw(gc, event.getText());
        }

        coordsCell.setCoords(selectedCell.getxCoord(), selectedCell.getyCoord());
        coordsCell.draw(gc);
        camera.picture.take(gc, sheet.textCells, camera.getTable_x(), camera.getTable_y());
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
        selectedCell = new SelectedCell(2*DEFAULT_CELL_W, 2*DEFAULT_CELL_H, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.LIGHTGRAY);

        camera.picture.draw(gc);
        coordsCell.draw(gc);
        firstCol.draw(gc, camera.getTable_y());
        firstRow.draw(gc, camera.getTable_x());
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);

        sheet = new Sheet();
    }
}

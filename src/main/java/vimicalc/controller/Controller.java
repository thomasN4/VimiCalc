package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static final int DEFAULT_CELL_HEIGHT = 23;
    public static final int DEFAULT_CELL_WIDTH = DEFAULT_CELL_HEIGHT*4;
    public static final Color DEFAULT_CELL_COLOR = Color.WHITE;
    public static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};

    @FXML private Canvas canvas;

    public static Camera camera;
    public static CoordCell coordCell;
    public static FirstCol firstCol;
    public static FirstRow firstRow;
    public static GraphicsContext gc;
    public static InfoBar infoBar;
    public static SelectedCell selectedCell;
    public static StatusBar statusBar;

    public static void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[3]) || statusBar.getMode().equals(MODE[4])) {
            switch (event.getCode()) {
                case H -> {if (selectedCell.getX() != DEFAULT_CELL_WIDTH)
                               selectedCell.updateX(gc, -DEFAULT_CELL_WIDTH);}
                case J -> selectedCell.updateY(gc, DEFAULT_CELL_HEIGHT);
                case K -> {if (selectedCell.getY() != DEFAULT_CELL_HEIGHT)
                               selectedCell.updateY(gc, -DEFAULT_CELL_HEIGHT);}
                case L -> selectedCell.updateX(gc, DEFAULT_CELL_WIDTH);
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
            if (event.getCode() == KeyCode.ESCAPE) {
                statusBar.setMode(MODE[3]);
                statusBar.draw(gc);
            } else selectedCell.draw(gc, event.getText());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        canvas.setOnKeyPressed(keyEvent -> System.out.println(keyEvent.getCode()));

        gc = canvas.getGraphicsContext2D();

        camera = new Camera(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, canvas.getWidth() - DEFAULT_CELL_WIDTH, canvas.getHeight() - 3 * DEFAULT_CELL_HEIGHT - 4, DEFAULT_CELL_COLOR, 0, 0);
        coordCell = new CoordCell(0, 0, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, DEFAULT_CELL_COLOR, "B2");
        firstCol = new FirstCol(0, DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, canvas.getHeight()-2*DEFAULT_CELL_HEIGHT-4, Color.LIGHTGRAY);
        firstRow = new FirstRow(DEFAULT_CELL_WIDTH, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);
        infoBar = new InfoBar(0, (int) canvas.getHeight() - DEFAULT_CELL_HEIGHT, canvas.getWidth(), DEFAULT_CELL_HEIGHT, DEFAULT_CELL_COLOR);
        statusBar = new StatusBar(0, (int) (canvas.getHeight() - 2 * DEFAULT_CELL_HEIGHT - 4), canvas.getWidth(), DEFAULT_CELL_HEIGHT + 4, Color.DARKGRAY);
        selectedCell = new SelectedCell(2 * DEFAULT_CELL_WIDTH, 2 * DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);

        camera.getPicture().draw(gc);
        coordCell.draw(gc);
        firstCol.draw(gc, camera.getTable_x(), camera.getTable_y());
        firstRow.draw(gc, camera.getTable_x(), camera.getTable_y());
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);
    }
}

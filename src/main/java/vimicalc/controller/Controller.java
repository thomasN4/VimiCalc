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

    private Camera camera;
    private CoordCell coordCell;
    private FirstCol firstCol;
    private FirstRow firstRow;
    private GraphicsContext gc;
    private InfoBar infoBar;
    private SelectedCell selectedCell;
    private StatusBar statusBar;

    @FXML
    void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[3])) {
            switch (event.getCode()) {
                case H:
                    System.out.println("Initial fill: " + gc.getFill());
                    selectedCell.erase(gc);
                    System.out.println("Eraser fill: " + gc.getFill());
                    System.out.println("Initial position: " + selectedCell.getX());
                    selectedCell.setX(selectedCell.getX() - DEFAULT_CELL_WIDTH);
                    System.out.println("Final position: " + selectedCell.getX());
                    selectedCell.draw(gc);
                    System.out.println("Final fill: " + gc.getFill());
                    break;
                case J:
                    selectedCell.updateY(gc, DEFAULT_CELL_HEIGHT);
                    break;
                case K:
                    selectedCell.updateY(gc, -DEFAULT_CELL_HEIGHT);
                    break;
                case L:
                    selectedCell.updateX(gc, DEFAULT_CELL_WIDTH);
                    break;
                case I:
                    statusBar.setMode(MODE[2]);
                    statusBar.draw(gc);
                    break;
            }
        }
        else if (statusBar.getMode().equals(MODE[2])) {
            if (event.getCode() == KeyCode.ALPHANUMERIC)
                selectedCell.draw(gc, event.getCharacter());
            else if (event.getCode() == KeyCode.ESCAPE) {
                statusBar.setMode(MODE[3]);
                statusBar.draw(gc);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

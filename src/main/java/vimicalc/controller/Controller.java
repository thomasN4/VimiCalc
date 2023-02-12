package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.paint.*;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    final int DEFAULT_CELL_HEIGHT = 23;
    final int DEFAULT_CELL_WIDTH = DEFAULT_CELL_HEIGHT*4;
    final Color DEFAULT_CELL_COLOR = Color.WHITE;

    @FXML private MenuBar menuBar;
    @FXML private Canvas canvas;

    private GraphicsContext gc;
    private Camera camera;
    private FirstCol firstCol;
    private FirstRow firstRow;
    private InfoBar infoBar;
    private SelectedCell selectedCell;
    private StatusBar statusBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        camera = new Camera(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, canvas.getWidth()-DEFAULT_CELL_WIDTH, canvas.getHeight()-3*DEFAULT_CELL_HEIGHT-4, DEFAULT_CELL_COLOR, 0, 0);
        firstCol = new FirstCol(0, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);
        firstRow = new FirstRow(0, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);
        infoBar = new InfoBar(0, (int) canvas.getHeight()-DEFAULT_CELL_HEIGHT, canvas.getWidth(), DEFAULT_CELL_HEIGHT, DEFAULT_CELL_COLOR);
        statusBar = new StatusBar(0, (int) (canvas.getHeight()-2*DEFAULT_CELL_HEIGHT-4), canvas.getWidth(), DEFAULT_CELL_HEIGHT+4, Color.DARKGRAY);
        selectedCell = new SelectedCell(2*DEFAULT_CELL_WIDTH, 2*DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);

        camera.getPicture().draw(gc);
        firstCol.draw(gc);
        firstRow.draw(gc);
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);
    }
}

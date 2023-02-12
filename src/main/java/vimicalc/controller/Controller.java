package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    final int DEFAULT_CELL_HEIGHT = 23;
    final int DEFAULT_CELL_WIDTH = DEFAULT_CELL_HEIGHT*4;
    final Color DEFAULT_CELL_COLOR = Color.WHITE;

    @FXML private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Camera camera = new Camera(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, canvas.getWidth() - DEFAULT_CELL_WIDTH, canvas.getHeight() - 3 * DEFAULT_CELL_HEIGHT - 4, DEFAULT_CELL_COLOR, 0, 0);
        FirstCol firstCol = new FirstCol(0, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);
        FirstRow firstRow = new FirstRow(0, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);
        InfoBar infoBar = new InfoBar(0, (int) canvas.getHeight() - DEFAULT_CELL_HEIGHT, canvas.getWidth(), DEFAULT_CELL_HEIGHT, DEFAULT_CELL_COLOR);
        StatusBar statusBar = new StatusBar(0, (int) (canvas.getHeight() - 2 * DEFAULT_CELL_HEIGHT - 4), canvas.getWidth(), DEFAULT_CELL_HEIGHT + 4, Color.DARKGRAY);
        SelectedCell selectedCell = new SelectedCell(2 * DEFAULT_CELL_WIDTH, 2 * DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, Color.LIGHTGRAY);

        camera.getPicture().draw(gc);
        firstCol.draw(gc);
        firstRow.draw(gc);
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);
    }
}

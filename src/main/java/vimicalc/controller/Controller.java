package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.paint.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private final int DEFAULT_CELL_HEIGHT = 23;
    private final int DEFAULT_CELL_WIDTH = DEFAULT_CELL_HEIGHT*4;
    private final Color DEFAULT_CELL_COLOR = Color.WHITE;
    @FXML private MenuBar menuBar;
    @FXML private Canvas canvas;

    private vimicalc.view.Camera camera;

    private void iBar_init() {
        GraphicsContext iBar = canvas.getGraphicsContext2D();
        iBar.setFill(Color.WHITE);
        int iBar_h = DEFAULT_CELL_HEIGHT;
        iBar.fillRect(0, canvas.getHeight()-iBar_h, canvas.getWidth(), iBar_h);
    }

    private void sBar_init() {
        GraphicsContext sBar = canvas.getGraphicsContext2D();
        sBar.setFill(Color.DARKGRAY);
        int sBar_h = DEFAULT_CELL_HEIGHT+4,
            sBar_y = (int) (canvas.getHeight()-sBar_h-DEFAULT_CELL_HEIGHT);
        sBar.fillRect(0, sBar_y, canvas.getWidth(), sBar_h);
    }

    private void lRow_init() {
        GraphicsContext lRow = canvas.getGraphicsContext2D();
        lRow.setFill(Color.LIGHTGRAY);
        lRow.fillRect(0, 0, canvas.getWidth(), DEFAULT_CELL_HEIGHT);
    }

    private void nCol_init() {
        GraphicsContext nCol = canvas.getGraphicsContext2D();
        nCol.setFill(Color.LIGHTGRAY);
        nCol.fillRect(0, DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, canvas.getHeight()-3*DEFAULT_CELL_HEIGHT-4);
    }

    private void picture_init() {
        GraphicsContext table = canvas.getGraphicsContext2D();
        table.setFill(DEFAULT_CELL_COLOR);
        table.fillRect(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, canvas.getWidth()-DEFAULT_CELL_WIDTH, canvas.getHeight()-3*DEFAULT_CELL_HEIGHT-4);
    }

    private void camera_init() {
        camera = new vimicalc.view.Camera(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, canvas.getWidth()-DEFAULT_CELL_WIDTH, canvas.getHeight()-3*DEFAULT_CELL_HEIGHT-4, 0, 0);
    }

    private void selCell_init() {
        GraphicsContext selCell = canvas.getGraphicsContext2D();
        selCell.setFill(Color.LIGHTGRAY);
        selCell.fillRect(2*DEFAULT_CELL_WIDTH, 2*DEFAULT_CELL_HEIGHT, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iBar_init();
        sBar_init();
        lRow_init();
        nCol_init();
        picture_init();
        camera_init();
        selCell_init();
    }

}

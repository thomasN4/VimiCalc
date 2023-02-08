package vimicalc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.paint.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private MenuBar menuBar;

    @FXML
    private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        GraphicsContext iBar = canvas.getGraphicsContext2D();
        iBar.setFill(Color.WHITE);
        int iBar_h = (int) Main.DEFAULT_CELL_HEIGHT;
        iBar.fillRect(0, canvas.getHeight()-iBar_h, canvas.getWidth(), iBar_h);

        GraphicsContext sBar = canvas.getGraphicsContext2D();
        sBar.setFill(Color.DARKGRAY);
        int sBar_h = (int) Main.DEFAULT_CELL_HEIGHT+4;
        sBar.fillRect(0, canvas.getHeight()-sBar_h-iBar_h, canvas.getWidth(), sBar_h);

        GraphicsContext lRow = canvas.getGraphicsContext2D();
        lRow.setFill(Color.LIGHTGRAY);
        int lRow_h = (int) Main.DEFAULT_CELL_HEIGHT;
        iBar.fillRect(0, 0, canvas.getWidth(), lRow_h);

        GraphicsContext nCol = canvas.getGraphicsContext2D();
        nCol.setFill(Color.LIGHTGRAY);
        int nCol_w = (int) Main.DEFAULT_CELL_WIDTH;
        iBar.fillRect(0, lRow_h, nCol_w, canvas.getHeight()-lRow_h-iBar_h-sBar_h);

        setLsNs();

    }

    private void setLsNs() {
        ;
    }

}

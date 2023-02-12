package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.scene.text.TextAlignment;
import vimicalc.model.Sheet;
import vimicalc.model.TextCell;
import vimicalc.view.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static final int DEFAULT_CELL_H = 23;
    public static final int DEFAULT_CELL_W = DEFAULT_CELL_H *4;
    public static final Color DEFAULT_CELL_C = Color.WHITE;
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

    public static Sheet sheet;

    public static void drawTextCells() {
        for (int i = 0; i < sheet.textCells.size(); i++) {
            vimicalc.model.TextCell tC = sheet.textCells.get(i);
            if (tC.xCoord() >= camera.getTable_x()/DEFAULT_CELL_W &&
                tC.xCoord() <= (camera.getTable_x()+camera.getPicture().getW())/DEFAULT_CELL_W &&
                tC.yCoord() >= camera.getTable_y()/DEFAULT_CELL_H &&
                tC.yCoord() <= (camera.getTable_y()+camera.getPicture().getH()/DEFAULT_CELL_H)) {
                gc.setFill(DEFAULT_CELL_C);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(tC.text(),
                            tC.xCoord() * DEFAULT_CELL_W - camera.getTable_x(),
                            tC.yCoord() * DEFAULT_CELL_H - camera.getTable_y(),
                            DEFAULT_CELL_W);
            }
        }
    }

    public static void onKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: "+event.getCode());
        if (statusBar.getMode().equals(MODE[3]) || statusBar.getMode().equals(MODE[4])) {
            switch (event.getCode()) {
                case H -> {if (selectedCell.getX() != DEFAULT_CELL_W)
                               selectedCell.updateX(gc, -DEFAULT_CELL_W);}
                case J -> selectedCell.updateY(gc, DEFAULT_CELL_H);
                case K -> {if (selectedCell.getY() != DEFAULT_CELL_H)
                               selectedCell.updateY(gc, -DEFAULT_CELL_H);}
                case L -> selectedCell.updateX(gc, DEFAULT_CELL_W);
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
                    selectedCell.setInsertedTxt((sheet.textCells.get(i)).text());
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                statusBar.setMode(MODE[3]);
                statusBar.draw(gc);
            } else if (event.getCode() == KeyCode.ENTER) {
                statusBar.setMode(MODE[3]);
                statusBar.draw(gc);
                sheet.textCells.add(new TextCell(selectedCell.getxCoord(),
                                                         selectedCell.getyCoord(),
                                                         selectedCell.getInsertedTxt()));
            } else selectedCell.draw(gc, event.getText());
        }

        drawTextCells();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        camera = new Camera(DEFAULT_CELL_W, DEFAULT_CELL_H, canvas.getWidth() - DEFAULT_CELL_W, canvas.getHeight() - 3 * DEFAULT_CELL_H - 4, DEFAULT_CELL_C, 0, 0);
        coordCell = new CoordCell(0, 0, DEFAULT_CELL_W, DEFAULT_CELL_H, DEFAULT_CELL_C, "B2");
        firstCol = new FirstCol(0, DEFAULT_CELL_H, DEFAULT_CELL_W, canvas.getHeight()-2* DEFAULT_CELL_H -4, Color.LIGHTGRAY);
        firstRow = new FirstRow(DEFAULT_CELL_W, 0, canvas.getWidth(), DEFAULT_CELL_H, Color.LIGHTGRAY);
        infoBar = new InfoBar(0, (int) canvas.getHeight() - DEFAULT_CELL_H, canvas.getWidth(), DEFAULT_CELL_H, DEFAULT_CELL_C);
        statusBar = new StatusBar(0, (int) (canvas.getHeight() - 2 * DEFAULT_CELL_H - 4), canvas.getWidth(), DEFAULT_CELL_H + 4, Color.DARKGRAY);
        selectedCell = new SelectedCell(2 * DEFAULT_CELL_W, 2 * DEFAULT_CELL_H, DEFAULT_CELL_W, DEFAULT_CELL_H, Color.LIGHTGRAY);

        camera.getPicture().draw(gc);
        coordCell.draw(gc);
        firstCol.draw(gc, camera.getTable_x(), camera.getTable_y());
        firstRow.draw(gc, camera.getTable_x(), camera.getTable_y());
        infoBar.draw(gc);
        statusBar.draw(gc);
        selectedCell.draw(gc);

        sheet = new Sheet();
    }
}

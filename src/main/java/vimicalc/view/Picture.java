package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;
import vimicalc.model.TextCell;

import java.util.ArrayList;

public class Picture extends Visible {
    public Picture(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void take(GraphicsContext gc, ArrayList<TextCell> textCells, int table_x, int table_y) {
        int DCW = Controller.DEFAULT_CELL_W;
        int DCH = Controller.DEFAULT_CELL_H;
        for (TextCell tC : textCells) {
            if (tC.xCoord() >= table_x/DCW &&
                tC.xCoord() <= (table_x+w+DCW)/DCW &&
                tC.yCoord() >= table_y/DCH &&
                tC.yCoord() <= (table_y+h+DCH)/DCH) {
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.WHITE);
                gc.fillRect(tC.xCoord() * DCW - table_x, tC.yCoord() * DCH - table_y, DCW, DCH);
                gc.setFill(Color.BLACK);
                gc.fillText(tC.text(),
                        tC.xCoord() * DCW - table_x + 45,
                        tC.yCoord() * DCH - table_y + 16,
                        DCW);
            }
        }
    }
}

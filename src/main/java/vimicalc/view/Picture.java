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

    public void take(GraphicsContext gc, ArrayList<TextCell> textCells, int absX, int absY) {
        int DCW = Controller.DEFAULT_CELL_W;
        int DCH = Controller.DEFAULT_CELL_H;
        super.erase(gc);
        for (TextCell tC : textCells) {
            if (tC.xCoord() >= absX/DCW &&
                tC.xCoord() <= (absX+w+DCW)/DCW &&
                tC.yCoord() >= absY/DCH &&
                tC.yCoord() <= (absY+h+DCH)/DCH) {
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.BLACK);
                gc.fillText(tC.text(),
                        tC.xCoord() * DCW - absX + 45,
                        tC.yCoord() * DCH - absY + 16,
                        DCW);
            }
        }
    }
}

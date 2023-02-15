package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;
import vimicalc.model.Cell;

import java.util.ArrayList;

import static vimicalc.controller.Controller.interpreter;
import static vimicalc.controller.Controller.sheet;

public class Picture extends Visible {
    public Picture(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void take(GraphicsContext gc, ArrayList<Cell> cells, int absX, int absY) {
        ArrayList<Cell> modified = new ArrayList<>();
        int DCW = Controller.DEFAULT_CELL_W;
        int DCH = Controller.DEFAULT_CELL_H;
        super.draw(gc);

        // À revoir:
        for (Cell c : cells) {
            if (c.xCoord() >= absX / DCW + 1 &&
                    c.xCoord() <= (absX + w + DCW) / DCW &&
                    c.yCoord() >= absY / DCH + 1 &&
                    c.yCoord() <= (absY + h + DCH) / DCH) {
                if (c.formula() != null) {
                    interpreter.setLexedFormula(c.formula());
                    interpreter.interpret();
                    c.setTxt(String.valueOf(interpreter.getNumericResult()));
                    c.setVal(interpreter.getNumericResult());
                }
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.BLACK);
                gc.fillText(c.txt(),
                        c.xCoord() * DCW - absX + 45,
                        c.yCoord() * DCH - absY + 16,
                        DCW);
            }
        }
        sheet.updateCellVals(modified);
    }
}

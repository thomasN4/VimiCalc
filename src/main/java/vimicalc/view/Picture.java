package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;
import vimicalc.model.Cell;
import vimicalc.model.Sheet;

import java.util.ArrayList;

public class Picture extends Visible {
    private ArrayList<Cell> visibleCells;
    public Picture(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public ArrayList<Cell> getVisibleCells() {
        return visibleCells;
    }

    public void take(GraphicsContext gc, Sheet sheet, int absX, int absY) {
        ArrayList<Cell> modified = new ArrayList<>();
        visibleCells = new ArrayList<>();
        int DCW = Controller.DEFAULT_CELL_W;
        int DCH = Controller.DEFAULT_CELL_H;
        super.draw(gc);

        // À revoir:
        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= absX / DCW + 1 &&
                    c.xCoord() <= (absX + w + DCW) / DCW &&
                    c.yCoord() >= absY / DCH + 1 &&
                    c.yCoord() <= (absY + h + DCH) / DCH) {
                if (!c.formula().getTxt().equals("0")) {
                    System.out.println("Reinterpreting...");
                    c.setTxt(c.formula().interpret(sheet));
                    modified.add(c);
                }
                visibleCells.add(c);
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

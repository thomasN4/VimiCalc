package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;
import vimicalc.model.Cell;
import vimicalc.model.Sheet;

import java.util.ArrayList;

public class Picture extends Visible {
    private final int DCW = Controller.DEFAULT_CELL_W;
    private final int DCH = Controller.DEFAULT_CELL_H;
    private ArrayList<Cell> visibleCells;
    private boolean isntReady;

    public Picture(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public ArrayList<Cell> data() {
        return visibleCells;
    }

    public void resend(GraphicsContext gc, int absX, int absY) {
        if (!isntReady) {
            super.draw(gc);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.BLACK);
            visibleCells.forEach(c ->
                gc.fillText(
                    c.txt(),
                    c.xCoord() * DCW - absX + 45,
                    c.yCoord() * DCH - absY + 16,
                    DCW)
            );
        }
        isntReady = false;
    }

    public void take(GraphicsContext gc, @NotNull Sheet sheet, int absX, int absY) {
        visibleCells = new ArrayList<>();
        ArrayList<Cell> modified = new ArrayList<>();
        super.draw(gc);

        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= absX / DCW + 1 &&
                    c.xCoord() <= (absX + w + DCW) / DCW &&
                    c.yCoord() >= absY / DCH + 1 &&
                    c.yCoord() <= (absY + h + DCH) / DCH) {
                if (c.formula() != null) modified.add(new Cell(
                        c.xCoord(),
                        c.yCoord(),
                        c.formula().interpret(sheet),
                        c.formula()
                ));
                else visibleCells.add(c);
            }
        }
        sheet.updateCells(modified);

        visibleCells.addAll(modified);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.BLACK);
        visibleCells.forEach(c ->
            gc.fillText(
                c.txt(),
                c.xCoord() * DCW - absX + 45,
                c.yCoord() * DCH - absY + 16,
                DCW)
        );
        isntReady = true;
    }

    public void setIsntReady(boolean isntReady) {
        this.isntReady = isntReady;
    }
}

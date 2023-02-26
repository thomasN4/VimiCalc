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
            System.out.println("Redrawing picture...");
            super.draw(gc);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.BLACK);
            visibleCells.forEach(c ->
                gc.fillText(c.txt(),
                            c.xCoord() * DCW - absX + 45,
                            c.yCoord() * DCH - absY + 16,
                            DCW));
        }
        isntReady = false;
    }

    public void take(GraphicsContext gc, @NotNull Sheet sheet, int absX, int absY) {
        ArrayList<Cell> modified = new ArrayList<>();
        visibleCells = new ArrayList<>();
        super.draw(gc);
        System.out.println("Redrawing picture...");

        // À revoir:
        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= absX / DCW + 1 &&
                    c.xCoord() <= (absX + w + DCW) / DCW &&
                    c.yCoord() >= absY / DCH + 1 &&
                    c.yCoord() <= (absY + h + DCH) / DCH) {
                if (c.formula() != null) {
                    System.out.println("Reinterpreting...");
                    String result = c.formula().interpret(sheet);
                    c = new Cell(c.xCoord(), c.yCoord(), result, c.formula());
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
        sheet.updateCellValues(modified);
        isntReady = true;
    }

    public void setIsntReady(boolean isntReady) {
        this.isntReady = isntReady;
    }

    public void edit(Cell cell) {
        visibleCells.removeIf(c -> c.xCoord() == cell.xCoord() && c.yCoord() == cell.yCoord());
        visibleCells.add(cell);
    }
}

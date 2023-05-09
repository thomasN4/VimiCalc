package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;

public class Picture extends Visible {
    private final int DCW;
    private final int DCH;
    private ArrayList<Cell> visibleCells;
    private boolean isntReady;

    private final Positions positions;

    public Picture(int x, int y, int w, int h, Color c, int DCW, int DCH, int camAbsX, int camAbsY,
                   HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets) {
        super(x, y, w, h, c);
        this.DCW = DCW;
        this.DCH = DCH;
        positions = new Positions(camAbsX, camAbsY, w, h, DCW, DCH, xOffsets, yOffsets);
    }

    public ArrayList<Cell> data() {
        return visibleCells;
    }

    public Positions metadata() {
        return positions;
    }

    public void resend(GraphicsContext gc, int absX, int absY) {
        if (!isntReady) {
            super.draw(gc);
            drawVCells(gc, absX, absY);
        }
        isntReady = false;
    }

    public void take(GraphicsContext gc, @NotNull Sheet sheet, ArrayList<int[]> selectedCoords, int absX, int absY) {
        visibleCells = new ArrayList<>();
        super.draw(gc);

        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= positions.getFirstXC() &&
                c.xCoord() <= positions.getLastXC() &&
                c.yCoord() >= positions.getFirstYC() &&
                c.yCoord() <= positions.getLastYC()) {
                    visibleCells.add(c);
            }
        }

        gc.setFill(Color.DARKGRAY);
        selectedCoords.forEach(c ->
            gc.fillRect(
                positions.getCellAbsXs()[c[0]] - absX + DCW,
                positions.getCellAbsYs()[c[1]] - absY + DCH,
                positions.getCellAbsXs()[c[0]+1] - positions.getCellAbsXs()[c[0]],
                positions.getCellAbsYs()[c[1]+1] - positions.getCellAbsYs()[c[1]]
            )
        );

        drawVCells(gc, absX, absY);
        isntReady = true;
    }

    public void setIsntReady(boolean isntReady) {
        this.isntReady = isntReady;
    }

    private void drawVCells(@NotNull GraphicsContext gc, int absX, int absY) {
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        int cellHeight, cellWidth;
        for (Cell c : visibleCells) {
            if (c.isMergeStart()) {
                cellHeight = positions.getCellAbsYs()[c.getMergeDelimiter().yCoord()+1] -
                             positions.getCellAbsYs()[c.yCoord()];
                cellWidth = positions.getCellAbsXs()[c.getMergeDelimiter().xCoord()+1] -
                            positions.getCellAbsXs()[c.xCoord()];
            }
            else {
                cellHeight = positions.getCellAbsYs()[c.yCoord()+1] - positions.getCellAbsYs()[c.yCoord()];
                cellWidth = positions.getCellAbsXs()[c.xCoord()+1] - positions.getCellAbsXs()[c.xCoord()];
            }
            gc.fillText(
                c.txt(),
                positions.getCellAbsXs()[c.xCoord()] - absX + DCW + (float) cellWidth/2,
                positions.getCellAbsYs()[c.yCoord()] - absY + DCH + (float) cellHeight/2,
                cellWidth
            );
        }
    }
}
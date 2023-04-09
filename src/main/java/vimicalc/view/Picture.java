package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Metadata;
import vimicalc.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;

public class Picture extends Visible {
    private final int DCW;
    private final int DCH;
    private ArrayList<Cell> visibleCells;
    private boolean isntReady;

    private final Metadata metadata;

    public Picture(int x, int y, int w, int h, Color c, int DCW, int DCH,
                   int camAbsX, int camAbsY, HashMap<Integer, Integer> xOffsets,
                   HashMap<Integer, Integer> yOffsets) {
        super(x, y, w, h, c);
        this.DCW = DCW;
        this.DCH = DCH;
        metadata = new Metadata(camAbsX, camAbsY, xOffsets, yOffsets, w, h, DCW, DCH);
    }

    public ArrayList<Cell> data() {
        return visibleCells;
    }

    public Metadata metadata() {
        return metadata;
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
        metadata.generate(absX, absY);

        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= metadata.getFirstXC() &&
                c.xCoord() <= metadata.getLastXC() &&
                c.yCoord() >= metadata.getFirstYC() &&
                c.yCoord() <= metadata.getLastYC()) {
                if (c.getMergeDelimiter() != null && !c.isMergeStart()) {
                    visibleCells.removeIf(d ->
                        c.getMergeDelimiter().xCoord() == d.xCoord() &&
                        c.getMergeDelimiter().yCoord() == d.yCoord()
                    );
                    visibleCells.add(c.getMergeDelimiter());
                }
                else
                    visibleCells.add(c);
            }
        }

        gc.setFill(Color.DARKGRAY);
        selectedCoords.forEach(c ->
            gc.fillRect(
                metadata.getCellAbsXs()[c[0]] - absX + DCW,
                metadata.getCellAbsYs()[c[1]] - absY + DCH,
                metadata.getCellAbsXs()[c[0]+1] - metadata.getCellAbsXs()[c[0]],
                metadata.getCellAbsYs()[c[1]+1] - metadata.getCellAbsYs()[c[1]]
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
                cellHeight = metadata.getCellAbsYs()[c.getMergeDelimiter().yCoord()+1] -
                             metadata.getCellAbsYs()[c.yCoord()];
                cellWidth = metadata.getCellAbsXs()[c.getMergeDelimiter().xCoord()+1] -
                            metadata.getCellAbsXs()[c.xCoord()];
            }
            else {
                cellHeight = metadata.getCellAbsYs()[c.yCoord()+1] - metadata.getCellAbsYs()[c.yCoord()];
                cellWidth = metadata.getCellAbsXs()[c.xCoord()+1] - metadata.getCellAbsXs()[c.xCoord()];
            }
            gc.fillText(
                c.txt(),
                metadata.getCellAbsXs()[c.xCoord()] - absX + DCW + (float) cellWidth/2,
                metadata.getCellAbsYs()[c.yCoord()] - absY + DCH + (float) cellHeight/2,
                cellWidth
            );
        }
    }
}
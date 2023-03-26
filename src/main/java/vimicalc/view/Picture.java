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
    private final int DCW;  // 'C' pour "Cell"
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

    public void take(GraphicsContext gc, @NotNull Sheet sheet, ArrayList<int[]> selectedCoords, int absX, int absY) {
        visibleCells = new ArrayList<>();
        super.draw(gc);
        metadata.generate(absX, absY);

        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= metadata.getFirstXC() &&
                c.xCoord() <= metadata.getLastXC() &&
                c.yCoord() >= metadata.getFirstYC() &&
                c.yCoord() <= metadata.getLastYC()) {
                if (c.formula() != null && !c.formula().getTxt().contains("matMult"))
                    visibleCells.add(new Cell(
                        c.xCoord(),
                        c.yCoord(),
                        c.formula().interpret(sheet),
                        c.formula()
                    ));
                else visibleCells.add(c);
            }
        }

        gc.setFill(Color.DARKGRAY);
        selectedCoords.forEach(c -> {
            System.out.println("Drawing the selected cells...");
            gc.fillRect(
                metadata.getCellAbsXs()[c[0]] - metadata.getCamAbsX() + DCW,
                metadata.getCellAbsYs()[c[1]] - metadata.getCamAbsY() + DCH,
                metadata.getCellAbsXs()[c[0]] - metadata.getCellAbsXs()[c[0]-1],
                metadata.getCellAbsYs()[c[1]] - metadata.getCellAbsYs()[c[1]-1]
            );
        });

        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        int cellHeight, cellWidth;
        for (Cell c : visibleCells) {
            cellHeight = metadata.getCellAbsYs()[c.yCoord()] - metadata.getCellAbsYs()[c.yCoord()-1];
            cellWidth = metadata.getCellAbsXs()[c.xCoord()] - metadata.getCellAbsXs()[c.xCoord()-1];
            gc.fillText(
                c.txt(),
                metadata.getCellAbsXs()[c.xCoord()] - metadata.getCamAbsX() + DCW + (float) cellWidth/2,
                metadata.getCellAbsYs()[c.yCoord()] - metadata.getCamAbsY() + DCH + (float) cellHeight/2,
                cellWidth
            );
        }

        isntReady = true;
    }

    public void setIsntReady(boolean isntReady) {
        this.isntReady = isntReady;
    }
}
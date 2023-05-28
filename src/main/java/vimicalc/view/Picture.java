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
import java.util.List;
import java.util.Map;

public class Picture extends simpleRect {
    private final int DCW;
    private final int DCH;
    private ArrayList<Cell> visibleCells;
    private boolean isntReady;
    private final Positions metadata;
    private final HashMap<List<Integer>, Formatting> cellsFormatting;

    public Picture(int x, int y, int w, int h, Color c, int DCW, int DCH, int camAbsX, int camAbsY,
                   HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets,
                   HashMap<List<Integer>, Formatting> cellsFormatting) {
        super(x, y, w, h, c);
        this.DCW = DCW;
        this.DCH = DCH;
        metadata = new Positions(camAbsX, camAbsY, w, h, DCW, DCH, xOffsets, yOffsets);
        this.cellsFormatting = cellsFormatting;
    }

    public ArrayList<Cell> data() {
        return visibleCells;
    }

    public Positions metadata() {
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

        for (Cell c : sheet.getCells()) {
            if (c.xCoord() >= metadata.getFirstXC() &&
                c.xCoord() <= metadata.getLastXC() &&
                c.yCoord() >= metadata.getFirstYC() &&
                c.yCoord() <= metadata.getLastYC()) {
                    visibleCells.add(c);
            }
        }

        gc.setFill(new Color(0,0.67,1,1));
        selectedCoords.forEach(c ->
            gc.fillRect(
                metadata.getCellAbsXs()[c[0]] - absX + (float) DCW/2,
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

            try {
                System.out.println(cellsFormatting.get(List.of(c.xCoord(), c.yCoord())));
                cellsFormatting.get(List.of(c.xCoord(), c.yCoord())).renderCell(
                    gc,
                    (int) (metadata.getCellAbsXs()[c.xCoord()] - absX + (float)DCW/2),
                    metadata.getCellAbsYs()[c.yCoord()] - absY + DCH,
                    cellWidth,
                    cellHeight,
                    c.txt()
                );
            } catch (Exception ignored) {
                gc.fillText(
                    c.txt(),
                   metadata.getCellAbsXs()[c.xCoord()] - absX + (float) DCW / 2 + (float) cellWidth / 2,
                   metadata.getCellAbsYs()[c.yCoord()] - absY + DCH + (float) cellHeight / 2,
                    cellWidth
                );
            }
        }

        for (Map.Entry<List<Integer>, Formatting> entry : cellsFormatting.entrySet()) {
            int formattingXC = entry.getKey().get(0), formattingYC = entry.getKey().get(1);
            if (formattingXC >= metadata.getFirstXC() &&
                formattingXC <= metadata.getLastXC() &&
                formattingYC >= metadata.getFirstYC() &&
                formattingYC <= metadata.getLastYC()) {
                boolean ignore = false;
                for (Cell c : visibleCells)
                    if (c.xCoord() == formattingXC && c.yCoord() == formattingYC)
                        ignore = true;
                if (!ignore) entry.getValue().renderCell(
                    gc,
                    (int) (metadata.getCellAbsXs()[formattingXC] - absX + (float) DCW/2),
                    metadata.getCellAbsYs()[formattingYC] - absY + DCH,
                    metadata.getCellAbsXs()[formattingXC+1] - metadata.getCellAbsXs()[formattingXC],
                    metadata.getCellAbsYs()[formattingYC+1] - metadata.getCellAbsYs()[formattingYC],
                    ""
                );
            }
        }
    }
}
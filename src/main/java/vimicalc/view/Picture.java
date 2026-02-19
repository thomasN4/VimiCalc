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

/**
 * The main spreadsheet grid renderer, responsible for drawing all visible cells
 * onto the canvas.
 *
 * <p>Works closely with {@link Camera} and {@link Positions} to determine which
 * cells fall within the current viewport. Handles cell formatting, merged cells,
 * and visual selection highlighting.</p>
 */
public class Picture extends simpleRect {
    /** Default cell width in pixels. */
    private final int DCW;
    /** Default cell height in pixels. */
    private final int DCH;
    private ArrayList<Cell> visibleCells;
    private boolean isntReady;
    private final Positions metadata;
    private final HashMap<List<Integer>, Formatting> cellsFormatting;

    /**
     * Creates the picture renderer.
     *
     * @param x               the x pixel position
     * @param y               the y pixel position
     * @param w               the width in pixels
     * @param h               the height in pixels
     * @param c               the background color
     * @param DCW             the default cell width
     * @param DCH             the default cell height
     * @param metadata        the position metadata
     * @param cellsFormatting per-cell formatting overrides
     */
    public Picture(int x, int y, int w, int h, Color c, int DCW, int DCH, Positions metadata,
                   HashMap<List<Integer>, Formatting> cellsFormatting) {
        super(x, y, w, h, c);
        this.DCW = DCW;
        this.DCH = DCH;
        this.metadata = metadata;
        this.cellsFormatting = cellsFormatting;
    }

    /**
     * Returns the list of currently visible cells.
     *
     * @return the visible cells
     */
    public ArrayList<Cell> data() {
        return visibleCells;
    }

    /**
     * Returns the position metadata used for layout.
     *
     * @return the positions metadata
     */
    public Positions metadata() {
        return metadata;
    }

    /**
     * Performs a lightweight re-render using previously captured visible cells,
     * skipping the full cell scan. Called after {@link #take} has already
     * populated the visible cell list.
     *
     * @param gc   the graphics context
     * @param absX the camera's horizontal offset
     * @param absY the camera's vertical offset
     */
    public void resend(GraphicsContext gc, int absX, int absY) {
        if (!isntReady) {
            super.draw(gc);
            drawVCells(gc, absX, absY);
        }
        isntReady = false;
    }

    /**
     * Takes a full snapshot of the sheet, filters cells to those within the
     * current viewport, highlights any visually selected cells, and renders
     * everything onto the canvas.
     *
     * @param gc              the graphics context
     * @param sheet           the spreadsheet data model
     * @param selectedCoords  list of {@code [x, y]} coordinate pairs that are
     *                        currently selected in VISUAL mode
     * @param absX            the camera's horizontal offset
     * @param absY            the camera's vertical offset
     */
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

    /**
     * Sets the rendering readiness flag.
     *
     * @param isntReady {@code true} to indicate the picture needs a full re-render
     */
    public void setIsntReady(boolean isntReady) {
        this.isntReady = isntReady;
    }

    /**
     * Draws all visible cells, applying per-cell formatting where available.
     * Also renders formatting-only cells (cells with custom colors/styles
     * but no content) that fall within the viewport.
     */
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
package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.GUTTER_W;
import static vimicalc.view.Defaults.HEADER_H;

/**
 * The main spreadsheet grid renderer, responsible for drawing all visible cells
 * onto the canvas.
 *
 * <p>Works closely with {@link Camera} and {@link Positions} to determine which
 * cells fall within the current viewport. Handles cell formatting, merged cells,
 * and visual selection highlighting.</p>
 */
public class Picture extends simpleRect {
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
     * @param metadata        the position metadata
     * @param cellsFormatting per-cell formatting overrides
     */
    public Picture(int x, int y, int w, int h, Color c, Positions metadata,
                   HashMap<List<Integer>, Formatting> cellsFormatting) {
        super(x, y, w, h, c);
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
            drawGridlines(gc, absX, absY);
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
        drawGridlines(gc, absX, absY);

        for (Cell c : sheet.getCells()) {
            // A merge-start draws the whole merged block, so it must stay
            // visible as long as any part of its range intersects the
            // viewport — not only while its own (top-left) coordinate does.
            int maxXC = c.xCoord(), maxYC = c.yCoord();
            if (c.isMergeStart() && c.getMergeDelimiter() != null) {
                maxXC = c.getMergeDelimiter().xCoord();
                maxYC = c.getMergeDelimiter().yCoord();
            }
            if (maxXC >= metadata.getFirstXC() &&
                c.xCoord() <= metadata.getLastXC() &&
                maxYC >= metadata.getFirstYC() &&
                c.yCoord() <= metadata.getLastYC()) {
                    visibleCells.add(c);
            }
        }

        gc.setFill(new Color(0,0.67,1,1));
        selectedCoords.forEach(c ->
            gc.fillRect(
                metadata.getCellAbsXs()[c[0]] - absX + GUTTER_W,
                metadata.getCellAbsYs()[c[1]] - absY + HEADER_H,
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
     * Strokes the cell gridlines at every visible column and row boundary,
     * unless they are toggled off (the {@code :gridlines} command). Drawn
     * directly over the background so VISUAL-selection and cell-formatting
     * fills cover the lines, as in other spreadsheets.
     */
    private void drawGridlines(@NotNull GraphicsContext gc, int absX, int absY) {
        if (!metadata.gridlinesOn()) return;

        int[] absXs = metadata.getCellAbsXs();
        int[] absYs = metadata.getCellAbsYs();
        // Screen-space extent of the visible grid, so lines stop at the last
        // boundary instead of running past the laid-out cells.
        double left = absXs[metadata.getFirstXC()] - absX + GUTTER_W;
        double right = absXs[metadata.getLastXC() + 1] - absX + GUTTER_W;
        double top = absYs[metadata.getFirstYC()] - absY + HEADER_H;
        double bottom = absYs[metadata.getLastYC() + 1] - absY + HEADER_H;

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        // The +0.5 centers each 1px stroke on the pixel so lines render crisp.
        for (int i = metadata.getFirstXC(); i <= metadata.getLastXC() + 1; i++) {
            double x = absXs[i] - absX + GUTTER_W + 0.5;
            gc.strokeLine(x, top, x, bottom);
        }
        for (int j = metadata.getFirstYC(); j <= metadata.getLastYC() + 1; j++) {
            double y = absYs[j] - absY + HEADER_H + 0.5;
            gc.strokeLine(left, y, right, y);
        }
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
        // Scale the default font so unformatted cells zoom with the grid;
        // formatted cells scale via the zoom argument to renderCell.
        Font prevFont = gc.getFont();
        gc.setFont(Font.font(prevFont.getFamily(), DEFAULT_FONT_SIZE * metadata.getZoom()));
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
                    metadata.getCellAbsXs()[c.xCoord()] - absX + GUTTER_W,
                    metadata.getCellAbsYs()[c.yCoord()] - absY + HEADER_H,
                    cellWidth,
                    cellHeight,
                    c.txt(),
                    metadata.getZoom()
                );
            } catch (Exception ignored) {
                gc.fillText(
                    c.txt(),
                   metadata.getCellAbsXs()[c.xCoord()] - absX + GUTTER_W + (float) cellWidth / 2,
                   metadata.getCellAbsYs()[c.yCoord()] - absY + HEADER_H + (float) cellHeight / 2,
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
                    metadata.getCellAbsXs()[formattingXC] - absX + GUTTER_W,
                    metadata.getCellAbsYs()[formattingYC] - absY + HEADER_H,
                    metadata.getCellAbsXs()[formattingXC+1] - metadata.getCellAbsXs()[formattingXC],
                    metadata.getCellAbsYs()[formattingYC+1] - metadata.getCellAbsYs()[formattingYC],
                    "",
                    metadata.getZoom()
                );
            }
        }
        gc.setFont(prevFont);
    }
}
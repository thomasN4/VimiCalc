package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;
import vimicalc.model.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.GUTTER_W;
import static vimicalc.view.Defaults.HEADER_H;

/**
 * The cursor / selection highlight that indicates which cell is currently
 * active in the spreadsheet.
 *
 * <p>Tracks the logical cell coordinates ({@code xCoord}, {@code yCoord});
 * the pixel position and size are always derived from the grid layout and
 * the live camera offset (see {@link #syncToGrid()}), never accumulated.
 * Handles merged cells by expanding the highlight to cover the full merged
 * area. Also supports rendering text as it is typed in INSERT mode.</p>
 */
public class CellSelector extends simpleRect {

    private int xCoord, yCoord, mergedW, mergedH;
    private Cell selectedCell;
    private final Camera camera;
    private final Positions picPositions;
    private final Supplier<Mode> modeSupplier;
    private final HashMap<List<Integer>, Formatting> cellsFormatting;

    /**
     * Creates a cell selector; its pixel position is derived from the grid
     * layout, so {@code picPositions} must already be generated.
     *
     * @param c               the highlight color
     * @param camera          the viewport camera the pixel position derives from
     * @param picPositions    the position metadata for cell layout
     * @param cellsFormatting per-cell formatting overrides, used to style the
     *                        selected cell's text like its rendered form
     * @param modeSupplier    supplies the current editing mode
     */
    public CellSelector(Color c, Camera camera, Positions picPositions,
                        HashMap<List<Integer>, Formatting> cellsFormatting, Supplier<Mode> modeSupplier) {
        super(0, 0, 0, 0, c);
        this.camera = camera;
        this.picPositions = picPositions;
        this.cellsFormatting = cellsFormatting;
        this.modeSupplier = modeSupplier;
        // Starts at (2,2) rather than (1,1) because the first row and column
        // (index 1) are reserved for the header gutter (FirstRow / FirstCol).
        xCoord = 2;
        yCoord = 2;
        selectedCell = new Cell(xCoord, yCoord);
        syncToGrid();
    }

    /**
     * Returns the currently selected cell.
     *
     * @return the selected cell
     */
    public Cell getSelectedCell() {
        return selectedCell;
    }

    /**
     * Returns the logical column coordinate of the selector.
     *
     * @return the column coordinate
     */
    public int getXCoord() {
        return xCoord;
    }

    /**
     * Returns the logical row coordinate of the selector.
     *
     * @return the row coordinate
     */
    public int getYCoord() {
        return yCoord;
    }

    /**
     * Sets the currently selected cell.
     *
     * @param selectedCell the cell to select
     */
    public void setSelectedCell(Cell selectedCell) {
        this.selectedCell = selectedCell;
    }

    private void drawTxt(GraphicsContext gc) {
        Formatting f = cellsFormatting.get(List.of(xCoord, yCoord));
        Font prevFont = gc.getFont();
        if (f != null) {
            gc.setFill(f.setFXColor(f.getTxtColor()));
            gc.setTextBaseline(f.setFXVPos(f.getvPos()));
            gc.setTextAlign(f.setFXAlignment(f.getAlignment()));
            gc.setFont(Font.font(
                prevFont.getFamily(),
                f.setFXFontWeight(f.getFontWeight()),
                f.setFXFontPosture(f.getFontPosture()),
                f.getFontSize() * picPositions.getZoom()
            ));
        } else {
            gc.setFill(Color.BLACK);
            gc.setTextBaseline(VPos.CENTER);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font(prevFont.getFamily(), DEFAULT_FONT_SIZE * picPositions.getZoom()));
        }
        gc.fillText(selectedCell.txt()
            , x + (float) w/2
            , y + (float) h/2
            , w);
        gc.setFont(prevFont);
    }
    @Override
    public void draw(@NotNull GraphicsContext gc) {
        if (!selectedCell.isMergeStart() || modeSupplier.get() == Mode.VISUAL) {
            super.draw(gc);
            if (selectedCell.txt() != null) drawTxt(gc);
        }
        else {
            int prevW = w, prevH = h;
            w = mergedW; h = mergedH;
            super.draw(gc);
            if (selectedCell.txt() != null) drawTxt(gc);
            w = prevW; h = prevH;
        }
    }

    /**
     * Appends a character to the selected cell's text and redraws.
     * Used during INSERT mode for live character-by-character input.
     *
     * @param gc           the graphics context
     * @param insertedChar the character typed by the user
     */
    public void draw(GraphicsContext gc, String insertedChar) {
        selectedCell.setTxt(selectedCell.txt() + insertedChar);
        this.draw(gc);
    }

    /**
     * Returns the width of the merged cell area in pixels.
     *
     * @return the merged width
     */
    public int getMergedW() {
        return mergedW;
    }

    /**
     * Returns the height of the merged cell area in pixels.
     *
     * @return the merged height
     */
    public int getMergedH() {
        return mergedH;
    }

    /**
     * Adjusts the logical column coordinate by the given delta.
     *
     * @param xCoord_mov the column delta
     */
    public void updateXCoord(int xCoord_mov) {
        xCoord += xCoord_mov;
    }

    /**
     * Adjusts the logical row coordinate by the given delta.
     *
     * @param yCoord_mov the row delta
     */
    public void updateYCoord(int yCoord_mov) {
        yCoord += yCoord_mov;
    }

    /**
     * Looks up the cell at the current coordinates from the visible cell list
     * and updates the selector's state (selected cell, dimensions, merge info).
     *
     * <p>If no matching cell exists, creates a new empty cell at the position.</p>
     *
     * @param cells the list of currently visible cells from {@link Picture}
     */
    public void readCell(@NotNull ArrayList<Cell> cells) {
        System.out.println("Camera picture data: " + cells);
        for (Cell c : cells) {
            if (c.xCoord() == xCoord && c.yCoord() == yCoord) {
                selectedCell = c.copy();
                if (c.isMergeStart() && modeSupplier.get() != Mode.VISUAL) {
                    Cell mergeEnd = c.getMergeDelimiter();
                    mergedW = picPositions.getCellAbsXs()[mergeEnd.xCoord()+1] -
                        picPositions.getCellAbsXs()[xCoord];
                    mergedH = picPositions.getCellAbsYs()[mergeEnd.yCoord()+1] -
                        picPositions.getCellAbsYs()[yCoord];
                }
                syncToGrid();
                return;
            }
        }
        selectedCell = new Cell(xCoord, yCoord);
        syncToGrid();
    }

    /**
     * Derives the pixel position and size from the grid layout and the live
     * camera offset — the same formula the cell grid and headers use, so the
     * highlight can never drift away from its cell (issue #30).
     */
    private void syncToGrid() {
        int[] xs = picPositions.getCellAbsXs(), ys = picPositions.getCellAbsYs();
        w = xs[xCoord+1] - xs[xCoord];
        h = ys[yCoord+1] - ys[yCoord];
        x = xs[xCoord] - camera.getAbsX() + GUTTER_W;
        y = ys[yCoord] - camera.getAbsY() + HEADER_H;
    }
}

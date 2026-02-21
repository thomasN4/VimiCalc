package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;
import vimicalc.model.Cell;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * The cursor / selection highlight that indicates which cell is currently
 * active in the spreadsheet.
 *
 * <p>Tracks both the logical cell coordinates ({@code xCoord}, {@code yCoord})
 * and the pixel position on the canvas. Handles merged cells by expanding
 * the highlight to cover the full merged area. Also supports rendering
 * text as it is typed in INSERT mode.</p>
 */
public class CellSelector extends simpleRect {

    private int xCoord, yCoord, mergedW, mergedH;
    private Cell selectedCell;
    private final Positions picPositions;
    private final Supplier<Mode> modeSupplier;

    /**
     * Creates a cell selector at the given pixel position.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the highlight color
     * @param picPositions the position metadata for cell layout
     * @param modeSupplier supplies the current editing mode
     */
    public CellSelector(int x, int y, int w, int h, Color c, Positions picPositions, Supplier<Mode> modeSupplier) {
        super(x, y, w, h, c);
        this.picPositions = picPositions;
        this.modeSupplier = modeSupplier;
        // Starts at (2,2) rather than (1,1) because the first row and column
        // (index 1) are reserved for the header gutter (FirstRow / FirstCol).
        xCoord = 2;
        yCoord = 2;
        selectedCell = new Cell(xCoord, yCoord);
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
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(selectedCell.txt()
            , x + (float) w/2
            , y + (float) h/2
            , w);
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
     * Adjusts the pixel x-position by the given delta.
     *
     * @param x_mov the horizontal pixel delta
     */
    public void updateX(int x_mov) {
        x += x_mov;
    }

    /**
     * Adjusts the pixel y-position by the given delta.
     *
     * @param y_mov the vertical pixel delta
     */
    public void updateY(int y_mov) {
        y += y_mov;
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
                setDimensions();
                return;
            }
        }
        selectedCell = new Cell(xCoord, yCoord);
        setDimensions();
    }

    private void setDimensions() {
        w = picPositions.getCellAbsXs()[xCoord+1] - picPositions.getCellAbsXs()[xCoord];
        h = picPositions.getCellAbsYs()[yCoord+1] - picPositions.getCellAbsYs()[yCoord];
    }
}

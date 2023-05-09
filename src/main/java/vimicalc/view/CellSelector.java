package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;
import vimicalc.model.Cell;

import java.util.ArrayList;

import static vimicalc.controller.Controller.currMode;

public class CellSelector extends Visible {

    private int xCoord, yCoord, mergedW, mergedH;
    private Cell selectedCell;
    private final Positions picPositions;

    public CellSelector(int x, int y, int w, int h, Color c, Positions picPositions) {
        super(x, y, w, h, c);
        this.picPositions = picPositions;
        xCoord = 2;
        yCoord = 2;
        selectedCell = new Cell(xCoord, yCoord);
    }

    public Cell getSelectedCell() {
        return selectedCell;
    }

    public int getXCoord() {
        return xCoord;
    }

    public int getYCoord() {
        return yCoord;
    }

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
        if (!selectedCell.isMergeStart() || currMode == Mode.VISUAL) {
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

    public void draw(GraphicsContext gc, String insertedChar) {
        selectedCell.setTxt(selectedCell.txt() + insertedChar);
        this.draw(gc);
    }

    public int getMergedW() {
        return mergedW;
    }

    public int getMergedH() {
        return mergedH;
    }

    public void updateX(int x_mov) {
        x += x_mov;
    }

    public void updateY(int y_mov) {
        y += y_mov;
    }

    public void updateXCoord(int xCoord_mov) {
        xCoord += xCoord_mov;
    }

    public void updateYCoord(int yCoord_mov) {
        yCoord += yCoord_mov;
    }

    public void readCell(@NotNull ArrayList<Cell> cells) {
        System.out.println("Camera picture data: " + cells);
        for (Cell c : cells) {
            if (c.xCoord() == xCoord && c.yCoord() == yCoord) {
                selectedCell = c.copy();
                if (c.isMergeStart() && currMode != Mode.VISUAL) {
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

package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;

import java.util.ArrayList;

public class CellSelector extends Visible {

    private int xCoord, yCoord, mergedW, mergedH;
    private Cell selectedCell;
    private final Metadata picMetadata;

    public CellSelector(int x, int y, int w, int h, Color c, Metadata picMetadata) {
        super(x, y, w, h, c);
        this.picMetadata = picMetadata;
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
        if (!selectedCell.isMergeStart()) {
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
                if (c.isMergeStart()) {
                    Cell mergeEnd = c.getMergeDelimiter();
                    mergedW = picMetadata.getCellAbsXs()[mergeEnd.xCoord()+1] -
                        picMetadata.getCellAbsXs()[xCoord];
                    mergedH = picMetadata.getCellAbsYs()[mergeEnd.yCoord()+1] -
                        picMetadata.getCellAbsYs()[yCoord];
                }
                setDimensions();
                return;
            }
        }
        selectedCell = new Cell(xCoord, yCoord);
        setDimensions();
    }

    private void setDimensions() {
        w = picMetadata.getCellAbsXs()[xCoord+1] - picMetadata.getCellAbsXs()[xCoord];
        h = picMetadata.getCellAbsYs()[yCoord+1] - picMetadata.getCellAbsYs()[yCoord];
    }
}

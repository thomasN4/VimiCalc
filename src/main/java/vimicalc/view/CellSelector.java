package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Formula;
import vimicalc.model.Metadata;

import java.util.ArrayList;

public class CellSelector extends Visible {

    private int xCoord;
    private int yCoord;
    private Cell selectedCell;
    private final Metadata picMetadata;

    public CellSelector(int x, int y, int w, int h, Color c, Metadata picMetadata) {
        super(x, y, w, h, c);
        this.picMetadata = picMetadata;
        xCoord = x/w;
        yCoord = y/h;
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

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(selectedCell.txt()
            , x + (float) w/2
            , y + (float) h/2
            , w);
    }

    public void draw(GraphicsContext gc, String insertedChar) {
        super.draw(gc);
        selectedCell.setTxt(selectedCell.txt() + insertedChar);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(selectedCell.txt()
            , x + (float) w/2
            , y + (float) h/2
            , w);
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
        selectedCell = new Cell(xCoord, yCoord, "", new Formula("", xCoord, yCoord));
        for (Cell c : cells)
            if (c.xCoord() == xCoord && c.yCoord() == yCoord) {
                if (c.formula() == null) {
                    if (c.value() != 0)
                        selectedCell = new Cell(
                            xCoord,
                            yCoord,
                            c.txt(),
                            new Formula(String.valueOf(c.value()), xCoord, yCoord)
                        );
                    else
                        selectedCell = new Cell(
                            xCoord,
                            yCoord,
                            c.txt(),
                            new Formula("", xCoord, yCoord)
                        );
                }
                else
                    selectedCell = new Cell(
                        xCoord,
                        yCoord,
                        c.value(),
                        new Formula(c.formula().getTxt(), xCoord, yCoord)
                    );
                break;
            }
    }

    public void updateW() {
        w = picMetadata.getCellAbsXs()[xCoord+1] - picMetadata.getCellAbsXs()[xCoord];
    }

    public void updateH() {
        h = picMetadata.getCellAbsYs()[yCoord+1] - picMetadata.getCellAbsYs()[yCoord];
    }
}

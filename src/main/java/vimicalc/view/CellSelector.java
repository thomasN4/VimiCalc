package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Formula;

import java.util.ArrayList;

public class CellSelector extends Visible {

    private int xCoord;
    private int yCoord;
    private Cell selectedCell;

    public CellSelector(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
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
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(selectedCell.txt(), x+45, y+16);
    }

    public void draw(GraphicsContext gc, String insertedChar) {
        super.draw(gc);
        selectedCell.setTxt(selectedCell.txt() + insertedChar);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(selectedCell.txt(), x+45, y+16);
    }

    public void updateX(int x_mov) {
        super.setX(x+x_mov);
    }

    public void updateY(int y_mov) {
        super.setY(y+y_mov);
    }

    public void updateXCoord(int xCoord_mov) {
        xCoord += xCoord_mov;
    }

    public void updateYCoord(int yCoord_mov) {
        yCoord += yCoord_mov;
    }

    public void readCell(@NotNull ArrayList<Cell> cells) {
        selectedCell = new Cell(xCoord, yCoord, "", new Formula(""));
        for (Cell c : cells)
            if (c.xCoord() == xCoord && c.yCoord() == yCoord) {
                if (c.formula() == null) {
                    if (c.value() != 0)
                        selectedCell = new Cell(
                            xCoord,
                            yCoord,
                            c.txt(),
                            new Formula(String.valueOf(c.value()))
                        );
                }
                else
                    selectedCell = new Cell(
                        xCoord,
                        yCoord,
                        c.value(),
                        new Formula(c.formula().getTxt())
                    );
            }
    }
}

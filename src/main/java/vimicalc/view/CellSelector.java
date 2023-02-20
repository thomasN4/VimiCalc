package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Controller;
import vimicalc.model.Cell;

import java.util.ArrayList;

public class CellSelector extends Visible {

    private int xCoord;
    private int yCoord;
    private Cell selectedCell;
    private Cell emptyCell;

    public CellSelector(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        xCoord = x/Controller.DEFAULT_CELL_W;
        yCoord = y/Controller.DEFAULT_CELL_H;
        setEmptyCell(new Cell(xCoord, yCoord));
        setSelectedCell(getEmptyCell());
        System.out.println("Initial empty cell: "+emptyCell.toString());
    }

    public Cell getSelectedCell() {
        return selectedCell;
    }

    public Cell getEmptyCell() {
//        System.out.println("Empty cell: "+emptyCell.toString());
        return emptyCell;
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public String getInsertedTxt() {
        return getSelectedCell().txt();
    }

    public void setInsertedTxt(String insertedTxt) {
        getSelectedCell().setTxt(insertedTxt);
    }

    public void setSelectedCell(Cell selectedCell) {
        this.selectedCell = selectedCell;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(getSelectedCell().txt(), x+45, y+16);
    }

    public void draw(GraphicsContext gc, String insertedChar) {
        System.out.println("Drawing new text.");
        super.draw(gc);
        getSelectedCell().setTxt(getSelectedCell().txt() + insertedChar);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(getSelectedCell().txt(), x+45, y+16);
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
        setEmptyCell(new Cell(xCoord, yCoord));
        setSelectedCell(getEmptyCell());
        for (Cell tC : cells) {
            if (tC.xCoord() == xCoord && tC.yCoord() == yCoord)
                setSelectedCell(tC);
        }
        System.out.println("Selected cell's formula: "+ getSelectedCell().formula().getTxt());
    }

    public void delCharInTxt() {
        getSelectedCell().setTxt(getSelectedCell().txt().substring(0, getSelectedCell().txt().length()-1));
    }

    public void setEmptyCell(Cell emptyCell) {
        this.emptyCell = emptyCell;
    }
}

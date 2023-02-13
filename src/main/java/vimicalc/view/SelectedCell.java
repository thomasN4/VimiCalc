package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;
import vimicalc.model.TextCell;

import java.util.ArrayList;

public class SelectedCell extends Visible {

    private int xCoord;
    private int yCoord;
    private String insertedTxt = "";

    public SelectedCell(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        xCoord = x/Controller.DEFAULT_CELL_W;
        yCoord = y/Controller.DEFAULT_CELL_H;
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public String getInsertedTxt() {
        return insertedTxt;
    }

    public void setInsertedTxt(String insertedTxt) {
        this.insertedTxt = insertedTxt;
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(insertedTxt, x+45, y+16);
    }

    public void draw(GraphicsContext gc, String insertedChar) {
        super.draw(gc);
        insertedTxt += insertedChar;
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(insertedTxt, x+45, y+16);
    }

    public void updateX(int x_mov) {
        super.setX(x+x_mov);
        insertedTxt = "";
    }

    public void updateY(int y_mov) {
        super.setY(y+y_mov);
        insertedTxt = "";
    }

    public void updateX(GraphicsContext gc, int x_mov) {
        super.erase(gc);
        super.setX(x+x_mov);
        super.draw(gc);
        insertedTxt = "";
    }

    public void updateY(GraphicsContext gc, int y_mov) {
        super.erase(gc);
        super.setY(y+y_mov);
        super.draw(gc);
        insertedTxt = "";
    }

    public void updateXCoord(int xCoord_mov) {
        xCoord += xCoord_mov;
    }

    public void updateYCoord(int yCoord_mov) {
        yCoord += yCoord_mov;
    }

    public void readCell(ArrayList<TextCell> textCells) {
        for (TextCell tC : textCells) {
            if (tC.xCoord() == xCoord && tC.yCoord() == yCoord)
                insertedTxt = tC.text();
        }
    }
}

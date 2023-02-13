package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;

public class SelectedCell extends Visible {

    private int xCoord;
    private int yCoord;
    private String insertedTxt = "";

    public SelectedCell(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        setCoords();
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

    public void setCoords() {
        xCoord = x/Controller.DEFAULT_CELL_W;
        yCoord = y/Controller.DEFAULT_CELL_H;
    }

    public void setInsertedTxt(String insertedTxt) {
        this.insertedTxt = insertedTxt;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        setCoords();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        setCoords();
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

    public void updateX(GraphicsContext gc, int x_mov) {
        super.erase(gc);
        super.setX(x+x_mov);
        super.draw(gc);
        setCoords();
        insertedTxt = "";
    }

    public void updateY(GraphicsContext gc, int y_mov) {
        super.erase(gc);
        super.setY(y+y_mov);
        super.draw(gc);
        setCoords();
        insertedTxt = "";
    }
}

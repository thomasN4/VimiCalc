package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class SelectedCell extends Visible {

    private String insertedTxt = "";

    public SelectedCell(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
    }

    public void draw(GraphicsContext gc, String insertedChar) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(insertedTxt+=insertedChar, x+45, y+16);
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
}

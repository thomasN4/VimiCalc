package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.controller.Controller;
import vimicalc.model.Cell;

public class InfoBar extends Visible {

    private String keyStroke;
    private boolean enteringFormula;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
    }

    public void setEnteringFormula(boolean enteringFormula) {
        this.enteringFormula = enteringFormula;
    }

    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    public void draw(GraphicsContext gc, Cell selectedCell) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + Controller.CANVAS_W - 4, y+16);
        gc.setTextAlign(TextAlignment.LEFT);
        if (enteringFormula) {
            gc.fillText("% " + selectedCell.formula().getTxt(), 2, y + 16);
        } else
            gc.fillText('(' + selectedCell.formula().getTxt() + ')', 2, y + 16);
    }
}

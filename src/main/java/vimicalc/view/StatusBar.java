package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class StatusBar extends Visible {
    private String actualMode;
    private String fileName;
    public StatusBar(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
        actualMode = "[NORMAL]";
        fileName = "[new_file.vclc]";
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" "+actualMode+"  "+fileName, 2, y+20);
    }

    public void setActualMode(String actualMode) {
        this.actualMode = actualMode;
    }
}
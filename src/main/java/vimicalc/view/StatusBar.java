package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class StatusBar extends Visible {
    private String mode;
    private String fileName;
    public StatusBar(int x, int y, double w, double h, Color c) {
        super(x, y, w, h, c);
        mode = "[NORMAL]";
        fileName = "[new_file.vclc]";
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" "+ mode +"  "+fileName, 2, y+20);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
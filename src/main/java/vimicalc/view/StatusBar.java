package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class StatusBar extends Visible {
    private String filename;
    private String mode;

    public StatusBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        mode = vimicalc.controller.Controller.MODE[3];
        filename = "[new_file]";
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" "+ mode +"  "+ filename, 2, y+19);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
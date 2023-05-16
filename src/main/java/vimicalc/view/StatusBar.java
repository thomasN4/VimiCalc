package vimicalc.view;

import static vimicalc.controller.Controller.currMode;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class StatusBar extends Visible {
    private String filename;

    public StatusBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        filename = "new_file";
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLUE);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(" [" + currMode + "]  --" + filename + "--", 2, y + (float)h/2);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
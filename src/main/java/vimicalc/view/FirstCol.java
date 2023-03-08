package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class FirstCol extends Visible{
    public FirstCol(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    public void draw(GraphicsContext gc, int absY) {
        super.draw(gc);
        int jump = y;  // équivalent à DEFAULT_CELL_HEIGHT
        for (int i = 1; i < gc.getCanvas().getHeight()/jump; i++) {
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(""+(i+absY/jump), 45, (i+1)*jump-absY%jump-7, jump);
        }
    }
}
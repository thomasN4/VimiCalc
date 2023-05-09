package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;

public class FirstRow extends Visible {
    Positions picPositions;

    public FirstRow(int x, int y, int w, int h, Color c, Positions picPositions) {
        super(x, y, w, h, c);
        this.picPositions = picPositions;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        int cellWidth;
        for (int xC = picPositions.getFirstXC(); xC <= picPositions.getLastXC(); xC++) {
            cellWidth = picPositions.getCellAbsXs()[xC+1] - picPositions.getCellAbsXs()[xC];
            gc.fillText(toAlpha(xC-1)
                , picPositions.getCellAbsXs()[xC] - picPositions.getCamAbsX() + x + (float) cellWidth/2
                , (float) h/2
                , cellWidth);
        }
    }
}
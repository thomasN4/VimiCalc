package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

/**
 * The row number column displayed on the left side of the spreadsheet.
 *
 * <p>Renders numeric row labels (1, 2, 3, ...) centered within each row,
 * using the layout information from {@link Positions}.</p>
 */
public class FirstCol extends simpleRect {
    Positions picPositions;

    /**
     * Creates the row number column.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the background color
     * @param picPositions the position metadata for cell layout
     */
    public FirstCol(int x, int y, int w, int h, Color c, Positions picPositions) {
        super(x, y, w, h, c);
        this.picPositions = picPositions;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        int cellHeight;
        for (int yC = picPositions.getFirstYC(); yC <= picPositions.getLastYC(); yC++) {
            cellHeight = picPositions.getCellAbsYs()[yC+1] - picPositions.getCellAbsYs()[yC];
            gc.fillText(""+yC
                , (float) w/2
                , picPositions.getCellAbsYs()[yC] - picPositions.getCamAbsY() + y + (float) cellHeight/2
                , w);
        }
    }
}
package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;

/**
 * The column header row displayed at the top of the spreadsheet.
 *
 * <p>Renders alphabetic column labels (A, B, C, ...) centered within each
 * column, using the layout information from {@link Positions}.</p>
 */
public class FirstRow extends simpleRect {
    Positions picPositions;

    /**
     * Creates the column header row.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the background color
     * @param picPositions the position metadata for cell layout
     */
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
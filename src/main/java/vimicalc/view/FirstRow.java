package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;
import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;

/**
 * The column header row displayed at the top of the spreadsheet.
 *
 * <p>Renders alphabetic column labels (A, B, C, ...) centered within each
 * column, using the layout information from {@link Positions}.</p>
 */
public class FirstRow extends simpleRect {
    Camera camera;
    Positions picPositions;

    /**
     * Creates the column header row.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the background color
     * @param camera       the viewport camera the label positions derive from
     * @param picPositions the position metadata for cell layout
     */
    public FirstRow(int x, int y, int w, int h, Color c, Camera camera, Positions picPositions) {
        super(x, y, w, h, c);
        this.camera = camera;
        this.picPositions = picPositions;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        // Scale the label font with the view zoom so headers grow with the
        // grid; fillText's max-width squeezes labels that outgrow the header.
        Font prevFont = gc.getFont();
        gc.setFont(Font.font(prevFont.getFamily(), DEFAULT_FONT_SIZE * picPositions.getZoom()));
        int cellWidth;
        for (int xC = picPositions.getFirstXC(); xC <= picPositions.getLastXC(); xC++) {
            cellWidth = picPositions.getCellAbsXs()[xC+1] - picPositions.getCellAbsXs()[xC];
            gc.fillText(toAlpha(xC-1)
                , picPositions.getCellAbsXs()[xC] - camera.getAbsX() + x + (float) cellWidth/2
                , (float) h/2
                , cellWidth);
        }
        gc.setFont(prevFont);
    }
}
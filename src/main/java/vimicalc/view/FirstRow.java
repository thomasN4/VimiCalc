package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.toAlpha;
import static vimicalc.view.Defaults.CHROME_BORDER;
import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.HEADER_SEPARATOR;
import static vimicalc.view.Defaults.HEADER_TXT;

/**
 * The column header row displayed at the top of the spreadsheet.
 *
 * <p>Renders alphabetic column labels (A, B, C, ...) centered within each
 * column, using the layout information from {@link Positions}. Also paints
 * the top-left corner block shared with {@link FirstCol}, so it must draw
 * after the row gutter.</p>
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
     * @param c            the background fill
     * @param camera       the viewport camera the label positions derive from
     * @param picPositions the position metadata for cell layout
     */
    public FirstRow(int x, int y, int w, int h, Paint c, Camera camera, Positions picPositions) {
        super(x, y, w, h, c);
        this.camera = camera;
        this.picPositions = picPositions;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        // The corner block above the row gutter, painted with the same fill so
        // the whole top edge reads as one continuous header surface.
        gc.setFill(c);
        gc.fillRect(0, y, x, h);
        gc.setFill(HEADER_TXT);
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

        // Inset hairline separators between column labels; the +0.5 centers
        // each 1px stroke on the pixel so lines render crisp.
        gc.setStroke(HEADER_SEPARATOR);
        gc.setLineWidth(1);
        for (int xC = picPositions.getFirstXC() + 1; xC <= picPositions.getLastXC() + 1; xC++) {
            double sepX = picPositions.getCellAbsXs()[xC] - camera.getAbsX() + x + 0.5;
            gc.strokeLine(sepX, y + 4, sepX, y + h - 5);
        }
        // Edge lines where the header meets the grid (bottom, full width
        // including the corner) and where the corner meets the gutter.
        gc.setStroke(CHROME_BORDER);
        gc.strokeLine(0, y + h - 0.5, x + w, y + h - 0.5);
        gc.strokeLine(x - 0.5, y, x - 0.5, y + h);
    }
}
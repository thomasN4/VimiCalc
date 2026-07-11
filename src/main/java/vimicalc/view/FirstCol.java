package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import static vimicalc.view.Defaults.CHROME_BORDER;
import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.HEADER_SEPARATOR;
import static vimicalc.view.Defaults.HEADER_TXT;

/**
 * The row number column displayed on the left side of the spreadsheet.
 *
 * <p>Renders numeric row labels (1, 2, 3, ...) centered within each row,
 * using the layout information from {@link Positions}.</p>
 */
public class FirstCol extends simpleRect {
    Camera camera;
    Positions picPositions;

    /**
     * Creates the row number column.
     *
     * @param x            the x pixel position
     * @param y            the y pixel position
     * @param w            the width in pixels
     * @param h            the height in pixels
     * @param c            the background fill
     * @param camera       the viewport camera the label positions derive from
     * @param picPositions the position metadata for cell layout
     */
    public FirstCol(int x, int y, int w, int h, Paint c, Camera camera, Positions picPositions) {
        super(x, y, w, h, c);
        this.camera = camera;
        this.picPositions = picPositions;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(HEADER_TXT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        // Scale the label font with the view zoom so headers grow with the
        // grid; fillText's max-width squeezes labels that outgrow the gutter.
        Font prevFont = gc.getFont();
        gc.setFont(Font.font(prevFont.getFamily(), DEFAULT_FONT_SIZE * picPositions.getZoom()));
        int cellHeight;
        for (int yC = picPositions.getFirstYC(); yC <= picPositions.getLastYC(); yC++) {
            cellHeight = picPositions.getCellAbsYs()[yC+1] - picPositions.getCellAbsYs()[yC];
            gc.fillText(""+yC
                , (float) w/2
                , picPositions.getCellAbsYs()[yC] - camera.getAbsY() + y + (float) cellHeight/2
                , w);
        }
        gc.setFont(prevFont);

        // Inset hairline separators between row labels; the +0.5 centers each
        // 1px stroke on the pixel so lines render crisp.
        gc.setStroke(HEADER_SEPARATOR);
        gc.setLineWidth(1);
        for (int yC = picPositions.getFirstYC() + 1; yC <= picPositions.getLastYC() + 1; yC++) {
            double sepY = picPositions.getCellAbsYs()[yC] - camera.getAbsY() + y + 0.5;
            gc.strokeLine(x + 4, sepY, x + w - 5, sepY);
        }
        // Edge line where the gutter meets the grid.
        gc.setStroke(CHROME_BORDER);
        gc.strokeLine(x + w - 0.5, y, x + w - 0.5, y + h);
    }
}
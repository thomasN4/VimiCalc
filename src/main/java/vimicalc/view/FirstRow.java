package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Metadata;

import static vimicalc.utils.Conversions.toAlpha;

public class FirstRow extends Visible {
    Metadata picMetadata;

    public FirstRow(int x, int y, int w, int h, Color c, Metadata picMetadata) {
        super(x, y, w, h, c);
        this.picMetadata = picMetadata;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        int cellWidth;
        for (int xC = picMetadata.getFirstXC(); xC <= picMetadata.getLastXC(); xC++) {
//            System.out.println("FirstRow currCellAbsX: " +
//                (picMetadata.getCellAbsXs()[xC] - picMetadata.getCamAbsX() + x));
            cellWidth = picMetadata.getCellAbsXs()[xC] - picMetadata.getCellAbsXs()[xC-1];
            gc.fillText(toAlpha(xC-1)
                , picMetadata.getCellAbsXs()[xC] - picMetadata.getCamAbsX() + x + (float) cellWidth/2
                , (float) h/2
                , cellWidth);
        }
    }
}
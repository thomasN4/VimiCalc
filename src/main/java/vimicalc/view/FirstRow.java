package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Metadata;

import static vimicalc.utils.Conversions.toAlpha;

public class FirstRow extends Visible {
    Metadata currPicMetaData;

    public FirstRow(int x, int y, int w, int h, Color c, Metadata currPicMetaData) {
        super(x, y, w, h, c);
        this.currPicMetaData = currPicMetaData;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        for (int xC = currPicMetaData.getFirstXC(); xC <= currPicMetaData.getLastXC(); xC++) {
            gc.fillText(""+toAlpha(xC-1)
                , currPicMetaData.getCellAbsXs()[xC] % currPicMetaData.getPicW() + 1
                , 16
                , currPicMetaData.getCellAbsXs()[xC] - currPicMetaData.getCellAbsXs()[xC-1]);
        }
    }
}
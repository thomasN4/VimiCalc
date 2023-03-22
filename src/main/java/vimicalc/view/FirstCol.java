package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Metadata;

public class FirstCol extends Visible {
    Metadata currPicMetaData;

    public FirstCol(int x, int y, int w, int h, Color c, Metadata currPicMetaData) {
        super(x, y, w, h, c);
        this.currPicMetaData = currPicMetaData;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        for (int yC = currPicMetaData.getFirstYC(); yC <= currPicMetaData.getLastYC(); yC++) {
            gc.fillText(""+yC
                , 45
                , currPicMetaData.getCellAbsYs()[yC] % currPicMetaData.getPicH()
                , currPicMetaData.getCellAbsYs()[yC] - currPicMetaData.getCellAbsYs()[yC-1]);
        }
    }
}
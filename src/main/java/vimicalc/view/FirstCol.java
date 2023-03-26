package vimicalc.view;

import javafx.geometry.VPos;
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
        gc.setTextBaseline(VPos.CENTER);
        int cellHeight;
        for (int yC = currPicMetaData.getFirstYC(); yC <= currPicMetaData.getLastYC(); yC++) {
//            System.out.println("FirstCol currCellAbsY: " +
//                (currPicMetaData.getCellAbsYs()[yC] - currPicMetaData.getCamAbsY() + y));
            cellHeight = currPicMetaData.getCellAbsYs()[yC] - currPicMetaData.getCellAbsYs()[yC-1];
            gc.fillText(""+yC
                , (float) w/2
                , currPicMetaData.getCellAbsYs()[yC] - currPicMetaData.getCamAbsY() + y + (float) cellHeight/2
                , cellHeight);
        }
    }
}
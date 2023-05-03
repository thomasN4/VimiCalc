package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class FirstCol extends Visible {
    Metadata picMetadata;

    public FirstCol(int x, int y, int w, int h, Color c, Metadata picMetadata) {
        super(x, y, w, h, c);
        this.picMetadata = picMetadata;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        int cellHeight;
        for (int yC = picMetadata.getFirstYC(); yC <= picMetadata.getLastYC(); yC++) {
//            System.out.println("FirstCol currCellAbsY: " +
//                (picMetadata.getCellAbsYs()[yC] - picMetadata.getCamAbsY() + y));
            cellHeight = picMetadata.getCellAbsYs()[yC+1] - picMetadata.getCellAbsYs()[yC];
            gc.fillText(""+yC
                , (float) w/2
                , picMetadata.getCellAbsYs()[yC] - picMetadata.getCamAbsY() + y + (float) cellHeight/2
                , w);
        }
    }
}
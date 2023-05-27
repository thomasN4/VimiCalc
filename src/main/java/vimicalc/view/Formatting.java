package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class Formatting {
    Color cellColor, txtColor;
    VPos vPos;
    TextAlignment alignment;
    Font font;
    int xC, yC, w, h;

    public Formatting(Color cellColor, Color txtColor, VPos vPos, TextAlignment alignment, Font font,
                      int xC, int yC, int w, int h) {
        this.cellColor = cellColor;
        this.txtColor = txtColor;
        this.vPos = vPos;
        this.alignment = alignment;
        this.font = font;
        this.xC = xC;
        this.yC = yC;
        this.w = w;
        this.h = h;
    }

    public void renderCell(@NotNull GraphicsContext gc, int x, int y, int w, int h, String txt) {
        gc.setFill(txtColor);
        gc.setTextAlign(alignment);
        gc.setTextBaseline(vPos);
        gc.setFont(font);
        gc.fillText(txt, x, y, w);
        gc.setFill(cellColor);
        gc.fillRect(x, y, w, h);
    }
}
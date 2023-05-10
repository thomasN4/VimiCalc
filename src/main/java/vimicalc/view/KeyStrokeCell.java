package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class KeyStrokeCell extends Visible {
    private String keyStroke;
    public KeyStrokeCell(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
    }

    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLUE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(keyStroke, (float) w/2, (float) h/2, w);
    }
}

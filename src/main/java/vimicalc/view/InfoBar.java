package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class InfoBar extends Visible {

    private String keyStroke;
    private String infobarTxt;
    private boolean enteringCommandInVISUAL;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
    }
    public boolean isEnteringCommandInVISUAL() { return enteringCommandInVISUAL; }

    public void setEnteringCommandInVISUAL(boolean enteringCommandInVISUAL) {
        this.enteringCommandInVISUAL = enteringCommandInVISUAL;
    }
    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    public void setCommandTxt(String infobarTxt) { this.infobarTxt = ":" + infobarTxt; }

    public void setEnteringFormula(String infobarTxt) {
        this.infobarTxt = "% " + infobarTxt.replace(".0" , "");
    }

    public void setInfobarTxt(String infobarTxt) {
        if (infobarTxt != null)
            this.infobarTxt = "(" + infobarTxt + ")";
        else
            this.infobarTxt = "(I)";
    }
    public String getInfobarTxt() { return infobarTxt; }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + w - 4, y + 16);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(infobarTxt,2, y + 16);
    }
}
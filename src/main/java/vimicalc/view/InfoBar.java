package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.model.Cell;
import vimicalc.model.Formula;

public class InfoBar extends Visible {

    private String keyStroke;
    private String commandTxt;
    private String infobarTxt;
    private boolean enteringCommandInVISUAL;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
        commandTxt = "";
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
        this.infobarTxt = "% " + infobarTxt;
    }

    public void setInfobarTxt(String infobarTxt) {
        if (!infobarTxt.isEmpty()) {
            this.infobarTxt = "(" + infobarTxt + ")";
        } else {
            this.infobarTxt = "(I)";
        }
    }

    public String getInfobarTxt() { return infobarTxt; }

    public void draw(GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + w - 4, y + 16);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(infobarTxt,2, y + 16);
    }
}
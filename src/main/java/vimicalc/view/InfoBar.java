package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InfoBar extends simpleRect {

    public static String iBarExpr;
    private String infobarTxt;
    private boolean enteringCommandInVISUAL;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        iBarExpr = "";
    }
    public boolean isEnteringCommandInVISUAL() {
        return enteringCommandInVISUAL;
    }

    public void setEnteringCommandInVISUAL(boolean enteringCommandInVISUAL) {
        this.enteringCommandInVISUAL = enteringCommandInVISUAL;
    }

    public void setCommandTxt(String infobarTxt) {
        this.infobarTxt = ":" + infobarTxt;
    }

    public void setEnteringFormula(String infobarTxt) {
        this.infobarTxt = "% " + infobarTxt.replace(".0" , "");
    }

    public void setInfobarTxt(String infobarTxt) {
        this.infobarTxt = Objects.requireNonNullElse(infobarTxt, "(=I)");
    }

    public String getInfobarTxt() {
        return infobarTxt;
    }

    @Override
    public void draw(@NotNull GraphicsContext gc) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(iBarExpr, x + w - 4, y + (float)h/2);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(infobarTxt,2, y + (float)h/2);
    }
}
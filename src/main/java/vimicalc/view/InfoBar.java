package vimicalc.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.model.Cell;

import static vimicalc.utils.Conversions.isNumber;

public class InfoBar extends Visible {

    private String keyStroke;
    private String commandTxt;
    private String infobarTxt;
    private boolean enteringCommand;
    private boolean enteringCommandInVISUAL;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
        commandTxt = "";
    }
    public boolean isEnteringCommandInVISUAL() { return enteringCommandInVISUAL; }
    public String isEnteringCommand() { return ":" + commandTxt; }

    public void setEnteringCommand(boolean enteringCommand) {
        this.enteringCommand = enteringCommand;
    }

    public void setEnteringCommandInVISUAL(boolean enteringCommandInVISUAL) {
        this.enteringCommandInVISUAL = enteringCommandInVISUAL;
    }

    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    public void setCommandTxt(String commandTxt) {
        this.commandTxt = commandTxt;
    }

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
    public void draw(GraphicsContext gc, Cell selectedCell) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + w - 4, y + 16);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(infobarTxt,2, y + 16);
        /*if (enteringFormula) {
            gc.fillText("% " + selectedCell.formula().getTxt(), 2, y + 16);
        } else if (enteringCommand) {
            gc.fillText(':' + commandTxt, 2, y + 16);
        } else if (enteringCommandInVISUAL) {
            gc.fillText(":'<,'>" + commandTxt, 2, y + 16);
        } else if (!selectedCell.formula().getTxt().equals("")) {
            gc.fillText('(' + selectedCell.formula().getTxt() + ')', 2, y + 16);
        } else if (isNumber(selectedCell.txt())) {
            gc.fillText("(" + selectedCell.value() + ")", 2, y + 16);
        } else if (error) {
            gc.fillText(errorTxt, 2, y + 16);
        } else {
            gc.fillText("(I)", 2, y + 16);
        }*/
    }
}
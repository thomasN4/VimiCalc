package vimicalc.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import vimicalc.model.Cell;

public class InfoBar extends Visible {

    private String keyStroke;
    private String commandTxt;
    private boolean enteringFormula;
    private boolean enteringCommand;
    private boolean enteringCommandInVISUAL;

    public InfoBar(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
        keyStroke = "";
        commandTxt = "";
    }

    public boolean isEnteringCommandInVISUAL() {
        return enteringCommandInVISUAL;
    }

    public void setEnteringFormula(boolean enteringFormula) {
        this.enteringFormula = enteringFormula;
    }

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

    public void draw(GraphicsContext gc, Cell selectedCell) {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(keyStroke, x + w - 4, y + 16);
        gc.setTextAlign(TextAlignment.LEFT);
        if (enteringFormula) {
            gc.fillText("% " + selectedCell.formula().getTxt(), 2, y + 16);
        } else if (enteringCommand) {
            gc.fillText(':' + commandTxt, 2, y + 16);
        } else if (enteringCommandInVISUAL) {
            gc.fillText(":'<,'>" + commandTxt, 2, y + 16);
        } else if (selectedCell.formula() != null) {
            gc.fillText('(' + selectedCell.formula().getTxt() + ')', 2, y + 16);
        } else if (!selectedCell.txt().equals("")) {
            gc.fillText("(" + selectedCell.value() + ")", 2, y + 16);
        } else
            gc.fillText("(IDENTITY)", 2, y + 16);
    }
}

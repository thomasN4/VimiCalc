package vimicalc.controller;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;
import vimicalc.view.Visible;

import static vimicalc.controller.Controller.currMode;
import static vimicalc.controller.Mode.NORMAL;

public class HelpMenu extends Visible {
    private int position;
    private final GraphicsContext gc;
    private final String[] text = {
        "=====The Basics=====\n",
        "Use 'j' and 'k' to scroll up and down the help menu, and 'ESC' to close it.",
        "As you might have guessed, the whole thing works a lot like Vim.",
        "Keys such as 'h', 'j', 'k' and 'l' are for moving around the cells, 'i' is for inserting plain text, etc.",
        "It also uses modes such as INSERT, NORMAL, COMMAND and VISUAL,",
        "and some non-Vim ones such as FORMULA.",
        "\n",
        "\t-NORMAL mode allows you to do all sorts of commands with the letters part of the keyboard,",
        "\t and is the 'usual' mode where most of the editing happens and where you switch to other modes.",
        "\n",
        "\t-INSERT mode is for inserting plain text into a cell. Just type 'a' or 'i' to enter it and 'ESC' to leave",
        "\n",
        "\t-FORMULA mode is for entering formulas (as in Microsoft Excel). They use Reverse Polish Notation.",
        "\t Examples: 'B2:D3 F7:G9 matMult', '3 5 8 * +', 'kl -3j / 3 mod'",
        "\t That last one used relative coordinates.",
        "\t 'ESC'ing will just exit this mode and cancel the formula. If you want to save the formula to the cell,",
        "\t you would have to press 'ENTER' instead.",
        "\n",
        "\t-VISUAL mode allows you to select several cells and do things to them.",
        "\t Things such as merging cells by pressing 'm', or copying them by pressing 'y'",
    };

    public HelpMenu(GraphicsContext gc) {
        super(30, 30, 720, 480, Color.LIGHTYELLOW);
        this.gc = gc;
        position = 0;
    }

    public void naviguate(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case J -> {
                if (position < text.length) position++;
            }
            case K -> {
                if (position > 0) position--;
            }
            case ESCAPE -> {
                position = 0;
                currMode = NORMAL;
            }
        }
        drawText();
    }

    public int getPosition() {
        return position;
    }

    public String[] getText() {
        return text;
    }

    public void drawText() {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.TOP);
        gc.setTextAlign(TextAlignment.LEFT);
        StringBuilder txt = new StringBuilder();
        for (int i = position; i < text.length; i++)
            txt.append(text[i]).append('\n');
        gc.fillText(txt.toString(), x+10, y+10);
    }
}
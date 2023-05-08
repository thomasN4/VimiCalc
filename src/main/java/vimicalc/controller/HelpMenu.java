package vimicalc.controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.view.Visible;

import static vimicalc.controller.Controller.currMode;
import static vimicalc.controller.Controller.onKeyPressed;
import static vimicalc.controller.Mode.NORMAL;

public class HelpMenu extends Visible {
    private int position;
    private final GraphicsContext gc;
    private final String basics = "" +
            "=====The Basics=====\n" +
            "Use 'j' and 'k' to scroll up and down the help menu, and 'ESC' to close it.\n" +
            "As you might have guessed, the whole thing works a lot like Vim.\n" +
            "Keys such as 'h', 'j', 'k' and 'l' are for moving around the cells, " +
            "'i' is for inserting text (plain text), etc.\n" +
            "It also uses modes such as INSERT, NORMAL, COMMAND and VISUAL, and some non-Vim ones such as FORMULA.\n" +
            "\t-NORMAL mode allows you to do all sorts of commands with the letters part of the keyboard, " +
            "and is the 'usual' mode where most of the editing happens.\n" +
            "\t-INSERT mode is for inserting plain text into a cell. Just type 'a' or 'i' to enter it and 'ESC' to leave\n" +
            "\t-FORMULA mode is for entering formulas (as in Microsoft Excel). They use Reverse Polish Notation.\n" +
            "\t Examples: 'B2:D3 F7:G9 matMult', '3 5 8 * +', 'kl -3j / 3 mod'\n" +
            "\t That last one used relative coordinates.\n" +
            "\t-VISUAL mode lets you select several cells.";

    public HelpMenu(GraphicsContext gc) {
        super(10, 10, 760, 540, Color.LIGHTYELLOW);
        this.gc = gc;
        position = 0;
    }

    public void naviguate(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case J -> {
                position -= 5;
                drawText();
            }
            case K -> {
                position += 5;
                drawText();
            }
            case ESCAPE -> {
                position = 0;
                currMode = NORMAL;
            }
        }
    }

    public void drawText() {
        gc.setFill(Color.BLACK);
        gc.fillText(basics, x+5, y+5-position, w);
    }
}
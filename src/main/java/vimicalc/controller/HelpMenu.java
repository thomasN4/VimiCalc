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
        "=====Overview of how WeSpreadSheets works=====",
        "\n",
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
        "\t 'ESC'-ing will just exit this mode and cancel the formula. If you want to save the formula to the cell,",
        "\t you would have to press 'ENTER' instead.",
        "\n",
        "\t-VISUAL mode allows you to select several cells and do things to them.",
        "\t Things such as merging cells by pressing 'm', or copying them by pressing 'y',",
        "\t and applying formulas while appending the destination coordinate at the end (eg 'A3:B4 det AX8').",
        "\n",
        "\t-COMMAND mode is for entering certain uh commands. Examples:",
        "\t resCol [offset from default width in pixels], w [file path] for saving a file, and of course,",
        "\t q for quitting",
        "\n",
        "And then we have KeyCommands, which are basically actions made up of keyboard shortcuts entered in",
        "NORMAL mode. They appear at the bottom right of the window as you type.",
        "Here are some more information about them.",
        "\n",
        "\t-Multipliers: you just type a number before typing the rest of the command.",
        "\n",
        "\t-Macros: You type 'q' and then supply a letter as the name of the macro. You can then just do whatever ",
        "\t and everything will be recorded until you press 'q' again. To replay it, just type @[the letter].",
        "\t You could also prefix with a multiplier or stick them inside a conditional KeyCommand.",
        "\n",
        "\t-Conditional KeyCommands: basically '<[formula]{[then]{[else]}'. You can omit the else part.",
        "\t If the formula doesn't evaluate to 0, the KeyCommand in the 'then' part is executed.",
        "\t Some examples would be helpful: '<C3 2 mod{d5J6l}', '<6 5j2k 3 mod{@a{@b}'",
        "\n",
        "\t-The cancel the writing of a KeyCommand, press Ctrl-C",
        "\n",
        "For more info, there is only the source code in the meantime."
    };

    public HelpMenu(GraphicsContext gc) {
        super(30, 30, 720, 480, Color.LIGHTYELLOW);
        this.gc = gc;
        position = 0;
    }

    public void navigate(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case J -> {
                System.out.println("Scrolling down...");
                if (position < text.length) position += 1;
            }
            case K -> {
                System.out.println("Scrolling up...");
                if (position > 0) position -= 1;
            }
            case ESCAPE -> {
                position = 0;
                currMode = NORMAL;
            }
        }
        drawText();
    }

    public String percentage() {
        return (int) (100.0 * position / text.length) + "%";
    }

    public void drawText() {
        super.draw(gc);
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.TOP);
        gc.setTextAlign(TextAlignment.LEFT);
        StringBuilder txt = new StringBuilder();
        for (int i = position; i < position + 24; i++) {
            if (i < text.length)
                txt.append(text[i]).append('\n');
            else break;
        }
        gc.fillText(txt.toString(), x+10, y+10);
    }
}
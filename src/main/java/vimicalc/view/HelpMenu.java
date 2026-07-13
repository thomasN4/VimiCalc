package vimicalc.view;

import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An overlay window that displays built-in help text when the user enters
 * HELP mode (via {@code :h}, {@code :help}, or {@code :?}).
 *
 * <p>Supports scrolling with {@code j}/{@code k} keys and dismissal with
 * {@code ESC}. The text is a static array of lines covering modes,
 * key commands, macros, formulas, and conditional expressions. Backed by a
 * scene-graph {@link Label}; the controller owns mode transitions.</p>
 */
public class HelpMenu {
    private int position;
    private final Label helpLabel;
    private final String[] text = {
        "=====Overview of how WeSpreadSheet works=====",
        "\n",
        "Use 'j' and 'k' to scroll up and down the help menu, and 'ESC' to close it.",
        "As you might have guessed, the whole thing works a lot like Vim.",
        "Keys such as 'h', 'j', 'k' and 'l' are for moving around the cells, 'i' is for inserting plain text, etc.",
        "It also uses modes such as INSERT, NORMAL, COMMAND and VISUAL,",
        "and some non-Vim ones such as FORMULA.",
        "\n",
        "\t-NORMAL mode allows you to do all sorts of commands with the letters part of the keyboard,",
        "\t and is the 'usual' mode where most of the editing happens and where you switch to other modes.",
        "\t When not in this mode, press 'ESC' to return.",
        "\n",
        "\t-INSERT mode is for inserting plain text into a cell. Just type 'a' or 'i' to enter it and 'ESC' to leave",
        "\n",
        "\t-FORMULA mode is for entering formulas (as in Microsoft Excel). Press '=' in NORMAL mode to enter it.",
        "\t They use Reverse Polish Notation.",
        "\t Examples: 'B2:D3 F7:G9 matMul', '3 5 8 * +', 'kl -3j / 3 mod'",
        "\t That last one used relative coordinates.",
        "\t 'ESC'-ing will just exit this mode and cancel the formula. If you want to save the formula to the cell,",
        "\t you would have to press 'ENTER' instead.",
        "\n",
        "\t-VISUAL mode allows you to select several cells and do things to them.",
        "\t Things such as merging cells by pressing 'm', copying them by pressing 'y', deleting them with 'd',",
        "\t entering COMMAND mode with ';' to run commands on the selection,",
        "\t and applying formulas while appending the destination coordinate at the end (eg 'A3:B4 det AX8').",
        "\n",
        "\t-COMMAND mode is for entering certain uh commands. Start by typing ':' in NORMAL mode. Examples:",
        "\t :resizeColumn [offset from default width in pixels], :write [file path] for saving a file,",
        "\t and of course, :quit for quitting.",
        "\t The short Vim-style names still work as aliases: :w, :q, :wq, :e, :resCol, ...",
        "\t Formatting commands: :cellColor and :textColor (CSS color names or #hex values),",
        "\t :fontSize [px], :fontWeight [bold|normal|100-900], :boldText and :italicText toggles.",
        "\t Formatting commands without an argument reset the property to its default.",
        "\t View zoom: :zoom [25-400] sets the zoom percentage (no argument resets to 100%),",
        "\t or use Ctrl-= / Ctrl-- / Ctrl-0 in NORMAL mode to zoom in / out / reset.",
        "\t While you type a command name, a popup lists the fuzzily-matching commands",
        "\t (e.g. 'rc' matches :resizeColumn). 'TAB' or 'Ctrl-N' cycle through them,",
        "\t 'Shift-TAB' or 'Ctrl-P' cycle backwards, and 'ESC' dismisses the popup.",
        "\t :gridlines toggles the cell gridlines on and off.",
        "\n",
        "And then we have KeyCommands, which are basically actions made up of keyboard shortcuts entered in",
        "NORMAL mode. They appear at the bottom right of the window as you type.",
        "Here are some more information about them.",
        "\n",
        "\t-Multipliers: you just type a number before typing the rest of the command, like '5j' or '31@x'",
        "\n",
        "\t-Macros: You type 'q' and then supply a letter as the name of the macro. You can then just do whatever ",
        "\t and everything will be recorded until you press 'q' again. To replay it, just type @[the letter].",
        "\t You could also prefix with a multiplier or stick them inside a conditional KeyCommand.",
        "\n",
        "\t-Conditional KeyCommands: basically '<formula{then}{else}'. You can omit the else part.",
        "\t If the formula doesn't evaluate to 0, the KeyCommand in the 'then' part is executed.",
        "\t Some examples would be helpful: '<C3 2 mod{d5j6l}', '<6 5j2k 3 mod{@a{@b}'",
        "\n",
        "\t-To cancel the writing of a KeyCommand, press Ctrl-C",
        "\n",
        "\t-Go to a specific cell with 'g' followed by the coordinates, like 'gA3'",
        "\n",
        "\t-Repeat the last command with '.'",
        "\n",
        "\t-'ZZ' saves and quits, 'ZQ' quits without saving",
        "\n",
        "\t-'Ctrl-O' jumps back to the previous cursor location",
        "\n",
        "In INSERT mode, 'ESC' saves and exits while 'Shift+ESC' cancels without saving.",
        "\n",
        "For more info, there is only the source code and the javadoc for now."
    };

    /**
     * Creates the help menu overlay bound to the given label.
     *
     * @param helpLabel the scene-graph label to show help text on
     */
    public HelpMenu(Label helpLabel) {
        this.helpLabel = helpLabel;
        position = 0;
    }

    /**
     * Handles keyboard input within the help menu.
     * {@code J} scrolls down, {@code K} scrolls up, {@code ESC} only resets
     * the scroll position — the controller flips the mode and calls
     * {@link #hide()}.
     *
     * @param event the key event to process
     */
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
            case ESCAPE -> position = 0;
            default -> {}
        }
        updateText();
    }

    /**
     * Shows the help overlay at the top of the document.
     */
    public void show() {
        position = 0;
        updateText();
        helpLabel.setVisible(true);
    }

    /**
     * Hides the help overlay.
     */
    public void hide() {
        helpLabel.setVisible(false);
    }

    /**
     * Returns the current scroll position as a percentage string (e.g. "42%").
     *
     * @return the scroll percentage
     */
    public String percentage() {
        return (int) (100.0 * position / text.length) + "%";
    }

    /** Updates the label to the 24-line slice starting at the current scroll position. */
    private void updateText() {
        StringBuilder txt = new StringBuilder();
        for (int i = position; i < position + 24; i++) {
            if (i < text.length)
                txt.append(text[i]).append('\n');
            else break;
        }
        helpLabel.setText(txt.toString());
    }
}

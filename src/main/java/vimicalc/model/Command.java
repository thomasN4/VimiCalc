package vimicalc.model;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;
import vimicalc.view.Formatting;

import static vimicalc.controller.Controller.*;

/**
 * Interprets colon commands entered in COMMAND mode (e.g. {@code :w}, {@code :q},
 * {@code :cellColor red}).
 *
 * <p>Supported commands:</p>
 * <ul>
 *   <li>{@code :h}, {@code :help}, {@code :?} — open the help menu</li>
 *   <li>{@code :e <path>} — open/read a {@code .wss} file</li>
 *   <li>{@code :w [path]} — save the current sheet</li>
 *   <li>{@code :wq [path]} — save and quit</li>
 *   <li>{@code :q} — quit without saving</li>
 *   <li>{@code :resCol <offset>} — resize the current column</li>
 *   <li>{@code :resRow <offset>} — resize the current row</li>
 *   <li>{@code :purgeDeps} — clear all formula dependencies</li>
 *   <li>{@code :cellColor [color]} — set the cell background color</li>
 *   <li>{@code :txtColor [color]} — set the cell text color</li>
 *   <li>{@code :boldTxt} — toggle bold text on the current cell</li>
 * </ul>
 */
public class Command extends Interpretable {
    /**
     * Creates a command from the given text at the specified cell position.
     *
     * @param txt the command text (without the leading colon)
     * @param xC  the column index of the current cell
     * @param yC  the row index of the current cell
     */
    public Command(String txt, int xC, int yC) {
        super(txt, xC, yC);
    }
    /**
     * Set to {@code false} after evaluation if the entered command was not recognized.
     * Starts as {@code true} and is reset to {@code true} at the beginning of every
     * {@link #interpret(Lexeme[], Sheet)} call.
     */
    public boolean commandExists = true;

    /**
     * Reads a {@code .wss} file into the sheet.
     *
     * @param sheet   the target sheet
     * @param command the lexed command tokens (path in {@code command[1]})
     * @throws Exception if the file cannot be read
     */
    public void readFile(Sheet sheet, Lexeme[] command) throws Exception {
        sheet.readFile(command[1].getFunc());
    }

    /**
     * Writes the sheet to a {@code .wss} file.
     *
     * @param sheet   the sheet to save
     * @param command the lexed command tokens (optional path in {@code command[1]})
     * @throws Exception if the file cannot be written
     */
    public void writeFile(Sheet sheet, Lexeme[] command) throws Exception {
        if (command.length == 1) sheet.writeFile();
        else sheet.writeFile(command[1].getFunc());
    }

    public Lexeme[] interpret(Lexeme[] command, Sheet sheet) throws Exception {
        commandExists = true;
        switch (command[0].getFunc()) {
            case "h", "help", "?" -> {
                infoBar.setInfobarTxt(helpMenu.percentage());
                infoBar.draw(gc);
                currMode = Mode.HELP;
            }
            case "e" -> readFile(sheet, command);
            case "resCol" -> sheet.getPositions().generate(
                new int[]{xC, (int) command[1].getVal()},
                true
            );
            case "resRow" -> sheet.getPositions().generate(
                new int[]{yC, (int) command[1].getVal()},
                false
            );
            case "purgeDeps" -> sheet.purgeDependencies();
            case "w" -> writeFile(sheet, command);
            case "wq" -> {
                try {
                    writeFile(sheet, command);
                } catch (Exception ignored) {}
                Platform.exit();
            }
            case "q" -> Platform.exit();
            case "cellColor" -> {
                if (command.length == 1) cellColor("", sheet);
                else cellColor(command[1].getFunc(), sheet);
            }
            case "txtColor" -> {
                if (command.length == 1) txtColor("", sheet);
                else txtColor(command[1].getFunc(), sheet);
            }
            case "boldTxt" -> boldTxt(sheet);
            default -> {
                commandExists = false;
                throw new Exception("Command \"" + command[0].getFunc() + "\" doesn't exist.");
            }
        }
        return new Lexeme[]{new Lexeme(0)};
    }

    private void cellColor(@NotNull String color, Sheet sheet) {
        Color cC = switch (color) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "vLGray" -> Color.DIMGRAY;  // Note: despite the name, maps to DIMGRAY (dark gray)
            case "lGray" -> Color.LIGHTGRAY;
            case "gray" -> Color.GRAY;
            case "dGray" -> Color.DARKGRAY;
            case "black" -> Color.BLACK;
            // Note: "white" is not supported for cellColor (use default/empty instead);
            // txtColor does support "white" for white-on-dark styling.
            default -> DEFAULT_CELL_C;
        };

        Formatting f;
        System.out.println("Executing command 'cellColor'...");
        f = sheet.findFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setCellColor(cC);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.findFormatting(xC, yC));
    }

    private void txtColor(@NotNull String color, Sheet sheet) {
        Color tC = switch (color) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "white" -> Color.WHITE;
            case "vLGray" -> Color.DIMGRAY;
            case "lGray" -> Color.LIGHTGRAY;
            case "gray" -> Color.GRAY;
            case "dGray" -> Color.DARKGRAY;
            default -> DEFAULT_TXT_C;
        };

        Formatting f;
        System.out.println("Executing command 'txtColor'...");
        f = sheet.findFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setTxtColor(tC);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.findFormatting(xC, yC));
    }

    private void boldTxt(@NotNull Sheet sheet) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
            f.setFontWeight(FontWeight.BOLD);
        } else if (f.getFontWeight().equals("bold")) {
            f.setFontWeight(FontWeight.NORMAL);
            if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        } else f.setFontWeight(FontWeight.BOLD);
    }
}

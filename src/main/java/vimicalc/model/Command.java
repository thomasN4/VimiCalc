package vimicalc.model;

import org.jetbrains.annotations.NotNull;
import vimicalc.view.Formatting;

import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.DEFAULT_ZOOM;

/**
 * Interprets colon commands entered in COMMAND mode (e.g. {@code :w}, {@code :q},
 * {@code :cellColor red}).
 *
 * <p>Supported commands:</p>
 * <ul>
 *   <li>{@code :h}, {@code :help}, {@code :?} — open the help menu</li>
 *   <li>{@code :e <path>} — open/read a {@code .json} file</li>
 *   <li>{@code :w [path]} — save the current sheet</li>
 *   <li>{@code :wq [path]} — save and quit</li>
 *   <li>{@code :q} — quit without saving</li>
 *   <li>{@code :resCol <offset>} — resize the current column</li>
 *   <li>{@code :resRow <offset>} — resize the current row</li>
 *   <li>{@code :purgeDeps} — clear all formula dependencies</li>
 *   <li>{@code :cellColor [color]} — set the cell background color</li>
 *   <li>{@code :txtColor [color]} — set the cell text color</li>
 *   <li>{@code :boldTxt} — toggle bold text on the current cell</li>
 *   <li>{@code :italicTxt} — toggle italic text on the current cell</li>
 *   <li>{@code :fontSize [px]} — set the font size (no argument resets to default)</li>
 *   <li>{@code :fontWeight [bold|normal|100-900]} — set the font weight (no argument resets to normal)</li>
 *   <li>{@code :zoom [25-400]} — set the view zoom percentage (no argument resets to 100%)</li>
 * </ul>
 *
 * <p>Color arguments accept the built-in names (red, green, blue, white, black,
 * gray, lGray, dGray, vLGray), any CSS color name (e.g. crimson, teal), or a
 * hex value like {@code #ff8800}. An empty or unrecognized color resets to the
 * default.</p>
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
     * {@link #interpret(Token[], Sheet)} call.
     */
    public boolean commandExists = true;

    /** The result of the last {@link #interpret(Token[], Sheet)} call. */
    private CommandResult commandResult = CommandResult.NONE;

    /**
     * Returns the result of the last interpretation (e.g. {@link CommandResult#HELP}).
     */
    public CommandResult getCommandResult() {
        return commandResult;
    }

    /**
     * Reads a {@code .json} file into the sheet.
     *
     * @param sheet   the target sheet
     * @param command the lexed command tokens (path in {@code command[1]})
     * @throws Exception if the file cannot be read
     */
    public void readFile(Sheet sheet, Token[] command) throws Exception {
        sheet.readFile(command[1].getFunc());
    }

    /**
     * Writes the sheet to a {@code .json} file.
     *
     * @param sheet   the sheet to save
     * @param command the lexed command tokens (optional path in {@code command[1]})
     * @throws Exception if the file cannot be written
     */
    public void writeFile(Sheet sheet, Token[] command) throws Exception {
        if (command.length == 1) sheet.writeFile();
        else sheet.writeFile(command[1].getFunc());
    }

    public Token[] interpret(Token[] command, Sheet sheet) throws Exception {
        commandExists = true;
        commandResult = CommandResult.NONE;
        switch (command[0].getFunc()) {
            case "h", "help", "?" -> commandResult = CommandResult.HELP;
            case "e" -> readFile(sheet, command);
            case "resCol" -> sheet.getPositions().applyOffset(
                new int[]{xC, (int) command[1].getVal()},
                true
            );
            case "resRow" -> sheet.getPositions().applyOffset(
                new int[]{yC, (int) command[1].getVal()},
                false
            );
            case "purgeDeps" -> sheet.purgeDependencies();
            case "w" -> writeFile(sheet, command);
            case "wq" -> {
                try {
                    writeFile(sheet, command);
                } catch (Exception ignored) {}
                commandResult = CommandResult.QUIT;
            }
            case "q" -> commandResult = CommandResult.QUIT;
            case "cellColor" -> {
                if (command.length == 1) cellColor("", sheet);
                else cellColor(command[1].getFunc(), sheet);
            }
            case "txtColor" -> {
                if (command.length == 1) txtColor("", sheet);
                else txtColor(command[1].getFunc(), sheet);
            }
            case "boldTxt" -> boldTxt(sheet);
            case "italicTxt" -> italicTxt(sheet);
            case "fontSize" -> {
                if (command.length == 1) fontSize(DEFAULT_FONT_SIZE, sheet);
                else {
                    if (command[1].isFunction() ||
                        command[1].getVal() < 4 || command[1].getVal() > 200)
                        throw new Exception("fontSize expects a size between 4 and 200.");
                    fontSize((int) command[1].getVal(), sheet);
                }
            }
            case "zoom" -> {
                // Global view zoom — unlike :resCol/:fontSize, ignores xC/yC.
                if (command.length == 1) sheet.getPositions().setZoom(DEFAULT_ZOOM);
                else {
                    if (command[1].isFunction() ||
                        command[1].getVal() < 25 || command[1].getVal() > 400)
                        throw new Exception("zoom expects a percentage between 25 and 400.");
                    sheet.getPositions().setZoom(command[1].getVal() / 100);
                }
            }
            case "fontWeight" -> {
                if (command.length == 1) fontWeight("normal", sheet);
                else if (!command[1].isFunction()) {
                    if (command[1].getVal() < 100 || command[1].getVal() > 900)
                        throw new Exception("fontWeight expects bold, normal, or a weight between 100 and 900.");
                    fontWeight(String.valueOf((int) command[1].getVal()), sheet);
                }
                else {
                    if (!command[1].getFunc().equals("bold") && !command[1].getFunc().equals("normal"))
                        throw new Exception("fontWeight expects bold, normal, or a weight between 100 and 900.");
                    fontWeight(command[1].getFunc(), sheet);
                }
            }
            default -> {
                commandExists = false;
                throw new Exception("Command \"" + command[0].getFunc() + "\" doesn't exist.");
            }
        }
        return new Token[]{new Token(0)};
    }

    private void cellColor(@NotNull String color, Sheet sheet) {
        Formatting f;
        System.out.println("Executing command 'cellColor'...");
        f = sheet.findFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setCellColor(color);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.findFormatting(xC, yC));
    }

    private void txtColor(@NotNull String color, Sheet sheet) {
        Formatting f;
        System.out.println("Executing command 'txtColor'...");
        f = sheet.findFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setTxtColor(color);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.findFormatting(xC, yC));
    }

    private void boldTxt(@NotNull Sheet sheet) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
            f.setFontWeight("bold");
        } else if (f.getFontWeight().equals("bold")) {
            f.setFontWeight("normal");
            if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        } else f.setFontWeight("bold");
    }

    private void italicTxt(@NotNull Sheet sheet) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
            f.setFontPosture("italic");
        } else if (f.getFontPosture().equals("italic")) {
            f.setFontPosture("regular");
            if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        } else f.setFontPosture("italic");
    }

    private void fontSize(int size, @NotNull Sheet sheet) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        f.setFontSize(size);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
    }

    private void fontWeight(@NotNull String weight, @NotNull Sheet sheet) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        f.setFontWeight(weight);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
    }
}

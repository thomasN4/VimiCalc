package vimicalc.model;

import org.jetbrains.annotations.NotNull;
import vimicalc.view.Formatting;

import java.util.List;

/**
 * Executes colon commands entered in COMMAND mode (e.g. {@code :w}, {@code :q},
 * {@code :cellColor red}).
 *
 * <p>Supported commands (canonical name first, aliases after):</p>
 * <ul>
 *   <li>{@code :help}, {@code :h}, {@code :?} — open the help menu</li>
 *   <li>{@code :edit}, {@code :e <path>} — open/read a {@code .json} file</li>
 *   <li>{@code :write}, {@code :w [path]} — save the current sheet</li>
 *   <li>{@code :writeQuit}, {@code :wq [path]} — save and quit</li>
 *   <li>{@code :quit}, {@code :q} — quit without saving</li>
 *   <li>{@code :resizeColumn}, {@code :resCol <offset>} — resize the current column</li>
 *   <li>{@code :resizeRow}, {@code :resRow <offset>} — resize the current row</li>
 *   <li>{@code :purgeDependencies}, {@code :purgeDeps} — clear all formula dependencies</li>
 *   <li>{@code :cellColor [color]} — set the cell background color</li>
 *   <li>{@code :textColor}, {@code :txtColor [color]} — set the cell text color</li>
 *   <li>{@code :boldText}, {@code :boldTxt} — toggle bold text on the current cell</li>
 *   <li>{@code :italicText}, {@code :italicTxt} — toggle italic text on the current cell</li>
 *   <li>{@code :fontSize [px]} — set the font size (no argument resets to default)</li>
 *   <li>{@code :fontWeight [bold|normal|100-900]} — set the font weight (no argument resets to normal)</li>
 *   <li>{@code :zoom [25-400]} — set the view zoom percentage (no argument resets to 100%)</li>
 *   <li>{@code :gridlines} — toggle cell gridlines on and off</li>
 * </ul>
 *
 * <p>Each command is defined once in {@link CommandRegistry}, which is the source of
 * record for command names, argument counts, and usage strings; the list above is a
 * human-readable summary and {@link #COMMAND_NAMES} /
 * {@link #CANONICAL_COMMAND_NAMES} are derived from the registry.</p>
 *
 * <p>Color arguments accept the built-in names (red, green, blue, white, black,
 * gray, lGray, dGray, vLGray), any CSS color name (e.g. crimson, teal), or a
 * hex value like {@code #ff8800}. An empty or unrecognized color resets to the
 * default.</p>
 */
public class Command {
    /**
     * Every recognized command name and alias, in registration order, derived from
     * {@link CommandRegistry}. Covers everything {@link #execute(Sheet)} accepts.
     */
    public static final List<String> COMMAND_NAMES = CommandRegistry.names();

    /**
     * The canonical command names only (no aliases), in registration order, derived
     * from {@link CommandRegistry}. Drives COMMAND-mode completion suggestions, so
     * the popup offers {@code write} but not its {@code w} alias. Because it is
     * derived, a command cannot exist without also being completable.
     */
    public static final List<String> CANONICAL_COMMAND_NAMES = CommandRegistry.canonicalNames();

    /** Column index of the cell this command is associated with. */
    private final int xC;
    /** Row index of the cell this command is associated with. */
    private final int yC;
    /** The raw command text (without the leading colon). */
    private String txt;

    /**
     * Creates a command from the given text at the specified cell position.
     *
     * @param txt the command text (without the leading colon)
     * @param xC  the column index of the current cell
     * @param yC  the row index of the current cell
     */
    public Command(String txt, int xC, int yC) {
        this.txt = txt;
        this.xC = xC;
        this.yC = yC;
    }

    /**
     * Returns the raw command text.
     *
     * @return the command text
     */
    public String getTxt() {
        return txt;
    }

    /**
     * Sets the raw command text.
     *
     * @param txt the new command text
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * Tokenises the command text and dispatches it against the given sheet.
     *
     * @param sheet the sheet context
     * @return the side-effect signal for the controller ({@link CommandResult#NONE},
     *         {@link CommandResult#HELP}, or {@link CommandResult#QUIT})
     * @throws Exception if the command is unrecognized or its arguments are invalid
     */
    public CommandResult execute(Sheet sheet) throws Exception {
        return execute(Tokenizer.tokenize(txt), sheet);
    }

    /**
     * Reads a {@code .json} file into the sheet.
     *
     * @param sheet   the target sheet
     * @param command the lexed command tokens (path in {@code command[1]})
     * @throws Exception if the file cannot be read
     */
    static void readFile(Sheet sheet, Token[] command) throws Exception {
        sheet.readFile(command[1].getSymbol());
    }

    /**
     * Writes the sheet to a {@code .json} file.
     *
     * @param sheet   the sheet to save
     * @param command the lexed command tokens (optional path in {@code command[1]})
     * @throws Exception if the file cannot be written
     */
    static void writeFile(Sheet sheet, Token[] command) throws Exception {
        if (command.length == 1) sheet.writeFile();
        else sheet.writeFile(command[1].getSymbol());
    }

    private CommandResult execute(Token[] command, Sheet sheet) throws Exception {
        // Bare ":" (or ":" + only spaces) tokenises to nothing — do nothing, like Vim.
        if (command.length == 0) return CommandResult.NONE;

        CommandRegistry.CommandDef def = CommandRegistry.lookup(command[0].getSymbol());
        if (def == null)
            throw new Exception("Command \"" + command[0].getSymbol() + "\" doesn't exist.");

        int argc = command.length - 1;
        if (argc < def.minArgs() || argc > def.maxArgs())
            throw new Exception("Usage: " + def.usage());

        return def.handler().run(command, sheet, xC, yC);
    }

    static void cellColor(@NotNull String color, Sheet sheet, int xC, int yC) {
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

    static void txtColor(@NotNull String color, Sheet sheet, int xC, int yC) {
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

    static void boldTxt(@NotNull Sheet sheet, int xC, int yC) {
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

    static void italicTxt(@NotNull Sheet sheet, int xC, int yC) {
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

    static void fontSize(int size, @NotNull Sheet sheet, int xC, int yC) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        f.setFontSize(size);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
    }

    static void fontWeight(@NotNull String weight, @NotNull Sheet sheet, int xC, int yC) {
        Formatting f = sheet.findFormatting(xC, yC);
        if (f == null) {
            f = new Formatting();
            sheet.addFormatting(xC, yC, f);
        }
        f.setFontWeight(weight);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
    }
}

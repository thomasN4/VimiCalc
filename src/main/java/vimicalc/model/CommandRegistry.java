package vimicalc.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static vimicalc.view.Defaults.DEFAULT_FONT_SIZE;
import static vimicalc.view.Defaults.DEFAULT_MACRO_DELAY_MS;
import static vimicalc.view.Defaults.DEFAULT_ZOOM;
import static vimicalc.view.Defaults.MAX_MACRO_DELAY_MS;

/**
 * The single source of truth for colon commands ({@code :w}, {@code :cellColor red}, …).
 *
 * <p>Each command is described once by a {@link CommandDef}: its canonical name, any
 * aliases, its allowed argument count, a usage string, and the {@link CommandHandler}
 * that runs it. Everything else the app needs about commands is <b>derived</b> from
 * these definitions:</p>
 *
 * <ul>
 *   <li>{@link Command#COMMAND_NAMES} is {@link #names()} (names plus aliases), and
 *       {@link Command#CANONICAL_COMMAND_NAMES} (the COMMAND-mode completion suggestions)
 *       is {@link #canonicalNames()}, so a command cannot exist without also being
 *       completable — the drift that caused issue #57 (a switch case with no completion
 *       entry) is impossible by construction. Aliases resolve but are not suggested,
 *       keeping short forms like {@code :w} out of the completion popup.</li>
 *   <li>{@link Command#execute(Sheet)} dispatches via {@link #lookup(String)} and enforces
 *       arity centrally before invoking the handler.</li>
 * </ul>
 *
 * <p>Registration order matches the historical {@code COMMAND_NAMES} order. Registering a
 * name or alias twice throws immediately at class-load time.</p>
 */
final class CommandRegistry {
    private CommandRegistry() {}

    /** Runs a command; returns the controller side-effect signal (usually {@link CommandResult#NONE}). */
    @FunctionalInterface
    interface CommandHandler {
        /**
         * @param args  the lexed command tokens; {@code args[0]} is the command name,
         *              {@code args[1..]} its arguments
         * @param sheet the sheet context
         * @param xC    the column index of the current cell
         * @param yC    the row index of the current cell
         */
        CommandResult run(Token[] args, Sheet sheet, int xC, int yC) throws Exception;
    }

    /**
     * A command definition.
     *
     * @param name    the canonical command name (e.g. {@code "cellColor"})
     * @param aliases alternate names (e.g. {@code ["h", "?"]} for {@code "help"}); usually short forms
     * @param minArgs the minimum number of arguments <b>after</b> the command name
     * @param maxArgs the maximum number of arguments <b>after</b> the command name
     * @param usage   a usage string, beginning with {@code :} + {@link #name()}
     * @param handler the behavior
     */
    record CommandDef(String name, List<String> aliases, int minArgs, int maxArgs,
                      String usage, CommandHandler handler) {}

    /** Every name and alias mapped to its definition. Iteration order is registration order. */
    private static final LinkedHashMap<String, CommandDef> REGISTRY = new LinkedHashMap<>();
    /** Every name and alias, flattened in registration order — the value of {@link #names()}. */
    private static final List<String> NAMES = new ArrayList<>();
    /** Canonical names only (no aliases), in registration order — the value of {@link #canonicalNames()}. */
    private static final List<String> CANONICAL_NAMES = new ArrayList<>();

    private static void register(CommandDef def) {
        registerName(def.name(), def);
        for (String alias : def.aliases()) registerName(alias, def);
        CANONICAL_NAMES.add(def.name());
    }

    private static void registerName(String name, CommandDef def) {
        if (REGISTRY.putIfAbsent(name, def) != null)
            throw new IllegalStateException("Duplicate command name or alias: " + name);
        NAMES.add(name);
    }

    /** Returns the definition for {@code name} (a canonical name or alias), or {@code null} if none. */
    static CommandDef lookup(String name) {
        return REGISTRY.get(name);
    }

    /** Returns every command name and alias, flattened in registration order. */
    static List<String> names() {
        return List.copyOf(NAMES);
    }

    /** Returns the canonical command names only (no aliases), in registration order. */
    static List<String> canonicalNames() {
        return List.copyOf(CANONICAL_NAMES);
    }

    static {
        register(new CommandDef("help", List.of("h", "?"), 0, 0,
            ":help, :h, :? — open the help menu",
            (args, sheet, xC, yC) -> CommandResult.HELP));
        register(new CommandDef("edit", List.of("e"), 1, 1,
            ":edit, :e <path> — open/read a .json file",
            (args, sheet, xC, yC) -> { Command.readFile(sheet, args); return CommandResult.NONE; }));
        register(new CommandDef("write", List.of("w"), 0, 1,
            ":write, :w [path] — save the current sheet",
            (args, sheet, xC, yC) -> { Command.writeFile(sheet, args); return CommandResult.NONE; }));
        register(new CommandDef("writeQuit", List.of("wq"), 0, 1,
            ":writeQuit, :wq [path] — save and quit",
            (args, sheet, xC, yC) -> {
                try {
                    Command.writeFile(sheet, args);
                } catch (Exception ignored) {}
                return CommandResult.QUIT;
            }));
        register(new CommandDef("quit", List.of("q"), 0, 0,
            ":quit, :q — quit without saving",
            (args, sheet, xC, yC) -> CommandResult.QUIT));
        register(new CommandDef("resizeColumn", List.of("resCol"), 1, 1,
            ":resizeColumn, :resCol <offset> — resize the current column",
            (args, sheet, xC, yC) -> {
                sheet.getPositions().applyOffset(new int[]{xC, (int) args[1].getVal()}, true);
                return CommandResult.NONE;
            }));
        register(new CommandDef("resizeRow", List.of("resRow"), 1, 1,
            ":resizeRow, :resRow <offset> — resize the current row",
            (args, sheet, xC, yC) -> {
                sheet.getPositions().applyOffset(new int[]{yC, (int) args[1].getVal()}, false);
                return CommandResult.NONE;
            }));
        register(new CommandDef("purgeDependencies", List.of("purgeDeps"), 0, 0,
            ":purgeDependencies, :purgeDeps — clear all formula dependencies",
            (args, sheet, xC, yC) -> { sheet.purgeDependencies(); return CommandResult.NONE; }));
        register(new CommandDef("cellColor", List.of(), 0, 1,
            ":cellColor [color] — set the cell background color",
            (args, sheet, xC, yC) -> {
                Command.cellColor(args.length == 1 ? "" : args[1].getSymbol(), sheet, xC, yC);
                return CommandResult.NONE;
            }));
        register(new CommandDef("textColor", List.of("txtColor"), 0, 1,
            ":textColor, :txtColor [color] — set the cell text color",
            (args, sheet, xC, yC) -> {
                Command.txtColor(args.length == 1 ? "" : args[1].getSymbol(), sheet, xC, yC);
                return CommandResult.NONE;
            }));
        register(new CommandDef("boldText", List.of("boldTxt"), 0, 0,
            ":boldText, :boldTxt — toggle bold text on the current cell",
            (args, sheet, xC, yC) -> { Command.boldTxt(sheet, xC, yC); return CommandResult.NONE; }));
        register(new CommandDef("italicText", List.of("italicTxt"), 0, 0,
            ":italicText, :italicTxt — toggle italic text on the current cell",
            (args, sheet, xC, yC) -> { Command.italicTxt(sheet, xC, yC); return CommandResult.NONE; }));
        register(new CommandDef("fontSize", List.of(), 0, 1,
            ":fontSize [px] — set the font size (no argument resets to default)",
            (args, sheet, xC, yC) -> {
                if (args.length == 1) Command.fontSize(DEFAULT_FONT_SIZE, sheet, xC, yC);
                else {
                    if (args[1].isSymbol() || args[1].getVal() < 4 || args[1].getVal() > 200)
                        throw new Exception("fontSize expects a size between 4 and 200.");
                    Command.fontSize((int) args[1].getVal(), sheet, xC, yC);
                }
                return CommandResult.NONE;
            }));
        register(new CommandDef("fontWeight", List.of(), 0, 1,
            ":fontWeight [bold|normal|100-900] — set the font weight (no argument resets to normal)",
            (args, sheet, xC, yC) -> {
                if (args.length == 1) Command.fontWeight("normal", sheet, xC, yC);
                else if (!args[1].isSymbol()) {
                    if (args[1].getVal() < 100 || args[1].getVal() > 900)
                        throw new Exception("fontWeight expects bold, normal, or a weight between 100 and 900.");
                    Command.fontWeight(String.valueOf((int) args[1].getVal()), sheet, xC, yC);
                } else {
                    if (!args[1].getSymbol().equals("bold") && !args[1].getSymbol().equals("normal"))
                        throw new Exception("fontWeight expects bold, normal, or a weight between 100 and 900.");
                    Command.fontWeight(args[1].getSymbol(), sheet, xC, yC);
                }
                return CommandResult.NONE;
            }));
        register(new CommandDef("zoom", List.of(), 0, 1,
            ":zoom [25-400] — set the view zoom percentage (no argument resets to 100%)",
            (args, sheet, xC, yC) -> {
                // Global view zoom — unlike :resizeColumn/:fontSize, ignores xC/yC.
                if (args.length == 1) sheet.getPositions().setZoom(DEFAULT_ZOOM);
                else {
                    if (args[1].isSymbol() || args[1].getVal() < 25 || args[1].getVal() > 400)
                        throw new Exception("zoom expects a percentage between 25 and 400.");
                    sheet.getPositions().setZoom(args[1].getVal() / 100);
                }
                return CommandResult.NONE;
            }));
        register(new CommandDef("gridlines", List.of(), 0, 0,
            ":gridlines — toggle cell gridlines on and off",
            (args, sheet, xC, yC) -> {
                // Global view toggle — like :zoom, ignores xC/yC.
                sheet.getPositions().toggleGridlines();
                return CommandResult.NONE;
            }));
        register(new CommandDef("macroDelay", List.of(), 0, 1,
            ":macroDelay [ms] — set the delay between replayed macro keystrokes (no argument resets to 0 = instant)",
            (args, sheet, xC, yC) -> {
                // Global session setting — like :zoom, ignores xC/yC.
                if (args.length == 1) sheet.getPositions().setMacroDelayMs(DEFAULT_MACRO_DELAY_MS);
                else {
                    if (args[1].isSymbol() || args[1].getVal() < 0 || args[1].getVal() > MAX_MACRO_DELAY_MS)
                        throw new Exception("macroDelay expects a delay between 0 and " + MAX_MACRO_DELAY_MS + " ms.");
                    sheet.getPositions().setMacroDelayMs((int) args[1].getVal());
                }
                return CommandResult.NONE;
            }));
    }
}

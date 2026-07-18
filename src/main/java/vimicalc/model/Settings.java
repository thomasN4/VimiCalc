package vimicalc.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static vimicalc.view.Defaults.DEFAULT_GRIDLINES;
import static vimicalc.view.Defaults.DEFAULT_MACRO_DELAY_MS;
import static vimicalc.view.Defaults.DEFAULT_ZOOM;
import static vimicalc.view.Defaults.MAX_MACRO_DELAY_MS;
import static vimicalc.view.Defaults.MAX_ZOOM;
import static vimicalc.view.Defaults.MIN_ZOOM;

/**
 * Persistent user preferences read once at startup from an optional dot-file
 * (issue #76). JavaFX-free; the only file I/O is {@link #loadFrom(List)},
 * called solely from {@code Main} — everything else is a pure function of its
 * input.
 *
 * <p><b>File location</b> (first existing candidate wins, see
 * {@link #candidatePaths(String, Path)}):</p>
 * <ol>
 *   <li>{@code $XDG_CONFIG_HOME/vimicalc/vimicalcrc}
 *       ({@code ~/.config/vimicalc/vimicalcrc} when the variable is unset)</li>
 *   <li>{@code ~/.vimicalc}</li>
 * </ol>
 * <p>The application never creates either file; absence simply means all
 * defaults.</p>
 *
 * <p><b>Format</b> — vimrc-style directives, one per line:</p>
 * <pre>
 * " VimiCalc configuration
 * set macroDelay 100
 * set gridlines off
 * </pre>
 * <p>{@code "} starts a comment (to end of line); blank lines are ignored.
 * Keys are the canonical camelCase names used by the colon-commands.</p>
 *
 * <p><b>Keys</b>:</p>
 * <table>
 *   <caption>Supported settings</caption>
 *   <tr><th>Key</th><th>Values</th><th>Default</th></tr>
 *   <tr><td>{@code completionNames}</td><td>{@code long} / {@code short}</td><td>{@code long}</td></tr>
 *   <tr><td>{@code macroDelay}</td><td>{@code 0}–{@code 10000} ms</td><td>{@code 0}</td></tr>
 *   <tr><td>{@code gridlines}</td><td>{@code on} / {@code off}</td><td>{@code on}</td></tr>
 *   <tr><td>{@code zoom}</td><td>{@code 0.25}–{@code 4.0}</td><td>{@code 1.0}</td></tr>
 * </table>
 *
 * <p>Note that {@code zoom} is a <em>factor</em>, unlike the {@code :zoom}
 * colon-command which takes a percentage ({@code 25}–{@code 400}).</p>
 *
 * <p><b>Error policy</b>: parsing never throws and never aborts startup.
 * Malformed lines, unknown keys, and invalid or out-of-range values are
 * skipped with a warning (collected in {@link #getWarnings()}), keeping the
 * default for that key — matching the reject-don't-clamp behavior of the
 * corresponding colon-commands. Unknown keys are tolerated so a config
 * written for a newer version doesn't break an older one.</p>
 */
public final class Settings {

    /**
     * Which command-name style completion suggests: full canonical names
     * ({@code LONG}) or short aliases ({@code SHORT}). Parsed and stored for
     * issue #72, which will wire it into command completion; it has no
     * consumer yet.
     */
    public enum CompletionNames { LONG, SHORT }

    /** Completion-name style suggested by command completion. */
    private CompletionNames completionNames = CompletionNames.LONG;
    /** Startup value for {@link vimicalc.view.Positions#setMacroDelayMs(int)}. */
    private int macroDelayMs = DEFAULT_MACRO_DELAY_MS;
    /** Startup value for {@link vimicalc.view.Positions#setGridlines(boolean)}. */
    private boolean gridlines = DEFAULT_GRIDLINES;
    /** Startup value for {@link vimicalc.view.Positions#setZoom(double)}. */
    private double zoom = DEFAULT_ZOOM;
    /** Human-readable warnings accumulated while loading and parsing. */
    private final List<String> warnings = new ArrayList<>();

    private Settings() {} // construct via defaults(), parse(), or loadFrom()

    /** @return settings with every key at its default and no warnings */
    public static Settings defaults() {
        return new Settings();
    }

    /**
     * Parses config-file content into a {@link Settings}. Pure function of its
     * input: no I/O, never throws. Invalid lines are skipped with a warning
     * (see the class Javadoc for the error policy); duplicate keys keep the
     * last occurrence.
     *
     * @param lines the file content, one entry per line
     * @return the parsed settings, with any warnings recorded
     */
    public static Settings parse(List<String> lines) {
        Settings settings = new Settings();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int comment = line.indexOf('"');
            if (comment >= 0) line = line.substring(0, comment);
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            if (tokens.length != 3 || !tokens[0].equals("set")) {
                settings.warnings.add("line " + (i + 1) + ": expected 'set <key> <value>'");
                continue;
            }
            settings.applyDirective(i + 1, tokens[1], tokens[2]);
        }
        return settings;
    }

    /**
     * Applies a single {@code set <key> <value>} directive, recording a
     * warning and keeping the current value when the key or value is invalid.
     *
     * @param lineNo 1-based line number, for warning messages
     * @param key    the setting name (canonical camelCase)
     * @param value  the unparsed value token
     */
    private void applyDirective(int lineNo, String key, String value) {
        switch (key) {
            case "completionNames" -> {
                if (value.equals("long")) completionNames = CompletionNames.LONG;
                else if (value.equals("short")) completionNames = CompletionNames.SHORT;
                else warnings.add("line " + lineNo + ": completionNames expects long or short.");
            }
            case "macroDelay" -> {
                Integer ms = parseInt(value);
                if (ms == null || ms < 0 || ms > MAX_MACRO_DELAY_MS)
                    warnings.add("line " + lineNo + ": macroDelay expects a delay between 0 and "
                                 + MAX_MACRO_DELAY_MS + " ms.");
                else macroDelayMs = ms;
            }
            case "gridlines" -> {
                if (value.equals("on")) gridlines = true;
                else if (value.equals("off")) gridlines = false;
                else warnings.add("line " + lineNo + ": gridlines expects on or off.");
            }
            case "zoom" -> {
                Double factor = parseDouble(value);
                if (factor == null || factor < MIN_ZOOM || factor > MAX_ZOOM)
                    warnings.add("line " + lineNo + ": zoom expects a factor between "
                                 + MIN_ZOOM + " and " + MAX_ZOOM + ".");
                else zoom = factor;
            }
            default -> warnings.add("line " + lineNo + ": unknown key '" + key + "'");
        }
    }

    /** @return the parsed int, or {@code null} if the token is not an integer */
    private static Integer parseInt(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** @return the parsed double, or {@code null} if the token is not a number */
    private static Double parseDouble(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Resolves the candidate config-file paths in priority order. Pure
     * function so path resolution is testable without touching the real
     * environment; {@code Main} supplies the live values.
     *
     * @param xdgConfigHome the {@code XDG_CONFIG_HOME} value; {@code null} or
     *                      blank means unset (falls back to {@code home/.config})
     * @param home          the user's home directory
     * @return {@code [<config dir>/vimicalc/vimicalcrc, home/.vimicalc]}
     */
    public static List<Path> candidatePaths(String xdgConfigHome, Path home) {
        Path configDir = (xdgConfigHome == null || xdgConfigHome.isBlank())
                         ? home.resolve(".config")
                         : Path.of(xdgConfigHome);
        return List.of(configDir.resolve("vimicalc").resolve("vimicalcrc"),
                       home.resolve(".vimicalc"));
    }

    /**
     * Reads and parses the first existing candidate file. The only I/O in
     * this class; called solely from {@code Main}. Never creates a file and
     * never throws: no candidate existing yields silent defaults, and an
     * unreadable file yields defaults plus a warning.
     *
     * @param candidates config-file paths in priority order,
     *                   from {@link #candidatePaths(String, Path)}
     * @return the loaded settings, or defaults when no file exists
     */
    public static Settings loadFrom(List<Path> candidates) {
        for (Path candidate : candidates) {
            if (!Files.exists(candidate)) continue;
            try {
                return parse(Files.readAllLines(candidate));
            } catch (IOException e) {
                Settings settings = new Settings();
                settings.warnings.add("could not read " + candidate + ": " + e.getMessage());
                return settings;
            }
        }
        return new Settings();
    }

    /** @return the completion-name style (stored for issue #72; unconsumed for now) */
    public CompletionNames getCompletionNames() {
        return completionNames;
    }

    /** @return the startup delay between replayed macro keystrokes, in milliseconds */
    public int getMacroDelayMs() {
        return macroDelayMs;
    }

    /** @return whether cell gridlines are drawn at startup */
    public boolean gridlinesOn() {
        return gridlines;
    }

    /** @return the startup view zoom factor */
    public double getZoom() {
        return zoom;
    }

    /** @return the warnings accumulated while loading and parsing, unmodifiable */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}

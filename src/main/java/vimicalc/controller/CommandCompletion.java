package vimicalc.controller;

import org.jetbrains.annotations.NotNull;
import vimicalc.model.Command;

import java.util.List;

/**
 * Prefix-based TAB completion of colon-command names in COMMAND mode
 * (vim {@code wildmode=full} style).
 *
 * <p>A completion session starts on the first {@link #next(String)} or
 * {@link #previous(String)} call: the text typed so far becomes the prefix, and
 * the entries of {@link Command#COMMAND_NAMES} starting with it (sorted
 * alphabetically) become the cycle. Repeated calls cycle forward/backward
 * through the matches, returning to the original prefix after the last match.
 * Any other keystroke should end the session via {@link #reset()}.</p>
 */
public class CommandCompletion {
    /** The text typed before completion started, or {@code null} when no session is active. */
    private String prefix = null;

    /** Command names matching {@link #prefix}, sorted alphabetically. */
    private List<String> matches = List.of();

    /**
     * Position in the cycle: an index into {@link #matches}, or
     * {@code matches.size()} for the original prefix.
     */
    private int position = 0;

    /**
     * Returns whether a completion session is in progress.
     */
    public boolean isActive() {
        return prefix != null;
    }

    /**
     * Returns the command names matching the session's prefix, sorted
     * alphabetically. Empty when no session is active or nothing matches.
     */
    public List<String> getMatches() {
        return matches;
    }

    /**
     * Returns the index into {@link #getMatches()} of the currently selected
     * match, or {@code -1} when the cycle is on the original prefix.
     */
    public int getSelectedIndex() {
        return position == matches.size() ? -1 : position;
    }

    /** Ends the current completion session, if any. */
    public void reset() {
        prefix = null;
        matches = List.of();
        position = 0;
    }

    /**
     * Advances the cycle to the next match, starting a session from
     * {@code current} if none is active.
     *
     * @param current the command text typed so far
     * @return the next match, or the original prefix after the last match;
     *         {@code current} unchanged if nothing matches
     */
    public String next(@NotNull String current) {
        return cycle(current, 1);
    }

    /**
     * Moves the cycle to the previous match, starting a session from
     * {@code current} if none is active.
     *
     * @param current the command text typed so far
     * @return the previous match (the last one on a fresh session), or the
     *         original prefix; {@code current} unchanged if nothing matches
     */
    public String previous(@NotNull String current) {
        return cycle(current, -1);
    }

    private String cycle(String current, int step) {
        if (prefix == null) {
            prefix = current;
            matches = Command.COMMAND_NAMES.stream()
                .filter(name -> name.startsWith(current))
                .sorted()
                .toList();
            position = matches.size();
        }
        // The cycle has matches.size() + 1 slots; the extra one is the prefix.
        position = Math.floorMod(position + step, matches.size() + 1);
        return position == matches.size() ? prefix : matches.get(position);
    }
}

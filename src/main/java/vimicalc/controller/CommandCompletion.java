package vimicalc.controller;

import org.jetbrains.annotations.NotNull;
import vimicalc.model.Command;

import java.util.List;

/**
 * Fuzzy completion of colon-command names in COMMAND mode.
 *
 * <p>Candidates come from {@link Command#CANONICAL_COMMAND_NAMES} — canonical
 * names only, so aliases like {@code w} never clutter the suggestions — and
 * are filtered and ranked by {@link FuzzyMatcher} (prefix, then camelHump,
 * then scattered subsequence; alphabetical within a tier).</p>
 *
 * <p>A session tracks the text the user actually typed plus the ranked
 * matches for it. {@link #update(String)} starts or refreshes the session on
 * every text change, leaving no match selected; {@link #next(String)} /
 * {@link #previous(String)} cycle the selection through the matches, with one
 * extra slot for the original typed text (vim {@code wildmode=full} style),
 * and also start a session on the fly when none is active. The view layer
 * renders {@link #getMatches()} / {@link #getSelectedIndex()} as the
 * completion popup.</p>
 *
 * <p>Note that after a cycle step writes a match into the command line, a
 * subsequent {@link #update(String)} (from further typing or backspace) makes
 * that line content the new typed text — the session has no memory of what
 * the typed text was before the cycle.</p>
 */
public class CommandCompletion {
    /** The text the user typed, or {@code null} when no session is active. */
    private String typed = null;

    /** Command names matching {@link #typed}, ranked by {@link FuzzyMatcher}. */
    private List<String> matches = List.of();

    /**
     * Position in the cycle: an index into {@link #matches}, or
     * {@code matches.size()} for the original typed text.
     */
    private int position = 0;

    /**
     * Returns whether a completion session is in progress.
     */
    public boolean isActive() {
        return typed != null;
    }

    /**
     * Returns the command names matching the session's typed text, best
     * match first. Empty when no session is active or nothing matches.
     */
    public List<String> getMatches() {
        return matches;
    }

    /**
     * Returns the index into {@link #getMatches()} of the currently selected
     * match, or {@code -1} when the cycle is on the original typed text.
     */
    public int getSelectedIndex() {
        return position == matches.size() ? -1 : position;
    }

    /** Ends the current completion session, if any. */
    public void reset() {
        typed = null;
        matches = List.of();
        position = 0;
    }

    /**
     * Starts or refreshes the session for {@code typed}: recomputes the
     * ranked matches and puts the cycle back on the typed-text slot, so no
     * match is selected. Call on every text change while completing a
     * command name.
     *
     * @param typed the command text typed so far
     */
    public void update(@NotNull String typed) {
        this.typed = typed;
        matches = FuzzyMatcher.rank(typed, Command.CANONICAL_COMMAND_NAMES);
        position = matches.size();
    }

    /**
     * Advances the cycle to the next match, starting a session from
     * {@code current} if none is active.
     *
     * @param current the command text typed so far
     * @return the next match, or the original typed text after the last
     *         match; {@code current} unchanged if nothing matches
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
     *         original typed text; {@code current} unchanged if nothing
     *         matches
     */
    public String previous(@NotNull String current) {
        return cycle(current, -1);
    }

    private String cycle(String current, int step) {
        if (typed == null) update(current);
        // The cycle has matches.size() + 1 slots; the extra one is the typed text.
        position = Math.floorMod(position + step, matches.size() + 1);
        return position == matches.size() ? typed : matches.get(position);
    }
}

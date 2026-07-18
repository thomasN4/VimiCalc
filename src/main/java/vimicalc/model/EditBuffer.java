package vimicalc.model;

import org.jetbrains.annotations.NotNull;

/**
 * A line of editable text paired with a caret position, supporting the
 * Emacs-style editing operations used by the COMMAND-mode colon-command
 * buffer (and intended for reuse by cell/formula editing).
 *
 * <p>The caret is an index into the text in {@code 0..text.length()}:
 * {@code 0} places it before the first character, {@code text.length()}
 * after the last. All operations clamp the caret to that range and are
 * pure string/index logic with no JavaFX dependency, so the class is
 * unit-testable headlessly.</p>
 *
 * <p>Word operations ({@link #wordLeft()}, {@link #wordRight()},
 * {@link #killWord()}) treat maximal runs of non-whitespace as words,
 * matching a standard Emacs-style line editor: they first skip any
 * whitespace in the direction of travel, then continue to the word's
 * boundary.</p>
 */
public class EditBuffer {

    /** The buffer text. */
    private String text;
    /** The caret index, always in {@code 0..text.length()}. */
    private int caret;

    /** Creates an empty buffer with the caret at position 0. */
    public EditBuffer() {
        this("");
    }

    /**
     * Creates a buffer holding the given text, caret at the end.
     *
     * @param text the initial text
     */
    public EditBuffer(@NotNull String text) {
        this.text = text;
        this.caret = text.length();
    }

    /**
     * Returns the buffer text.
     *
     * @return the text
     */
    public String text() {
        return text;
    }

    /**
     * Returns the caret index.
     *
     * @return the caret position, in {@code 0..text().length()}
     */
    public int caret() {
        return caret;
    }

    /**
     * Replaces the whole text and moves the caret to the end (used when
     * completion cycling swaps the command line).
     *
     * @param text the new text
     */
    public void setText(@NotNull String text) {
        this.text = text;
        this.caret = text.length();
    }

    /**
     * Inserts the given string at the caret and advances the caret past it.
     *
     * @param s the text to insert
     */
    public void insert(@NotNull String s) {
        text = text.substring(0, caret) + s + text.substring(caret);
        caret += s.length();
    }

    /** Deletes the character before the caret. No-op with the caret at 0. */
    public void backspace() {
        if (caret == 0) return;
        text = text.substring(0, caret - 1) + text.substring(caret);
        caret--;
    }

    /** Deletes the character at the caret (forward-delete). No-op at the end. */
    public void deleteAtCaret() {
        if (caret == text.length()) return;
        text = text.substring(0, caret) + text.substring(caret + 1);
    }

    /** Deletes from the caret to the end of the text. */
    public void killToEnd() {
        text = text.substring(0, caret);
    }

    /**
     * Deletes from the caret through the end of the next word: leading
     * whitespace and the following run of non-whitespace are removed.
     */
    public void killWord() {
        text = text.substring(0, caret) + text.substring(nextWordBoundary());
    }

    /** Moves the caret one character left, clamped to the start. */
    public void left() {
        if (caret > 0) caret--;
    }

    /** Moves the caret one character right, clamped to the end. */
    public void right() {
        if (caret < text.length()) caret++;
    }

    /** Moves the caret to the start of the text. */
    public void home() {
        caret = 0;
    }

    /** Moves the caret to the end of the text. */
    public void end() {
        caret = text.length();
    }

    /** Moves the caret to the start of the previous word. */
    public void wordLeft() {
        caret = previousWordBoundary();
    }

    /** Moves the caret to the end of the next word. */
    public void wordRight() {
        caret = nextWordBoundary();
    }

    /**
     * Returns the index just past the next word: from the caret, skips
     * whitespace, then the following run of non-whitespace.
     */
    private int nextWordBoundary() {
        int i = caret;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
        while (i < text.length() && !Character.isWhitespace(text.charAt(i))) i++;
        return i;
    }

    /**
     * Returns the index of the start of the previous word: from the caret,
     * skips whitespace backwards, then the preceding run of non-whitespace.
     */
    private int previousWordBoundary() {
        int i = caret;
        while (i > 0 && Character.isWhitespace(text.charAt(i - 1))) i--;
        while (i > 0 && !Character.isWhitespace(text.charAt(i - 1))) i--;
        return i;
    }
}

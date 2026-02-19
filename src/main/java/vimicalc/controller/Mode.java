package vimicalc.controller;

/**
 * The editing modes available in VimiCalc, inspired by Vim's modal editing.
 *
 * <p>Each mode determines how keyboard input is interpreted by the
 * {@link Controller}.</p>
 */
public enum Mode {
    /** Command-line mode, entered with {@code :}. Used for file I/O, formatting, and quitting. */
    COMMAND,
    /** Formula entry mode, entered with {@code =}. Accepts RPN (Reverse Polish Notation) expressions. */
    FORMULA,
    /** Help overlay mode, displaying built-in usage instructions. */
    HELP,
    /** Text insertion mode, entered with {@code i} or {@code a}. Characters are typed directly into the cell. */
    INSERT,
    /** Default mode for navigation and issuing key commands (hjkl movement, macros, etc.). */
    NORMAL,
    /** Visual selection mode, entered with {@code v}. Allows selecting cell ranges for bulk operations. */
    VISUAL
}
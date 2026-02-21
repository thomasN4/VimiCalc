package vimicalc.model;

/**
 * Signals a side effect that the controller should handle after
 * a {@link Command} has been interpreted.
 */
public enum CommandResult {
    /** No special action needed. */
    NONE,
    /** The controller should switch to HELP mode. */
    HELP
}

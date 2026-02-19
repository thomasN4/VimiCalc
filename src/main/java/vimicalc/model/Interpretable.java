package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import static vimicalc.utils.Conversions.isNumber;

/**
 * Abstract base class for expressions that can be interpreted against a
 * {@link Sheet}. Subclassed by {@link Formula} (RPN math expressions) and
 * {@link Command} (colon commands like {@code :w}, {@code :q}).
 *
 * <p>Provides a shared {@link #lexer(String)} that tokenises the expression
 * text into {@link Lexeme} arrays, and a template-method
 * {@link #interpret(Sheet)} that lexes and evaluates in one step.</p>
 */
abstract class Interpretable implements Serializable {
    /** Column index of the cell this expression is associated with. */
    protected int xC;
    /** Row index of the cell this expression is associated with. */
    protected int yC;
    /** The raw text of the expression. */
    protected String txt;

    protected Interpretable(String txt, int xC, int yC) {
        this.txt = txt;
        this.xC = xC;
        this.yC = yC;
    }

    /**
     * Tokenises the expression text into an array of {@link Lexeme}s.
     * Splits on spaces; numeric tokens become value lexemes, everything
     * else becomes function/identifier lexemes. Parentheses are stripped.
     *
     * @param txt the expression text to tokenise
     * @return the array of lexemes
     */
    protected Lexeme[] lexer(@NotNull String txt) {
        Lexeme[] argsLong = new Lexeme[txt.length()];
        txt += ' ';
        String arg = "";

        int argsLength = 0;
        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '(' || txt.charAt(i) == ')') continue;
            if (txt.charAt(i) == ' ') {
                if (isNumber(arg))
                    argsLong[argsLength++] = new Lexeme(Double.parseDouble(arg));
                else argsLong[argsLength++] = new Lexeme(arg);
                arg = "";
                continue;
            }
            arg += txt.charAt(i);
        }

        Lexeme[] args = new Lexeme[argsLength];
        System.arraycopy(argsLong, 0, args, 0, argsLength);

        return args;
    }

    /**
     * Returns the raw expression text.
     *
     * @return the expression text
     */
    public String getTxt() {
        return txt;
    }

    /**
     * Sets the raw expression text.
     *
     * @param txt the new expression text
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * Lexes the expression text and evaluates it, returning the final numeric result.
     * For {@link Formula}, returns the computed formula value. For {@link Command},
     * the return value is always 0 (meaningless) since commands operate via side effects.
     *
     * @param sheet the sheet context (for cell lookups and dependency tracking)
     * @return the computed value (meaningful for formulas only)
     * @throws Exception if evaluation fails (e.g. missing args, circular dependency)
     */
    public double interpret(Sheet sheet) throws Exception {
        return interpret(lexer(txt), sheet)[0].getVal();
    }

    /**
     * Evaluates a pre-lexed array of tokens against the given sheet.
     * Implemented by {@link Formula} (RPN evaluation) and {@link Command}
     * (command dispatch).
     *
     * @param args  the lexeme array to evaluate
     * @param sheet the sheet context
     * @return the resulting lexeme array (typically a single-element result)
     * @throws Exception if evaluation fails
     */
    public abstract Lexeme[] interpret(Lexeme[] args, Sheet sheet) throws Exception;
}

/**
 * A token produced by the {@link Interpretable#lexer(String)}.
 *
 * <p>A lexeme is either a <b>value</b> (a numeric literal) or a <b>function</b>
 * (an operator, cell reference, or named function like "sum", "det", etc.).
 * The {@code isFunction} flag determines which field ({@code val} or {@code func})
 * holds the meaningful data.</p>
 */
class Lexeme {
    /** The function/identifier name (empty string for value lexemes). */
    String func;
    /** The numeric value (only meaningful when {@code isFunction} is false). */
    double val;
    /** {@code true} if this lexeme represents a function/identifier rather than a numeric value. */
    boolean isFunction;

    Lexeme(String func) {
        this.func = func;
        isFunction = true;
    }

    Lexeme(double val) {
        this.val = val;
        isFunction = false;
        func = "";
    }

    String getFunc() {
        return func;
    }

    double getVal() {
        return val;
    }

    boolean isFunction() {
        return isFunction;
    }

    void setVal(double val) {
        this.val = val;
        isFunction = false;
    }
}

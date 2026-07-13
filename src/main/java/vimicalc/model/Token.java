package vimicalc.model;

/**
 * A token produced by {@link Tokenizer#tokenize(String)}.
 *
 * <p>A token is either a <b>literal</b> (a numeric value) or a <b>symbol</b>
 * (an operator, cell reference, range, named function, path, etc.).
 * The {@link Type} determines which field ({@code val} or {@code symbol})
 * holds the meaningful data.</p>
 *
 * <p>Tokens may be mutated during evaluation ({@link #setVal(double)}) when a
 * formula reduces a symbol in place to a numeric result.</p>
 */
class Token {
    /**
     * Classification of a token: a numeric {@link #LITERAL} or a non-numeric
     * {@link #SYMBOL} (operator, cell reference, range, named function, path, etc.).
     */
    enum Type {
        /** A numeric value. */
        LITERAL,
        /** Any non-numeric token (operators, refs, command names, paths, …). */
        SYMBOL
    }

    /** The symbol text (empty string for literal tokens). */
    private String symbol;
    /** The numeric value (only meaningful when type is {@link Type#LITERAL}). */
    private double val;
    /** Whether this token is a literal or a symbol. */
    private Type type;

    Token(String symbol) {
        this.symbol = symbol;
        this.type = Type.SYMBOL;
    }

    Token(double val) {
        this.val = val;
        this.type = Type.LITERAL;
        this.symbol = "";
    }

    String getSymbol() {
        return symbol;
    }

    double getVal() {
        return val;
    }

    boolean isSymbol() {
        return type == Type.SYMBOL;
    }

    boolean isLiteral() {
        return type == Type.LITERAL;
    }

    void setVal(double val) {
        this.val = val;
        this.type = Type.LITERAL;
    }
}

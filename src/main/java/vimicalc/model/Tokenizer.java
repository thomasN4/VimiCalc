package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.isNumber;

/**
 * Tokenises expression and command text into {@link Token} arrays.
 *
 * <p>Splits on spaces; numeric tokens become {@linkplain Token.Type#LITERAL
 * literals}, everything else becomes {@linkplain Token.Type#SYMBOL symbols}.
 * Parentheses are stripped. Runs of spaces produce no tokens; empty input
 * yields an empty array.</p>
 */
final class Tokenizer {
    private Tokenizer() {}

    /**
     * Tokenises the given text into an array of {@link Token}s.
     *
     * @param txt the expression or command text to tokenise
     * @return the array of tokens
     */
    static Token[] tokenize(@NotNull String txt) {
        Token[] argsLong = new Token[txt.length()];
        txt += ' ';
        String arg = "";

        int argsLength = 0;
        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '(' || txt.charAt(i) == ')') continue;
            if (txt.charAt(i) == ' ') {
                if (!arg.isEmpty()) {
                    if (isNumber(arg))
                        argsLong[argsLength++] = new Token(Double.parseDouble(arg));
                    else
                        argsLong[argsLength++] = new Token(arg);
                    arg = "";
                }
                continue;
            }
            arg += txt.charAt(i);
        }

        Token[] args = new Token[argsLength];
        System.arraycopy(argsLong, 0, args, 0, argsLength);

        return args;
    }
}

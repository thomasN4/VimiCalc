package vimicalc.controller;

public class KeyCommand {

    // Les Afuncs sont les fonctions avec des arguments et les Bfuncs, non
    private final char[] Afuncs = {'d', 'y', 'p'};
    private final char[] Bfuncs = {'h', 'j', 'k', 'l', 'a', 'i'};
    private String expr;

    public KeyCommand() {
        expr = "";
    }

    public void addChar(String c) {
        expr += c;
        verifyExprCompleteness(c.charAt(0));
    }

    public void verifyExprCompleteness(char c) {
        for (char f : Bfuncs)
            if (c == f) evaluate();
        if (expr.charAt(0) == expr.charAt(1)) evaluate();
    }

    public void evaluate() {
        ;
    }
}
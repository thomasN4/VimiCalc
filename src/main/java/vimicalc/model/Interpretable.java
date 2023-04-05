package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import static vimicalc.utils.Conversions.isNumber;

abstract class Interpretable {
    protected int sCX;  // Coordonnée x de la cellule associée
    protected int sCY;
    protected String txt;

    protected Interpretable(String txt, int sCX, int sCY) {
        this.txt = txt;
        this.sCX = sCX;
        this.sCY = sCY;
    }

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

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public double interpret(Sheet sheet) {
        return interpret(lexer(txt), sheet)[0].getVal();
    }

    public abstract Lexeme[] interpret(Lexeme[] args, Sheet sheet);
}

class Lexeme {
    String func;
    double val;
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

    void setFunc(String func) {
        this.func = func;
    }

    void setVal(double val) {
        this.val = val;
        isFunction = false;
    }
}

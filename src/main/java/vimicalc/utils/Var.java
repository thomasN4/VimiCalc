package vimicalc.utils;

public class Var {
    private char chr = 0;
    private String str;
    private Double dbl;
    private boolean bool;

    public Var(char chr) {
        this.chr = chr;
    }

    public Var(String str) {
        this.str = str;
    }

    public Var(double dbl) {
        this.str = str;
    }

    public Var(boolean bool) {
        this.bool = bool;
    }

    public Object getVal() {
        if (chr != 0)
            return chr;
        else if (str != null)
            return str;
        else if (dbl != null)
            return dbl;
        else
            return bool;
    }
}

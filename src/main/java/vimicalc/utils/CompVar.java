package vimicalc.utils;

// Ça ne sert vraiment à rien, et ça ne servira probablement jamais à rien.
public class CompVar {
    private char chr = 0;
    private String str;
    private Double dbl;
    private boolean bool;

    public CompVar(char chr) {
        this.chr = chr;
    }

    public CompVar(String str) {
        this.str = str;
    }

    public CompVar(double dbl) {
        this.str = str;
    }

    public CompVar(boolean bool) {
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

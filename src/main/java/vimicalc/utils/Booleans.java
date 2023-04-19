package vimicalc.utils;

public class Booleans {
    public static boolean intersect(Var[] funcsA, Var[] funcsB) {
        for (Var b : funcsB)
            for (Var a : funcsA)
                if (a == b) return true;
        return false;
    }
}

package vimicalc.utils;

public class Booleans {
    public static boolean intersect(char[] funcsA, char[] funcsB) {
        for (Object b : funcsB)
            for (Object a : funcsA)
                if (a == b) return true;
        return false;
    }
}

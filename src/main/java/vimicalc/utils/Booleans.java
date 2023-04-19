package vimicalc.utils;

public class Booleans {
    public static boolean intersect(CompVar[] funcsA, CompVar[] funcsB) {
        for (CompVar b : funcsB)
            for (CompVar a : funcsA)
                if (a.getVal().equals(b.getVal())) return true;
        return false;
    }
}

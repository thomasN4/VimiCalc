package vimicalc.utils;

public class Booleans {

    // C'est vraiment inutile, au moins pour l'instant, mais je n'ai pas envie de l'enlever.
    public static boolean intersect(CompVar[] funcsA, CompVar[] funcsB) {
        for (CompVar b : funcsB)
            for (CompVar a : funcsA)
                if (a.getVal().equals(b.getVal())) return true;
        return false;
    }
}

package vimicalc.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.pow;

public class Conversions {
    @Contract(pure = true)
    public static @NotNull String toAlpha(int num) {
        int rem = num % 26;
        if (num > 25) return toAlpha((num-rem-1)/26) + (char)(rem+65);
        else return ""+(char)(num+65);
    }

    public static int fromAlpha(@NotNull String alpha) {
        int num = 0;
        for (int i = 0; i < alpha.length(); i++) {
            num += (alpha.charAt(i)-64)*pow(26, alpha.length()-i-1);
        }
        return num;
    }

    public static boolean isNumber(String s) {
        boolean b = true;
        try {
            Double.parseDouble(s);
        } catch (Exception ignored) {
            b = false;
        }
        return b;
    }
}

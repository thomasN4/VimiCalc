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

    public static int[] coordsStrToInts(@NotNull String coords) {
        StringBuilder coordX = new StringBuilder(),
                coordY = new StringBuilder();

        for (int i = 0; i < coords.length(); i++) {
            if (coords.charAt(i) > 64)
                coordX.append(coords.charAt(i));
            else coordY.append(coords.charAt(i));
        }

        int xCoord, yCoord;
        try {
            xCoord = fromAlpha(coordX.toString());
            yCoord = Integer.parseInt(coordY.toString());
        } catch (Exception ignored) {
            xCoord = 0;
            yCoord = 0;
        }

        return new int[]{xCoord, yCoord};
    }

    public static String coordsIntsToStr(int xC, int yC) {
        return toAlpha(xC-1) + yC;
    }

    public static String relToAbsCoords(@NotNull String relC, int xRef, int yRef) {
        String absC = "";
        StringBuilder deltaStr = new StringBuilder();
        int absX = 0, absY = 0, deltaX = 1, deltaY = 1, pos = 0;
        if (isNumber(""+relC.charAt(0))) {
            do {
                deltaStr.append(relC.charAt(pos++));
            } while (isNumber(""+relC.charAt(pos)));
            if (relC.charAt(pos) == 'h' || relC.charAt(pos) == 'l')
                deltaX = Integer.parseInt(deltaStr.toString());
            else deltaY = Integer.parseInt(deltaStr.toString());
        }
        switch (Character.toLowerCase(relC.charAt(pos))) {
            case 'h' -> {
                absX = xRef - deltaX;
                absC = toAlpha(absX-1) + yRef;
            }
            case 'j' -> {
                absY = yRef + deltaY;
                absC = toAlpha(xRef-1) + absY;
            }
            case 'k' -> {
                absY = yRef - deltaY;
                absC = toAlpha(xRef-1) + absY;
            }
            case 'l' -> {
                absX = xRef + deltaX;
                absC = toAlpha(absX-1) + yRef;
            }
        }
        if (pos == relC.length()-1)
            return absC;
        else return relToAbsCoords(
                relC.substring(pos+1),
                (absX == 0) ? xRef : absX,
                (absY == 0) ? yRef : absY
            );
    }
}

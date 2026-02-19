package vimicalc.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.pow;

/**
 * Utility class for converting between spreadsheet coordinate formats.
 *
 * <p>Provides conversions between numeric column indices and alphabetic
 * column labels (A, B, ... Z, AA, AB, ...), cell reference strings
 * (e.g. "B3"), and relative Vim-style coordinates (e.g. "2j3l").</p>
 */
public class Conversions {
    /**
     * Converts a zero-based column index to an alphabetic column label.
     * <p>Examples: 0 → "A", 25 → "Z", 26 → "AA".</p>
     *
     * @param num the zero-based column index
     * @return the corresponding alphabetic label
     */
    @Contract(pure = true)
    public static @NotNull String toAlpha(int num) {
        int rem = num % 26;
        if (num > 25) return toAlpha((num-rem-1)/26) + (char)(rem+65);
        else return ""+(char)(num+65);
    }

    /**
     * Converts an alphabetic column label to a one-based column index.
     * <p>Examples: "A" → 1, "Z" → 26, "AA" → 27.</p>
     *
     * @param alpha the alphabetic column label (uppercase)
     * @return the corresponding one-based column index
     */
    public static int fromAlpha(@NotNull String alpha) {
        int num = 0;
        for (int i = 0; i < alpha.length(); i++) {
            num += (alpha.charAt(i)-64)*pow(26, alpha.length()-i-1);
        }
        return num;
    }

    /**
     * Checks whether a string can be parsed as a {@code double}.
     *
     * @param s the string to test
     * @return {@code true} if {@code s} is a valid numeric literal
     */
    public static boolean isNumber(String s) {
        boolean b = true;
        try {
            Double.parseDouble(s);
        } catch (Exception ignored) {
            b = false;
        }
        return b;
    }

    /**
     * Parses a cell reference string (e.g. "B3") into a two-element array
     * {@code [column, row]}, where column is one-based.
     *
     * @param coords the cell reference string (letters for column, digits for row)
     * @return {@code int[]{column, row}}, or {@code {0, 0}} if parsing fails
     */
    public static int[] coordsStrToInts(@NotNull String coords) {
        StringBuilder coordX = new StringBuilder(),
                coordY = new StringBuilder();

        for (int i = 0; i < coords.length(); i++) {
            if (Character.isAlphabetic(coords.charAt(i)))
                coordX.append(Character.toUpperCase(coords.charAt(i)));
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

    /**
     * Converts one-based column and row indices to a cell reference string.
     * <p>Example: (1, 3) → "A3".</p>
     *
     * @param xC the one-based column index
     * @param yC the row number
     * @return the cell reference string
     */
    public static String coordsIntsToStr(int xC, int yC) {
        return toAlpha(xC-1) + yC;
    }

    /**
     * Converts a Vim-style relative coordinate string to an absolute cell reference.
     *
     * <p>Relative coordinates use h/j/k/l directions with optional numeric prefixes.
     * For example, {@code "2j3l"} from reference (1, 1) means "2 down, 3 right",
     * yielding "D3". Recursively processes chained movements.</p>
     *
     * @param relC  the relative coordinate string (e.g. "2j", "3l", "2j3l")
     * @param xRef  the one-based column of the reference cell
     * @param yRef  the row of the reference cell
     * @return the absolute cell reference string (e.g. "D3")
     */
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

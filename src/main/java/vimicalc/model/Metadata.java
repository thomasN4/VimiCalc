package vimicalc.model;

import java.util.Arrays;
import java.util.HashMap;

public class Metadata {
    private int camAbsX, camAbsY, firstXC, lastXC, firstYC, lastYC;
    private final int picW, picH, DCW, DCH;
    private final HashMap<Integer, Integer> xOffsets;
    private final HashMap<Integer, Integer> yOffsets;
    private int[] cellAbsXs, cellAbsYs;

    public Metadata(int camAbsX, int camAbsY,
                    HashMap<Integer, Integer> xOffsets,
                    HashMap<Integer, Integer> yOffsets,
                    int picW, int picH, int DCW, int DCH) {
        this.camAbsX = camAbsX;
        this.camAbsY = camAbsY;
        this.xOffsets = xOffsets;
        this.yOffsets = yOffsets;
        this.picW = picW;
        this.picH = picH;
        this.DCW = DCW;
        this.DCH = DCH;
    }

    public void generate(int camAbsX, int camAbsY) {
        int[] cellAbsXsLong = new int[picW], cellAbsYsLong = new int[picH];
        int currAbsX = DCW, currAbsY = DCH, xC = 1, yC = 1;
        boolean firstXCFound = false, firstYCFound = false,
                lastXCFound = false, lastYCFound = false;
        Integer xOffset, yOffset;

        do {
            if (currAbsX > camAbsX && !firstXCFound) {
                firstXC = xC - 1;
                firstXCFound = true;
            }
            cellAbsXsLong[xC] = currAbsX;
            xOffset = xOffsets.get(xC);
            currAbsX += DCW + ((xOffset == null) ? 0 : xOffset);
            if (currAbsX >= camAbsX + picW && !lastXCFound) {
                lastXC = xC;
                lastXCFound = true;
            }
            xC++;
        } while (currAbsX <= camAbsX + picW + DCW + ((xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC)));
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        do {
            if (currAbsY > camAbsY && !firstYCFound) {
                firstYC = yC - 1;
                firstYCFound = true;
            }
            cellAbsYsLong[yC] = currAbsY;
            yOffset = yOffsets.get(yC);
            currAbsY += DCH + ((yOffset == null) ? 0 : yOffset);
            if (currAbsY >= camAbsY + picH && !lastYCFound) {
                lastYC = yC;
                lastYCFound = true;
            }
            yC++;
        } while (currAbsY <= camAbsY + picH + DCH + ((yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC)));
        System.out.println("firstYC = " + firstYC);
        System.out.println("lastYC = " + lastYC);

        cellAbsXs = new int[xC];
        System.arraycopy(cellAbsXsLong, 0, cellAbsXs, 0, xC);
        cellAbsYs = new int[yC];
        System.arraycopy(cellAbsYsLong, 0, cellAbsYs, 0, yC);

        System.out.println("CellAbsXs: {");
        System.out.println("\t" + Arrays.toString(cellAbsXs));
        System.out.println('}');
        System.out.println("CellAbsYs: {");
        System.out.println("\t" + Arrays.toString(cellAbsYs));
        System.out.println('}');

        this.camAbsX = camAbsX;
        this.camAbsY = camAbsY;
    }

    // newOffset = {coord (X ou Y, conditionnel sur isXAxis), offset}
    public void generate(int[] newOffset, boolean isXAxis) {
        if (isXAxis)
            xOffsets.put(newOffset[0], newOffset[1]);
        else
            yOffsets.put(newOffset[0], newOffset[1]);
        generate(camAbsX, camAbsY);
    }

    public int getFirstXC() {
        return firstXC;
    }

    public int getLastXC() {
        return lastXC;
    }

    public int getFirstYC() {
        return firstYC;
    }

    public int getLastYC() {
        return lastYC;
    }

    public int[] getCellAbsXs() {
        return cellAbsXs;
    }

    public int[] getCellAbsYs() {
        return cellAbsYs;
    }

    public int getCamAbsX() {
        return camAbsX;
    }

    public int getCamAbsY() {
        return camAbsY;
    }
}

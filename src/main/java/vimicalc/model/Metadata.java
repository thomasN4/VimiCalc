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
        this.camAbsX = camAbsX;
        this.camAbsY = camAbsY;
        int[] cellAbsXsLong = new int[picW], cellAbsYsLong = new int[picH];
        int currAbsX = 0, currAbsY = 0, xC, yC;
        boolean firstXCFound = false, firstYCFound = false;
        Integer xOffset, yOffset;

        for (xC = 1; currAbsX < camAbsX + picW; xC++) {
            if (currAbsX > camAbsX && !firstXCFound) {
                firstXC = xC - 1;
                firstXCFound = true;
            }
            xOffset = xOffsets.get(xC);
            currAbsX += DCW + ((xOffset == null) ? 0 : xOffset);
            cellAbsXsLong[xC] = currAbsX;
        }
        lastXC = xC - 1;
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        for (yC = 1; currAbsY < camAbsY + picH; yC++) {
            if (currAbsY > camAbsY && !firstYCFound) {
                firstYC = yC-1;
                firstYCFound = true;
            }
            yOffset = yOffsets.get(yC);
            currAbsY += DCH + ((yOffset == null) ? 0 : yOffset);
            cellAbsYsLong[yC] = currAbsY;
        }
        lastYC = yC - 1;
        System.out.println("firstYC = " + firstYC);
        System.out.println("lastYC = " + lastYC);

        cellAbsXs = new int[lastXC];
        System.arraycopy(cellAbsXsLong, 0, cellAbsXs, 0, lastXC);
        cellAbsYs = new int[lastYC];
        System.arraycopy(cellAbsYsLong, 0, cellAbsYs, 0, lastYC);

        System.out.println("CellAbsXs: {");
        System.out.println("\t" + Arrays.toString(cellAbsXs));
        System.out.println('}');
        System.out.println("CellAbsYs: {");
        System.out.println("\t" + Arrays.toString(cellAbsYs));
        System.out.println('}');
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

    public int getPicW() {
        return picW;
    }

    public int getPicH() {
        return picH;
    }
}

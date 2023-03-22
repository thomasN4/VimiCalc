package vimicalc.model;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

        cellAbsXs = new int[picW];
        int currCellWidth;
        int maxAbsX = 0;
        AtomicInteger xOffset = new AtomicInteger();  // idk Java refuse que j'utilise un simple int
        int xC;
        boolean firstXCFound = false;
        for (xC = 1; maxAbsX < camAbsX + picW; xC++) {
            xOffset.set(0);
            int finalXC = xC;
            xOffsets.forEach((k, v) -> {
                if (k == finalXC)
                    xOffset.set(v);
            });
            currCellWidth = DCW + xOffset.get();
            maxAbsX += currCellWidth;
            cellAbsXs[xC] = maxAbsX;
            if (maxAbsX >= camAbsX && !firstXCFound) {
                firstXC = xC;
                firstXCFound = true;
            }
        }
        lastXC = xC;

        cellAbsYs = new int[picH];
        int currCellHeigth;
        int maxAbsY = 0;
        AtomicInteger yOffset = new AtomicInteger();
        int yC;
        boolean firstYCFound = false;
        for (yC = 1; maxAbsY < camAbsY + picH; yC++) {
            yOffset.set(0);
            int finalYC = yC;
            yOffsets.forEach((k, v) -> {
                if (k == finalYC)
                    yOffset.set(v);
            });
            currCellHeigth = DCH + yOffset.get();
            maxAbsY += currCellHeigth;
            cellAbsYs[yC] = maxAbsY;
            if (maxAbsY >= camAbsY && !firstYCFound) {
                firstYC = yC;
                firstYCFound = true;
            }
        }
        lastYC = yC;
    }

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

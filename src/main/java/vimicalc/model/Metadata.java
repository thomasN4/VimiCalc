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
        cellAbsYs = new int[picH];
        int currAbsX = 0, currAbsY = 0, xC, yC;
        AtomicInteger xOffset = new AtomicInteger(),
                      yOffset = new AtomicInteger();  // idk Java refuse que j'utilise un simple int
        boolean firstXCFound = false, firstYCFound = false;

        for (xC = 1; currAbsX <= camAbsX + picW; xC++) {
            xOffset.set(0);
            int finalXC = xC;
            xOffsets.forEach((k, v) -> {
                if (k == finalXC)
                    xOffset.set(v);
            });
            if (currAbsX > camAbsX && !firstXCFound) {
                firstXC = xC-1;
                firstXCFound = true;
            }
            currAbsX += DCW + xOffset.get();
            cellAbsXs[xC] = currAbsX;
        }
        lastXC = xC - 1;
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        for (yC = 1; currAbsY <= camAbsY + picH; yC++) {
            yOffset.set(0);
            int finalYC = yC;
            yOffsets.forEach((k, v) -> {
                if (k == finalYC)
                    yOffset.set(v);
            });
            if (currAbsY > camAbsY && !firstYCFound) {
                firstYC = yC-1;
                firstYCFound = true;
            }
            currAbsY += DCH + yOffset.get();
            cellAbsYs[yC] = currAbsY;
        }
        lastYC = yC - 1;
        System.out.println("firstYC = " + firstYC);
        System.out.println("lastYC = " + lastYC);
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

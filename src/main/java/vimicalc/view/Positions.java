package vimicalc.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Positions {
    private int camAbsX, camAbsY, firstXC, lastXC, firstYC, lastYC, maxXC, maxYC;
    private final int picW, picH, DCW, DCH;
    private final HashMap<Integer, Integer> xOffsets;
    private final HashMap<Integer, Integer> yOffsets;
    private int[] cellAbsXs, cellAbsYs;

    public Positions(int camAbsX, int camAbsY, int picW, int picH, int DCW, int DCH,
                     HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets) {
        this.camAbsX = camAbsX;
        this.camAbsY = camAbsY;
        this.xOffsets = xOffsets;
        this.yOffsets = yOffsets;
        this.picW = picW;
        this.picH = picH;
        this.DCW = DCW;
        this.DCH = DCH;
        maxXC = 0;
        maxYC = 0;
    }

    private int xOuterEdge(int xInnerEdge, int xC) {
        return xInnerEdge + picW + 2 * DCW +
               ((xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC)) +
               ((xOffsets.get(xC + 1) == null) ? 0 : xOffsets.get(xC + 1));
    }
    private int yOuterEdge(int yInnerEdge, int yC) {
        return yInnerEdge + picH + 2 * DCH +
               ((yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC)) +
               ((yOffsets.get(yC + 1) == null) ? 0 : yOffsets.get(yC + 1));
    }

    // Les positions sont calculées à partir de l'origine, à chaque fois que la caméra bouge.
    // Ça fonctionne, mais absolute yikes.
    public void generate(int xInnerEdge, int yInnerEdge) {
        System.out.println("Generating metadata...");
        ArrayList<Integer> cellAbsXsLong = new ArrayList<>(), cellAbsYsLong = new ArrayList<>();
        cellAbsXsLong.add(0); cellAbsYsLong.add(0);
        int currAbsX = DCW/2, currAbsY = DCH, xC = 1, yC = 1;
        boolean firstXCFound = false, lastXCFound = false,
                firstYCFound = false, lastYCFound = false;
        int xOffset, yOffset;

        do {
            xOffset = (xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC);
            if (!firstXCFound && currAbsX > xInnerEdge) {
                firstXC = xC - 1;
                firstXCFound = true;
            }
            cellAbsXsLong.add(currAbsX);
            currAbsX += DCW + xOffset;
            if (currAbsX > xInnerEdge + picW && !lastXCFound) {
                System.out.println(currAbsX + " " + xInnerEdge + " " + picW + " " + DCW);
                lastXC = xC;
                lastXCFound = true;
            }
            xC++;
        } while (!lastXCFound || currAbsX <= xOuterEdge(xInnerEdge, lastXC) || xC <= maxXC + 2);
        cellAbsXsLong.add(currAbsX);
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        do {
            yOffset = (yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC);
            if (!firstYCFound && currAbsY > yInnerEdge) {
                firstYC = yC - 1;
                firstYCFound = true;
            }
            cellAbsYsLong.add(currAbsY);
            currAbsY += DCH + yOffset;
            if (currAbsY > yInnerEdge + picH && !lastYCFound) {
                System.out.println(currAbsY + " " + yInnerEdge + " " + picH + " " + DCH);
                lastYC = yC;
                lastYCFound = true;
            }
            yC++;
        } while (!lastYCFound || currAbsY <= yOuterEdge(yInnerEdge, lastYC) || yC <= maxYC + 2);
        cellAbsYsLong.add(currAbsY);
        System.out.println("firstYC = " + firstYC);
        System.out.println("lastYC = " + lastYC);

        cellAbsXs = new int[xC];
        for (int i = 0; i < xC; i++)
            cellAbsXs[i] = cellAbsXsLong.get(i);
        cellAbsYs = new int[yC];
        for (int i = 0; i < yC; i++)
            cellAbsYs[i] = cellAbsYsLong.get(i);

        System.out.println("CellAbsXs: {");
        System.out.println("\t" + Arrays.toString(cellAbsXs));
        System.out.println('}');
        System.out.println("CellAbsYs: {");
        System.out.println("\t" + Arrays.toString(cellAbsYs));
        System.out.println('}');

        camAbsX = xInnerEdge;
        camAbsY = yInnerEdge;
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

    public HashMap<Integer, Integer> getxOffsets() {
        return xOffsets;
    }

    public HashMap<Integer, Integer> getyOffsets() {
        return yOffsets;
    }

    public int getMaxXC() {
        return maxXC;
    }

    public int getMaxYC() {
        return maxYC;
    }

    public void setMaxXC(int maxXC) {
        this.maxXC = maxXC;
    }

    public void setMaxYC(int maxYC) {
        this.maxYC = maxYC;
    }
}

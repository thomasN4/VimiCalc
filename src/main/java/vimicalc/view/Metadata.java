package vimicalc.view;

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

    private int xEdge(boolean explicit, int cAbsX, int xC, int explicitEdge) {
        if (!explicit)
            return cAbsX + picW + 2*DCW +
                   ((xOffsets.get(xC) == null) ? 0 : xOffsets.get(xC)) +
                   ((xOffsets.get(xC+1) == null) ? 0 : xOffsets.get(xC+1));
        else return explicitEdge;
    }
    private int yEdge(boolean explicit, int cAbsY, int yC, int explicitEdge) {
        if (!explicit)
            return cAbsY + picH + 2*DCH +
                    ((yOffsets.get(yC) == null) ? 0 : yOffsets.get(yC)) +
                    ((yOffsets.get(yC+1) == null) ? 0 : yOffsets.get(yC+1));
        else return explicitEdge;
    }
    public void generate(int cAbsX, int cAbsY, int explicitXEdge, int explicitYEdge) {
        System.out.println("Generating metadata...");
        int[] cellAbsXsLong = new int[picW+DCW], cellAbsYsLong = new int[picH+DCH];
        int currAbsX = DCW, currAbsY = DCH, xC = 1, yC = 1;
        boolean firstXCFound = false, lastXCFound = false,
                firstYCFound = false, lastYCFound = false,
                explicitX = explicitXEdge != -1, explicitY = explicitYEdge != -1;
        Integer xOffset, yOffset;

        do {
            if (currAbsX > cAbsX && !firstXCFound) {
                firstXC = xC - 1;
                firstXCFound = true;
            }
            cellAbsXsLong[xC] = currAbsX;
            xOffset = xOffsets.get(xC);
            currAbsX += DCW + ((xOffset == null) ? 0 : xOffset);
            if (currAbsX >= cAbsX + picW && !lastXCFound) {
                System.out.println(currAbsX + " " + cAbsX + " " + picW + " " + DCW);
                lastXC = xC;
                lastXCFound = true;
            }
            xC++;
        } while (currAbsX <= xEdge(explicitX, cAbsX, xC, explicitXEdge));
        cellAbsXsLong[xC] = currAbsX;
        System.out.println("firstXC = " + firstXC);
        System.out.println("lastXC = " + lastXC);

        do {
            if (currAbsY > cAbsY && !firstYCFound) {
                firstYC = yC - 1;
                firstYCFound = true;
            }
            cellAbsYsLong[yC] = currAbsY;
            yOffset = yOffsets.get(yC);
            currAbsY += DCH + ((yOffset == null) ? 0 : yOffset);
            if (currAbsY >= cAbsY + picH && !lastYCFound) {
                System.out.println(currAbsY + " " + cAbsY + " " + picH + " " + DCH);
                lastYC = yC;
                lastYCFound = true;
            }
            yC++;
        } while (currAbsY <= yEdge(explicitY, cAbsY, yC, explicitYEdge));
        cellAbsYsLong[yC] = currAbsY;
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

        this.camAbsX = cAbsX;
        this.camAbsY = cAbsY;
    }

    // newOffset = {coord (X ou Y, conditionnel sur isXAxis), offset}
    public void generate(int[] newOffset, boolean isXAxis) {
        if (isXAxis)
            xOffsets.put(newOffset[0], newOffset[1]);
        else
            yOffsets.put(newOffset[0], newOffset[1]);
        generate(camAbsX, camAbsY, -1, -1);
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
}

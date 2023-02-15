package vimicalc.model;

import java.util.Vector;

import static vimicalc.controller.Controller.sheet;

public class Tensor_utils {
    public Tensor_utils() {}

    // Ne retourne que les valeurs des cellules déjà initialisées
    public Vector<Double> createVectorFromArea(String s) {
        Vector<Double> vector = new Vector<>();
        StringBuilder firstCoords = new StringBuilder();
        String lastCoords;

        int i = 0;
        for ( ; s.charAt(i) != ':'; i++)
            firstCoords.append(s.charAt(i));
        lastCoords = s.substring(i+1);

        int firstCoordX = sheet.findCell(firstCoords.toString()).xCoord();
        int firstCoordY = sheet.findCell(firstCoords.toString()).yCoord();
        int lastCoordX = sheet.findCell(lastCoords).xCoord();
        int lastCoordY = sheet.findCell(lastCoords).yCoord();

        for (Cell c : sheet.getCells())
            if (c.xCoord() >= firstCoordX && c.xCoord() <= lastCoordX &&
                c.yCoord() >= firstCoordY && c.yCoord() <= lastCoordY)
                vector.add(c.val());

        return vector;
    }
}
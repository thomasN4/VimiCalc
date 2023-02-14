package vimicalc.model;

import java.util.ArrayList;
import java.util.Vector;

public class Tensor_utils {
    public Tensor_utils() {}

    public void AddMatrixToVector(Vector<Double> vector, ArrayList<Vector<Double>> matrix) {
        for (Vector<Double> v : matrix)
            vector.addAll(v);
    }

    public ArrayList<Vector<Double>> createMatrixFromCoords(String s) {
        ArrayList<Vector<Double>> mat = new ArrayList<Vector<Double>>();
        ;
        return mat;
    }
}
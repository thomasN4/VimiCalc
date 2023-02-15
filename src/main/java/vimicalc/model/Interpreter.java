package vimicalc.model;

import java.util.ArrayList;
import java.util.Vector;

import static vimicalc.controller.Controller.sheet;

public class Interpreter {

    private final String[] args_0 = {
            "sum",
    };

    private String rawFormula;
    private ArrayList<String> lexedFormula;
    private double numericResult;

    public Interpreter() {
        lexedFormula = new ArrayList<String>();
    }

    public void setRawFormula(String rawFormula) {
        this.rawFormula = rawFormula;
    }

    private void lexer() {
        String arg_i = "";
        for (int i = 0; i < rawFormula.length(); i++) {
            arg_i += rawFormula.charAt(i);
            if (rawFormula.charAt(i) == ' ')
                lexedFormula.add(arg_i);
        }
        lexedFormula.add(arg_i);
    }

    private void sum(Vector<Double> nums) {
        double s = 0;
        for (Double num : nums)
            s += num;
        numericResult = s;
    }

    public void interpret() {
        lexer();
        switch (lexedFormula.get(0)) {
            case "sum" -> {
                Vector<Double> nums = new Vector<Double>();
                Tensor_utils t = new Tensor_utils();
                boolean isCoordsArea = false;
                for (String s : lexedFormula.subList(1, lexedFormula.size())) {
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) == ':') {
                            t.AddMatrixToVector(nums, t.createMatrixFromCoords(s));
                            isCoordsArea = true;
                            break;
                        }
                    }
                    if (!isCoordsArea)
                        nums.add((sheet.findCell(s)).val());
                }
                sum(nums);
            }
        }
    }

    public double getNumericResult() {
        return numericResult;
    }

    public ArrayList<String> getLexedFormula() {
        return lexedFormula;
    }
}

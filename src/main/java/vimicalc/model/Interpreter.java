package vimicalc.model;

import java.util.ArrayList;
import java.util.Vector;

import static vimicalc.controller.Controller.sheet;

public class Interpreter {
    private String rawFormula;
    private ArrayList<String> lexedFormula;
    private double numericResult;
    private final Tensor_utils tensor_utils;

    public Interpreter() {
        lexedFormula = new ArrayList<>();
        tensor_utils = new Tensor_utils();
    }

    public void setRawFormula(String rawFormula) {
        this.rawFormula = rawFormula;
    }

    private void lexer() {
        lexedFormula = new ArrayList<>();
        StringBuilder arg_i = new StringBuilder();
        for (int i = 0; i < rawFormula.length(); i++) {
            if (rawFormula.charAt(i) == ' ') {
                lexedFormula.add(arg_i.toString());
                arg_i = new StringBuilder();
            } else
                arg_i.append(rawFormula.charAt(i));
        }
        lexedFormula.add(arg_i.toString());
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
                Vector<Double> nums = new Vector<>();
                boolean isCoordsArea = false;
                for (String s : lexedFormula.subList(1, lexedFormula.size())) {
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) == ':') {
                            nums.addAll(tensor_utils.createVectorFromArea(s));
                            isCoordsArea = true;
                            break;
                        }
                    }
                    if (!isCoordsArea)
                        nums.add((sheet.findCell(s)).val());
                    isCoordsArea = false;
                }
                System.out.println(nums);
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

    public void setLexedFormula(ArrayList<String> lexedFormula) {
        this.lexedFormula = lexedFormula;
    }
}

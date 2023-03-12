package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Vector;

import static vimicalc.Main.isNumber;

public class Formula extends Interpretable {
    public Formula(String txt) {
        super(txt);
    }

    private @NotNull String sum(@NotNull Vector<String> nums) {
        double s = 0;
        for (String n : nums) {
            if (n.equals("I")) continue;
            s += Double.parseDouble(n);
        }
        return String.valueOf(s);
    }

    private @NotNull String product(@NotNull Vector<String> factors) {
        double p = 1;
        for (String f : factors) {
            if (f.equals("I")) continue;
            p *= Double.parseDouble(f);
        }
        return String.valueOf(p);
    }

    private @NotNull String quotient(@NotNull Vector<String> nums) {
        double q = Double.parseDouble(nums.get(0));
        for (int i = 1; i < nums.size(); i++) {
            if (nums.get(i).equals("I")) continue;
            q /= Double.parseDouble(nums.get(i));
        }
        return String.valueOf(q);
    }

    private String arithmetic(String arg0, @NotNull ArrayList<String> args, Sheet sheet) {
        Vector<String> nums = new Vector<>();
        boolean isCoordsArea = false;
        for (String s : args.subList(1, args.size())) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == ':') {
                    nums.addAll(createVectorFromArea(s, sheet));
                    isCoordsArea = true;
                    break;
                }
            }
            if (!isCoordsArea)
                nums.add(interpret(s, sheet));
            isCoordsArea = false;
        }
        return switch (arg0) {
            case "+" -> sum(nums);
            case "*" -> product(nums);
            case "/" -> quotient(nums);
            default -> "0";
        };
    }

    public String interpret(String raw, Sheet sheet) {
        ArrayList<String> args = lexer(raw);
        String arg0 = args.get(0);
        if (arg0.charAt(0) == '(') {
            return interpret(
                arg0.substring(1, arg0.length() - 1) +
                unlexer(args.subList(1, args.size())), sheet
            );
        }
        else if (arg0.equals("+") || arg0.equals("*") || arg0.equals("/")) {
            return arithmetic(arg0, args, sheet);
        }
        else if (arg0.equals("det")) {
            return determinant(args.get(1), sheet);
        }
        else if (isNumber(arg0)) {
            return arg0;
        }
        else if (arg0.charAt(0) == '-') {
            return interpret("(* -1 " + arg0.substring(1) + ')', sheet);
        }
        else {
            Cell c = sheet.findCell(arg0);
            if (c.formula() != null)
                return interpret(c.formula().getTxt(), sheet);
            else {
                if (!c.txt().equals(""))
                    return String.valueOf(c.value());
                else
                    return "I";
            }
        }
    }

    private @NotNull String determinant(String coords, Sheet sheet) {
        return String.valueOf(determinant(
            createMatrixFromArea(coords, sheet)
        ));
    }

    private double determinant(double[][] imat) {
        if (imat.length > 2) {
            ArrayList<double[][]> omats = new ArrayList<>();
            for (int ignoredRow = 0; ignoredRow < imat.length; ignoredRow++) {
                double[][] omat = new double[imat.length - 1][imat.length - 1];
                int om_i = 0;
                for (int im_i = 1; im_i < imat.length; im_i++) {
                    for (int im_j = 1; im_j < imat.length; im_j++) {
                        if (im_i != ignoredRow) omat[om_i][im_j -1] = imat[im_i][im_j];
                    }
                    om_i++;
                }
                omats.add(omat);
            }

            double sum = 0;
            for (int i = 0; i < imat.length; i++) {
                sum += Math.pow(-1, i) * determinant(omats.get(i));
            }
            return sum;
        }
        else return imat[0][0] * imat[1][1] - imat[0][1] * imat[1][0];
    }
}

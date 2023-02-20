package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Vector;

import static vimicalc.Main.isNumber;

public class Formula {
    private String txt;

    public Formula(String txt) {
        this.txt = txt;
    }

    public Vector<String> createVectorFromArea(@NotNull String s, Sheet sheet) {
        Vector<String> vector = new Vector<>();
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
                    c.yCoord() >= firstCoordY && c.yCoord() <= lastCoordY) {
                vector.add(c.formula().interpret(sheet));
            }

        return vector;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    private @NotNull ArrayList<String> lexer(@NotNull String rawFormula) {
        ArrayList<String> lexedFormula = new ArrayList<>();
        StringBuilder arg_i = new StringBuilder();
        boolean isBetweenParentheses = false;

        for (int i = 0; i < rawFormula.length(); i++) {
            if (rawFormula.charAt(i) == '(') {
                isBetweenParentheses = true;
                arg_i.append('(');
            } else if (rawFormula.charAt(i) == ')') {
                arg_i.append(')');
                lexedFormula.add(arg_i.toString());
                arg_i = new StringBuilder();
                isBetweenParentheses = false;
                i++;
            } else if (isBetweenParentheses) {
                arg_i.append(rawFormula.charAt(i));
            } else if (rawFormula.charAt(i) == ' ') {
                lexedFormula.add(arg_i.toString());
                arg_i = new StringBuilder();
            } else
                arg_i.append(rawFormula.charAt(i));
        }
        lexedFormula.add(arg_i.toString());
        return lexedFormula;
    }

    private @NotNull String sum(@NotNull Vector<String> nums) {
        double s = 0;
        for (String n : nums)
            s += Double.parseDouble(n);
        return String.valueOf(s);
    }

    private @NotNull String product(@NotNull Vector<String> factors) {
        double p = 1;
        for (String f : factors)
            p *= Double.parseDouble(f);
        return String.valueOf(p);
    }

    private @NotNull String quotient(@NotNull Vector<String> nums) {
        double q = Double.parseDouble(nums.get(0));
        for (int i = 1; i < nums.size(); i++)
            q /= Double.parseDouble(nums.get(i));
        return String.valueOf(q);
    }

    private String arithmetic(String arg0, @NotNull ArrayList<String> args, Sheet sheet) {
        Vector<String> nums = new Vector<>();
        boolean isCoordsArea = false;
        for (String s : args.subList(1, args.size())) {
            if (s.charAt(0) == '(') {
                nums.add(interpret(s.substring(1, s.length() - 1), sheet));
                continue;
            }
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == ':') {
                    nums.addAll(createVectorFromArea(s, sheet));
                    isCoordsArea = true;
                    break;
                }
            }
            if (!isCoordsArea) {
                String fromCoordinate = sheet.findCell(s).formula().getTxt();
                if (!fromCoordinate.equals("0"))
                    nums.add(fromCoordinate);
                else nums.add(interpret(s, sheet));
            }
            isCoordsArea = false;
        }
        return switch (arg0) {
            case "+" -> sum(nums);
            case "*" -> product(nums);
            case "/" -> quotient(nums);
            default -> "0";
        };
    }

    public String interpret(Sheet sheet) {
        return interpret(txt, sheet);
    }

    public String interpret(String raw, Sheet sheet) {
        ArrayList<String> args = lexer(raw);
        System.out.println("Interpreting \""+args+'\"');
        String arg0 = args.get(0);
        if (arg0.equals("+") || arg0.equals("*") || arg0.equals("/")) {
            return arithmetic(arg0, args, sheet);
        } else if (isNumber(arg0)) {
            System.out.println("isNumber(arg0) = "+isNumber(arg0));
            return arg0;
        } else {
            return interpret(sheet.findCell(arg0).formula().getTxt(), sheet);
        }
    }

    public String getTxt() {
        return txt;
    }
}

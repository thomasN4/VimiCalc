package vimicalc.model;

import java.util.ArrayList;

public class Formula extends Interpretable {
    public Formula(String txt) {
        super(txt);
    }

    private double sum(Lexeme[] nums) {
        double s = 0;
        for (Lexeme n : nums) {
            if (n.getFunc().equals("I")) continue;
            s += n.getVal();
        }
        return s;
    }

    private double product(Lexeme[] nums) {
        double p = 1;
        for (Lexeme n : nums) {
            if (n.getFunc().equals("I")) continue;
            p *= n.getVal();
        }
        return p;
    }

    private double quotient(Lexeme[] nums) {
        double q;
        if (nums[0].getFunc().equals("I")) q = 1;
        else q = nums[0].getVal();
        for (int i = 1; i < nums.length; i++) {
            if (nums[i].getFunc().equals("I")) continue;
            q /= nums[i].getVal();
        }
        return q;
    }

    private double matArithmetic(String arg0, Lexeme arg1, Sheet sheet) {
        Lexeme[] vector = createVectorFromArea(arg1.getFunc(), sheet);

        return switch (arg0) {
            case "sum" -> sum(vector);
            case "prod" -> product(vector);
            case "quot" -> quotient(vector);
            default -> 0;
        };
    }

    private Lexeme negative(String arg, Sheet sheet) {
        return interpret(
            new Lexeme[]{
                new Lexeme(-1),
                new Lexeme(arg.substring(1)),
                new Lexeme('*')
            },
            sheet
        )[0];
    }

    public Lexeme[] interpret(Lexeme[] args, Sheet sheet) {
        Lexeme[] newArgs = new Lexeme[args.length-1];
        if (args.length == 1) {
            if (!args[0].isFunction())
                return args;
            else if (args[0].getFunc().charAt(0) == '-')
                return new Lexeme[]{negative(args[0].getFunc(), sheet)};
            else
                return new Lexeme[]{cellToLexeme(args[0].getFunc(), sheet)};
        }
        else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].isFunction()) {
                    String func = args[i].getFunc();
                    if (func.charAt(0) == '-' && func.length() > 1) {
                        args[i] = negative(func, sheet);
                        continue;
                    }
                    switch (func) {
                        case "sum", "prod", "quot" -> newArgs = reducedLs(
                            args, i, 1, new Lexeme(matArithmetic(func, args[i-1], sheet))
                        );
                        case "det" -> newArgs = reducedLs(
                            args, i, 1, new Lexeme(determinant(args[i-1].getFunc(), sheet))
                        );
                        case "+" -> {
                            if (i > 1) {
                                Lexeme x = (interpret(new Lexeme[]{args[i-2]}, sheet))[0];
                                Lexeme y = (interpret(new Lexeme[]{args[i-1]}, sheet))[0];
                                if (x.getFunc().equals("I")) x.setVal(0);
                                if (y.getFunc().equals("I")) y.setVal(0);
                                newArgs = reducedLs(
                                    args, i, 2, new Lexeme(x.getVal() + y.getVal())
                                );
                            } else
                                System.out.println("Not enough args.");
                        }
                        case "-" -> {
                            if (i > 1) {
                                Lexeme x = (interpret(new Lexeme[]{args[i-2]}, sheet))[0];
                                Lexeme y = (interpret(new Lexeme[]{args[i-1]}, sheet))[0];
                                if (x.getFunc().equals("I")) x.setVal(0);
                                if (y.getFunc().equals("I")) y.setVal(0);
                                newArgs = reducedLs(
                                        args, i, 2, new Lexeme(x.getVal() - y.getVal())
                                );
                            } else
                                System.out.println("Not enough args.");
                        }
                        case "*" -> {
                            if (i > 1) {
                                Lexeme x = (interpret(new Lexeme[]{args[i-2]}, sheet))[0];
                                Lexeme y = (interpret(new Lexeme[]{args[i-1]}, sheet))[0];
                                if (x.getFunc().equals("I")) x.setVal(1);
                                if (y.getFunc().equals("I")) y.setVal(1);
                                newArgs = reducedLs(
                                    args, i, 2, new Lexeme(x.getVal() * y.getVal())
                                );
                            } else
                                System.out.println("Not enough args.");
                        }
                        case "/" -> {
                            if (i > 1) {
                                Lexeme x = (interpret(new Lexeme[]{args[i-2]}, sheet))[0];
                                Lexeme y = (interpret(new Lexeme[]{args[i-1]}, sheet))[0];
                                if (x.getFunc().equals("I")) x.setVal(1);
                                if (y.getFunc().equals("I")) y.setVal(1);
                                newArgs = reducedLs(
                                    args, i, 2, new Lexeme(x.getVal() / y.getVal())
                                );
                            } else
                                System.out.println("Not enough args.");
                        }
                        default -> {
                            if (func.contains(":")) continue;
                            args[i] = cellToLexeme(func, sheet);
                            continue;
                        }
                    }
                    break;
                }
            }
            return interpret(newArgs, sheet);
        }
    }
    private Lexeme[] reducedLs(Lexeme[] original, int currPos, int reduction, Lexeme reduced) {
        Lexeme[] newArgs = new Lexeme[original.length-reduction];
        System.arraycopy(original, 0, newArgs, 0, currPos-reduction);
        newArgs[currPos-reduction] = reduced;
        System.arraycopy(original
            , currPos+1
            , newArgs
            , currPos-reduction+1
            , original.length-currPos-1);
        return newArgs;
    }
    private Lexeme cellToLexeme(String coords, Sheet sheet) {
        Cell c = sheet.findCell(coords);
        if (c.formula() != null) {
            return new Lexeme(c.formula().interpret(sheet));
        }
        else if (c.txt().equals(""))
            return new Lexeme("I");
        else
            return new Lexeme(c.value());
    }

    private double determinant(String coords, Sheet sheet) {
        return determinant(
            createMatrixFromArea(coords, sheet)
        );
    }

    private double determinant(double[][] imat) {
        if (imat.length > 2) {
            ArrayList<double[][]> omats = new ArrayList<>();
            for (int ignoredRow = 0; ignoredRow < imat.length; ignoredRow++) {
                double[][] omat = new double[imat.length - 1][imat.length - 1];
                int om_i = 0;
                for (int im_i = 0; im_i < imat.length; im_i++) {
                    if (im_i == ignoredRow) continue;
                    System.arraycopy(imat[im_i], 1, omat[om_i], 0, imat.length - 1);
                    om_i++;
                }
                omats.add(omat);
            }

            double sum = 0;
            for (int i = 0; i < imat.length; i++) {
                sum += Math.pow(-1, i) * determinant(omats.get(i)) * imat[i][0];
            }
            return sum;
        }
        else return imat[0][0] * imat[1][1] - imat[0][1] * imat[1][0];
    }
}


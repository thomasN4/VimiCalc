package vimicalc.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Formula extends Interpretable {
    public Formula(String txt) {
        super(txt);
    }

    private double sum(Lexeme[] nums) {
        double s = 0;
        for (Lexeme n : nums) {
            if (n.isFunction()) continue;
            s += n.getVal();
        }
        return s;
    }

    private double product(Lexeme[] nums) {
        double p = 1;
        for (Lexeme n : nums) {
            if (n.isFunction()) continue;
            p *= n.getVal();
        }
        return p;
    }

    private double quotient(Lexeme[] nums) {
        double q;
        if (nums[0].isFunction()) q = 1;
        else q = nums[0].getVal();
        for (int i = 1; i < nums.length; i++) {
            if (nums[i].isFunction()) continue;
            q /= nums[i].getVal();
        }
        return q;
    }

    private double matArithmetic(@NotNull String arg0, @NotNull Lexeme arg1, Sheet sheet) {
        Lexeme[] vector = createVectorFromArea(arg1.getFunc(), sheet);
        return switch (arg0) {
            case "sum" -> sum(vector);
            case "prod" -> product(vector);
            case "quot" -> quotient(vector);
            default -> 0;
        };
    }

    private Lexeme negative(@NotNull String arg, Sheet sheet) {
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
        byte reduction;
        Lexeme reduced;
        System.out.println("args.length = " + args.length);
        for (int i = 0; args.length > 1; i++) {
            reduction = 0;
            reduced = null;
            if (args[i].isFunction()) {
                String func = args[i].getFunc();
                if (func.charAt(0) == '-' && func.length() > 1) {
                    args[i] = negative(func, sheet);
                    continue;
                }
                switch (func) {
                    case "sum", "prod", "quot" -> {
                        reduction = 1;
                        reduced = new Lexeme(matArithmetic(func, args[i-1], sheet));
                    }
                    case "det" -> {
                        reduction = 1;
                        reduced = new Lexeme(determinant(args[i-1].getFunc(), sheet));
                    }
                    case "+" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(0);
                            if (y.isFunction()) y.setVal(0);
                            reduced = new Lexeme(x.getVal() + y.getVal());
                        } else
                            System.out.println("Not enough args.");
                    }
                    case "-" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(0);
                            if (y.isFunction()) y.setVal(0);
                            reduced = new Lexeme(x.getVal() - y.getVal());
                        } else
                            System.out.println("Not enough args.");
                    }
                    case "*" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(1);
                            if (y.isFunction()) y.setVal(1);
                            reduced = new Lexeme(x.getVal() * y.getVal());
                        } else
                            System.out.println("Not enough args.");
                    }
                    case "/" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(1);
                            if (y.isFunction()) y.setVal(1);
                            reduced = new Lexeme(x.getVal() / y.getVal());
                        } else
                            System.out.println("Not enough args.");
                    }
                    default -> {
                        if (func.contains(":")) continue;
                        args[i] = cellToLexeme(func, sheet);
                    }
                }
                if (reduction != 0) {
                    Lexeme[] newArgs = new Lexeme[args.length-reduction];
                    System.arraycopy(args, 0, newArgs, 0, i-reduction);
                    newArgs[i-reduction] = reduced;
                    System.arraycopy(args
                        , i+1
                        , newArgs
                        , i-reduction+1
                        , args.length-i-1);
                    args = new Lexeme[newArgs.length];
                    System.arraycopy(newArgs, 0, args, 0, args.length);
                    i -= reduction;
                }
            }
        }
        if (!args[0].isFunction())
            return args;
        else if (args[0].getFunc().charAt(0) == '-')
            return new Lexeme[]{negative(args[0].getFunc(), sheet)};
        else
            return new Lexeme[]{cellToLexeme(args[0].getFunc(), sheet)};
    }
    @Contract("_, _ -> new")
    private @NotNull Lexeme cellToLexeme(String coords, @NotNull Sheet sheet) {
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
            Matrix[] omats = new Matrix[imat.length];
            for (int ignoredRow = 0; ignoredRow < imat.length; ignoredRow++) {
                double[][] omat = new double[imat.length - 1][imat.length - 1];
                int om_i = 0;
                for (int im_i = 0; im_i < imat.length; im_i++) {
                    if (im_i == ignoredRow) continue;
                    System.arraycopy(imat[im_i], 1, omat[om_i], 0, imat.length - 1);
                    om_i++;
                }
                omats[ignoredRow] = new Matrix(omat);
            }

            double sum = 0;
            for (int i = 0; i < imat.length; i++) {
                sum += Math.pow(-1, i) * determinant(omats[i].getItems()) * imat[i][0];
            }
            return sum;
        }
        else return imat[0][0] * imat[1][1] - imat[0][1] * imat[1][0];
    }
}

class Matrix {
    double[][] items;

    Matrix(double[][] items) {
        this.items = items;
    }

    public double[][] getItems() {
        return items;
    }
}


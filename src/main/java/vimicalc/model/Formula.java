package vimicalc.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Formula extends Interpretable {
    public Formula(String txt, int sCX, int sCY) {
        super(txt, sCX, sCY);
    }

    protected double[][] createMatrixFromArea(@NotNull String s, @NotNull Sheet sheet) {
        sheet.addDependent(sCX, sCY);
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
        double[][] mat = new double[lastCoordY - firstCoordY + 1][lastCoordX - firstCoordX + 1];

        for (i = 0; i <= lastCoordY - firstCoordY; i++) {
            final int I = i;
            for (int j = 0; j <= lastCoordX - firstCoordX; j++) {
                final int J = j;
                sheet.getCells().forEach(c -> {
                    if (c.xCoord() == firstCoordX + J && c.yCoord() == firstCoordY + I) {
                        if (c.txt() == null || c.txt().equals("t3mp"))
                            mat[I][J] = 0;
                        else
                            mat[I][J] = c.value();
                        sheet.addDepended(c.xCoord(), c.yCoord(), sheet.findDependency(sCX, sCY));
                    }
                });
            }
        }

        return mat;
    }

    protected Lexeme[] createVectorFromArea(@NotNull String coords, @NotNull Sheet sheet) {
        sheet.addDependent(sCX, sCY);
        StringBuilder firstCoords = new StringBuilder();
        String lastCoords;

        int i = 0;
        for (; coords.charAt(i) != ':'; i++)
            firstCoords.append(coords.charAt(i));
        lastCoords = coords.substring(i+1);

        int firstCoordX = sheet.findCell(firstCoords.toString()).xCoord();
        int firstCoordY = sheet.findCell(firstCoords.toString()).yCoord();
        int lastCoordX = sheet.findCell(lastCoords).xCoord();
        int lastCoordY = sheet.findCell(lastCoords).yCoord();
        Lexeme[] vectorLong = new Lexeme[
                (lastCoordX - firstCoordX + 1) * (lastCoordY - firstCoordY + 1)
                ];

        i = 0;
        for (Cell c : sheet.getCells())
            if (c.xCoord() >= firstCoordX && c.xCoord() <= lastCoordX &&
                c.yCoord() >= firstCoordY && c.yCoord() <= lastCoordY) {
                if (c.txt() == null || c.txt().equals("t3mp"))
                    vectorLong[i++] = new Lexeme("I");
                else
                    vectorLong[i++] = new Lexeme(c.value());
                sheet.addDepended(c.xCoord(), c.yCoord(), sheet.findDependency(sCX, sCY));
            }

        Lexeme[] vector = new Lexeme[i];
        System.arraycopy(vectorLong, 0, vector, 0, i);

        return vector;
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

    private double tableArithmetic(@NotNull String arg0, @NotNull Lexeme arg1, Sheet sheet) {
        Lexeme[] vector = createVectorFromArea(arg1.getFunc(), sheet);
        return switch (arg0) {
            case "sum" -> sum(vector);
            case "prod" -> product(vector);
            case "quot" -> quotient(vector);
            default -> 0;
        };
    }

    private Lexeme negative(@NotNull String arg, Sheet sheet) {
        return (interpret(
            new Lexeme[]{
                new Lexeme(-1),
                new Lexeme(arg.substring(1)),
                new Lexeme("*")
            },
            sheet
        ))[0];
    }

    public Lexeme[] interpret(Lexeme[] args, Sheet sheet) {
        byte reduction;
        Lexeme reduced;

        for (int i = 0; args.length > 1; i++) {
            reduction = 0;
            reduced = null;
            if (args[i].isFunction()) {
                String func = args[i].getFunc();
                System.out.println("func = \"" + func + '\"');
                if (func.charAt(0) == '-' && func.length() > 1) {
                    args[i] = negative(func, sheet);
                    continue;
                }
                switch (func) {
                    case "sum", "prod", "quot" -> {
                        reduction = 1;
                        reduced = new Lexeme(tableArithmetic(func, args[i-1], sheet));
                    }
                    case "det" -> {
                        reduction = 1;
                        try {
                            reduced = new Lexeme(determinant(args[i - 1].getFunc(), sheet));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            reduced = new Lexeme(0);
                        }
                    }
                    case "matMult" -> {
                        reduction = 2;
                        try {
                            reduced = new Lexeme(matMult(
                                args[i-2].getFunc(), args[i-1].getFunc(), sheet
                            ));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            reduced = new Lexeme(0);
                        }
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
                        if (func.contains(":") || func.contains("\\")) continue;
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
        sheet.addDependent(sCX, sCY);
        sheet.addDepended(c.xCoord(), c.yCoord(), sheet.findDependency(sCX, sCY));

        if (c.txt() == null || c.txt().equals("t3mp"))
            return new Lexeme("I");
        else
            return new Lexeme(c.value());
    }

    private double determinant(String coords, Sheet sheet) throws Exception {
        return determinant(
            createMatrixFromArea(coords, sheet)
        );
    }
    private double determinant(double[][] imat) throws Exception {
        if (imat.length != imat[0].length)
            throw new Exception("The matrix isn't square.");

        if (imat.length > 2) {
            Matrix[] omats = new Matrix[imat.length];
            for (int ignoredRow = 0; ignoredRow < imat.length; ignoredRow++) {
                double[][] omat = new double[imat.length-1][imat.length-1];
                int om_i = 0;
                for (int im_i = 0; im_i < imat.length; im_i++) {
                    if (im_i == ignoredRow) continue;
                    System.arraycopy(imat[im_i], 1, omat[om_i], 0, imat.length-1);
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
    
    public double matMult(String coords1, String coords2, Sheet sheet) throws Exception {
        Matrix mat1 = new Matrix(createMatrixFromArea(coords1, sheet));
        Matrix mat2 = new Matrix(createMatrixFromArea(coords2, sheet));

        if (mat1.getWidth() != mat2.getHeight())
            throw new Exception("There's a mismatch in the number of rows and columns.");

        for (int i = 0; i < mat1.getHeight(); i++) {
            for (int j = 0; j < mat2.getWidth(); j++) {
                if (i == 0 && j == 0)
                    continue;
                sheet.addCell(new Cell(
                    sCX + j,
                    sCY + i,
                    forOnePos(mat1.getRow(i), mat2.getCol(j))
                ));
            }
        }

        return forOnePos(mat1.getRow(0), mat2.getCol(0));
    }
    public double forOnePos(double[] row, double[] col) {
        double v = 0;
        for (int i = 0; i < row.length; i++) {
            v += row[i] * col[i];
        }
        return v;
    }
}

class Matrix {
    private final double[][] items;
    private final int width;
    private final int height;

    @Contract(pure = true)
    Matrix(double[][] items) {
        this.items = items;
        width = items[0].length;
        height = items.length;
    }

    public double[][] getItems() {
        return items;
    }

    public double[] getRow(int i) {
        return items[i];
    }

    public double[] getCol(int j) {
        double[] col = new double[items.length];
        for (int i = 0; i < items.length; i++)
            col[i] = items[i][j];
        return col;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
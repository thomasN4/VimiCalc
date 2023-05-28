package vimicalc.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.lang.Math.pow;
import static vimicalc.controller.KeyCommand.Mfuncs;
import static vimicalc.utils.Conversions.*;

public class Formula extends Interpretable {
    public Formula(String txt, int xC, int yC) {
        super(txt, xC, yC);
    }

    protected double[][] createMatrixFromArea(@NotNull String s, @NotNull Sheet sheet) throws Exception {
        sheet.addDependent(xC, yC);
        StringBuilder firstCoords = new StringBuilder();
        String lastCoords;

        int i = 0;
        for ( ; s.charAt(i) != ':' && s.charAt(i) != ';'; i++)
            firstCoords.append(s.charAt(i));
        lastCoords = s.substring(i+1);

        if (Mfuncs.contains(firstCoords.charAt(firstCoords.length()-1))) {
            firstCoords = new StringBuilder(relToAbsCoords(firstCoords.toString(), xC, yC));
        }
        if (Mfuncs.contains(lastCoords.charAt(lastCoords.length()-1))) {
            lastCoords = relToAbsCoords(lastCoords, xC, yC);
        }

        int firstCoordX = sheet.findCell(firstCoords.toString()).xCoord();
        int firstCoordY = sheet.findCell(firstCoords.toString()).yCoord();
        int lastCoordX = sheet.findCell(lastCoords).xCoord();
        int lastCoordY = sheet.findCell(lastCoords).yCoord();
        double[][] mat = new double[lastCoordY - firstCoordY + 1][lastCoordX - firstCoordX + 1];
        System.out.println("Creating matrix...");

        for (i = 0; i <= lastCoordY - firstCoordY; ++i) {
            for (int j = 0; j <= lastCoordX - firstCoordX; ++j) {
                int foundXC = j + firstCoordX, foundYC = i + firstCoordY;
                Cell c = sheet.findCell(foundXC, foundYC);
                if (c.value() == null)
                    mat[i][j] = 0;
                else
                    mat[i][j] = c.value();
                sheet.addDepended(foundXC, foundYC, sheet.findDependency(xC, yC));
            }
        }
        System.out.println("Matrix to be evaluated: " + Arrays.deepToString(mat));

        return mat;
    }

    protected Lexeme[] createVectorFromArea(@NotNull String coordsArea, @NotNull Sheet sheet) throws Exception {
        sheet.addDependent(xC, yC);
        StringBuilder firstCoords = new StringBuilder();
        String lastCoords;

        int k = 0;
        for (; coordsArea.charAt(k) != ':' && coordsArea.charAt(k) != ';'; k++)
            firstCoords.append(coordsArea.charAt(k));
        lastCoords = coordsArea.substring(k +1);

        if (Mfuncs.contains(firstCoords.charAt(firstCoords.length()-1))) {
            firstCoords = new StringBuilder(relToAbsCoords(firstCoords.toString(), xC, yC));
        }
        if (Mfuncs.contains(lastCoords.charAt(lastCoords.length()-1))) {
            lastCoords = relToAbsCoords(lastCoords, xC, yC);
        }

        int firstCoordX = sheet.findCell(firstCoords.toString()).xCoord();
        int firstCoordY = sheet.findCell(firstCoords.toString()).yCoord();
        int lastCoordX = sheet.findCell(lastCoords).xCoord();
        int lastCoordY = sheet.findCell(lastCoords).yCoord();
        Lexeme[] vectorLong = new Lexeme[
            (lastCoordX - firstCoordX + 1) * (lastCoordY - firstCoordY + 1)
        ];

        k = 0;
        for (int i = firstCoordY; i <= lastCoordY; ++i) {
            for (int j = firstCoordX; j <= lastCoordX; ++j) {
                Cell c = sheet.findCell(j, i);
                if (c.value() == null)
                    vectorLong[k++] = new Lexeme("I");
                else
                    vectorLong[k++] = new Lexeme(c.value());
                sheet.addDepended(j, i, sheet.findDependency(xC, yC));
            }
        }

        Lexeme[] vector = new Lexeme[k];
        System.arraycopy(vectorLong, 0, vector, 0, k);

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

    private double tableArithmetic(@NotNull String arg0, @NotNull Lexeme arg1, Sheet sheet) throws Exception {
        Lexeme[] vector = createVectorFromArea(arg1.getFunc(), sheet);
        return switch (arg0) {
            case "sum" -> sum(vector);
            case "prod" -> product(vector);
            case "quot" -> quotient(vector);
            default -> 0;
        };
    }

    private Lexeme negative(@NotNull String arg, Sheet sheet) throws Exception {
        return (interpret(
            new Lexeme[]{
                new Lexeme(-1),
                new Lexeme(arg.substring(1)),
                new Lexeme("*")
            },
            sheet
        ))[0];
    }

    public Lexeme[] interpret(Lexeme[] args, Sheet sheet) throws Exception {
        byte reduction;
        Lexeme reduced;

        for (int i = 0; args.length > 1 || !args[0].getFunc().equals(""); i++) {
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
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(tableArithmetic(func, args[i-1], sheet));
                        } else throw new Exception("Not enough args.");
                    }
                    case "det" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(determinant(args[i-1].getFunc(), sheet));
                        } else throw new Exception("Not enough args.");
                    }
//                    case "tpose" -> {
//                        if (i == 0) throw new Exception("Not enough args.");
//                        reduction = 1;
//                        reduced = new Lexeme(transpose(args[i-1].getFunc(), sheet));
//                    }
                    case "matMult" -> {
                        if (i > 1) {
                            reduction = 2;
                            reduced = new Lexeme(matMult(
                                args[i-2].getFunc(), args[i-1].getFunc(), sheet
                            ));
                        } else throw new Exception("Not enough args.");
                    }
                    case "sin" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.sin(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "asin" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.asin(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "cos" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.cos(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "acos" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.acos(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "tan" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.tan(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "atan" -> {
                        if (i > 0) {
                            reduction = 1;
                            reduced = new Lexeme(Math.atan(args[i - 1].getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "ln" -> {
                        if (i == 0) throw new Exception("Not enough args");
                        reduction = 1;
                        reduced = new Lexeme(Math.log(args[i-1].getVal()));
                    }
                    case "logBase" -> {
                        if (i < 2) throw new Exception("Not enough args");
                        reduction = 2;
                        reduced = new Lexeme(Math.log(args[i-2].getVal()) / Math.log(args[i-1].getVal()));
                    }
                    case "log10" -> {
                        if (i == 0) throw new Exception("Not enough args");
                        reduction = 1;
                        reduced = new Lexeme(Math.log10(args[i-1].getVal()));
                    }
                    case "exp" -> {
                        if (i == 0) throw new Exception("Not enough args");
                        reduction = 1;
                        reduced = new Lexeme(pow(Math.E, args[i - 1].getVal()));
                    }
                    case "^" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i - 2];
                            Lexeme y = args[i - 1];
                            if (x.isFunction()) x.setVal(0);
                            if (y.isFunction()) y.setVal(0);
                            reduced = new Lexeme(pow(x.getVal(), y.getVal()));
                        } else throw new Exception("Not enough args.");
                    }
                    case "+" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(0);
                            if (y.isFunction()) y.setVal(0);
                            reduced = new Lexeme(x.getVal() + y.getVal());
                        } else throw new Exception("Not enough args.");
                    }
                    case "-" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(0);
                            if (y.isFunction()) y.setVal(0);
                            reduced = new Lexeme(x.getVal() - y.getVal());
                        } else throw new Exception("Not enough args.");
                    }
                    case "*" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(1);
                            if (y.isFunction()) y.setVal(1);
                            reduced = new Lexeme(x.getVal() * y.getVal());
                        } else throw new Exception("Not enough args.");
                    }
                    case "/" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(1);
                            if (y.isFunction()) y.setVal(1);
                            reduced = new Lexeme(x.getVal() / y.getVal());
                        } else throw new Exception("Not enough args.");
                    }
                    case "mod" -> {
                        if (i > 1) {
                            reduction = 2;
                            Lexeme x = args[i-2];
                            Lexeme y = args[i-1];
                            if (x.isFunction()) x.setVal(1);
                            if (y.isFunction()) y.setVal(1);
                            reduced = new Lexeme(x.getVal() % y.getVal());
                        } else throw new Exception("Not enough args.");
                    }
                    case "PI" -> args[i] = new Lexeme(Math.PI);
                    default -> {
                        if (func.contains(":") || func.contains("\\") || func.contains(";"))
                            continue;
                        if ((isNumber(""+func.charAt(0)) ||
                                Mfuncs.contains(Character.toLowerCase(func.charAt(0)))) &&
                            Mfuncs.contains(func.charAt(func.length()-1)))
                            args[i] = cellToLexeme(
                                          relToAbsCoords(func, xC, yC),
                                          sheet,
                                          args.length
                                      );
                        else args[i] = cellToLexeme(func, sheet, args.length);
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
            return new Lexeme[]{cellToLexeme(args[0].getFunc(), sheet, args.length)};
    }

    private @NotNull Lexeme cellToLexeme(String coords, @NotNull Sheet sheet, int argsLength) throws Exception {
        Cell c = sheet.findCell(coords);
        sheet.addDependent(xC, yC);
        sheet.addDepended(c.xCoord(), c.yCoord(), sheet.findDependency(xC, yC));

        if (c.value() == null) {
            if (argsLength > 1) return new Lexeme("I");
            else return new Lexeme(0);
        } else return new Lexeme(c.value());
    }

//    private double transpose(String coords, Sheet sheet) throws Exception {
//        Matrix ogMat = new Matrix(createMatrixFromArea(coords, sheet));
//        for (int i = 0; i < ogMat.getHeight(); ++i)
//            for (int j = 0; j < ogMat.getWidth(); ++j)
//                if (i != 0 || j != 0)
//                    sheet.addCell(new Cell(
//                        xC + i,
//                        yC + j,
//                        ogMat.getRow(i)[j]
//                    ));
//        return ogMat.getRow(0)[0];
//    }

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
                sum += pow(-1, i) * determinant(omats[i].getItems()) * imat[i][0];
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
                    xC + j,
                    yC + i,
                    for1Pos(mat1.getRow(i), mat2.getCol(j))
                ));
            }
        }

        return for1Pos(mat1.getRow(0), mat2.getCol(0));
    }
    public double for1Pos(double[] row, double[] col) {
        double v = 0;
        for (int i = 0; i < row.length; i++) {
            v += row[i] * col[i];
        }
        return v;
    }
}

class Matrix {
    final double[][] items;
    final int width;
    final int height;

    @Contract(pure = true)
    Matrix(double[][] items) {
        this.items = items;
        width = items[0].length;
        height = items.length;
    }

    double[][] getItems() {
        return items;
    }

    double[] getRow(int i) {
        return items[i];
    }

    double[] getCol(int j) {
        double[] col = new double[items.length];
        for (int i = 0; i < items.length; i++)
            col[i] = items[i][j];
        return col;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }
}
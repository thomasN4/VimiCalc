package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static vimicalc.Main.isNumber;

abstract class Interpretable {
    protected String txt;

    protected Interpretable(String txt) {
        this.txt = txt;
    }

    protected Lexeme[] lexer(@NotNull String txt) {
        ArrayList<Lexeme> argsArrayList = new ArrayList<>();
        txt += ' ';
        String arg = "";

        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '(' || txt.charAt(i) == ')') continue;
            if (txt.charAt(i) == ' ') {
                if (isNumber(arg))
                    argsArrayList.add(new Lexeme(Double.parseDouble(arg)));
                else argsArrayList.add(new Lexeme(arg));
                arg = "";
                continue;
            }
            arg += txt.charAt(i);
        }

        Lexeme[] args = new Lexeme[argsArrayList.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = argsArrayList.get(i);
        }

        return args;
    }

    protected double[][] createMatrixFromArea(@NotNull String s, Sheet sheet) {
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
                        if (c.formula() != null)
                            mat[I][J] = c.formula().interpret(sheet);
                        else if (!c.txt().equals(""))
                            mat[I][J] = c.value();
                        else
                            mat[I][J] = 0;
                    }
                });
            }
        }

        return mat;
    }

    protected Lexeme[] createVectorFromArea(String coords, Sheet sheet) {
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
        Lexeme[] vector;
        if (lastCoordX - firstCoordX != 0)
            vector = new Lexeme[lastCoordX - firstCoordX + 1];
        else
            vector = new Lexeme[lastCoordY - firstCoordY + 1];

        i = 0;
        for (Cell c : sheet.getCells())
            if (c.xCoord() >= firstCoordX && c.xCoord() <= lastCoordX &&
                c.yCoord() >= firstCoordY && c.yCoord() <= lastCoordY) {
                if (c.formula() != null)
                    vector[i++] = new Lexeme(c.formula().interpret(sheet));
                else if (c.txt().equals(""))
                    vector[i++] = new Lexeme("I");
                else
                    vector[i++] = new Lexeme(String.valueOf(c.value()));
            }

        return vector;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public double interpret(Sheet sheet) {
        return interpret(lexer(txt), sheet)[0].getVal();
    }

    public abstract Lexeme[] interpret(Lexeme[] args, Sheet sheet);
}

class Lexeme {
    private final String func;
    private double val;
    private boolean isFunction;

    Lexeme(String func) {
        this.func = func;
        isFunction = true;
    }

    Lexeme(double val) {
        this.val = val;
        isFunction = false;
        func = "";
    }

    public String getFunc() {
        return func;
    }

    public double getVal() {
        return val;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public void setVal(double val) {
        this.val = val;
        isFunction = false;
    }
}

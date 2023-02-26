package vimicalc.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Vector;

abstract class Interpretable {
    protected String txt;

    protected Interpretable(String txt) {
        this.txt = txt;
    }

    protected @NotNull ArrayList<String> lexer(@NotNull String rawFormula) {
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
                if (i + 1 < rawFormula.length()) i++;
            } else if (isBetweenParentheses) {
                arg_i.append(rawFormula.charAt(i));
            } else if (rawFormula.charAt(i) == ' ') {
                lexedFormula.add(arg_i.toString());
                arg_i = new StringBuilder();
            } else
                arg_i.append(rawFormula.charAt(i));
        }

        if (!arg_i.isEmpty()) lexedFormula.add(arg_i.toString());
        return lexedFormula;
    }

    protected Vector<String> createVectorFromArea(@NotNull String s, Sheet sheet) {
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
                if (c.formula() != null)
                    vector.add(c.formula().interpret(sheet));
                else vector.add(String.valueOf(c.value()));
            }

        return vector;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String interpret(Sheet sheet) {
        return interpret(txt, sheet);
    }

    public abstract String interpret(String raw, Sheet sheet);
}

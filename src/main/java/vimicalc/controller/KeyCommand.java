package vimicalc.controller;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;

import java.util.HashMap;

import static vimicalc.controller.Controller.*;
import static vimicalc.utils.Conversions.isNumber;

// Les combos de keys qu'on peut entrer en mode NORMAL
public class KeyCommand {

    // Les fonctions A sont des fonctions avec des arguments, mais les fonctions B, non
    private final char[] Afuncs = {'d', 'y', 'p'};
    private final char[] Bfuncs = {'h', 'j', 'k', 'l', 'a', 'i'};
    private HashMap<Character, String> macros;
    private String expr;

    public KeyCommand() {
        expr = "";
    }

    public void addChar(@NotNull KeyEvent event) {
        char c = event.getText().charAt(0);
        switch (event.getCode()) {
            case LEFT -> c = 'h';
            case DOWN, ENTER -> c = 'j';
            case UP -> c  = 'k';
            case RIGHT, BACK_SPACE -> c = 'l';
            case TAB -> {
                if (event.isShiftDown()) c = 'h';
                else c = 'l';
            }
            case DELETE -> c = 'd';
            case EQUALS -> {
                cellSelector.setSelectedCell(new Cell(
                    cellSelector.getXCoord(),
                    cellSelector.getYCoord(),
                    cellSelector.getSelectedCell().txt()
                ));
                recordedCell.add(cellSelector.getSelectedCell().copy());
                currMode = Mode.FORMULA;
                if (cellSelector.getSelectedCell().formula() == null)
                    cellSelector.getSelectedCell().setFormula(
                        new Formula("", cellSelector.getXCoord(), cellSelector.getYCoord())
                    );
                infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                expr = ""; return;
            }
            case SEMICOLON -> {
                currMode = Mode.COMMAND;
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                infoBar.setCommandTxt(command.getTxt());
                expr = ""; return;
            }
        }
        expr += c;
        verifyExprCompleteness(c);
    }

    public void verifyExprCompleteness(char c) {
        for (char f : Bfuncs)
            if (c == f) evaluate();
        if (expr.length() > 1 && expr.charAt(0) == expr.charAt(1))
            evaluate();
    }

    public void evaluate() {
        switch (expr.charAt(0)) {
            case 'h' -> moveLeft();
            case 'j' -> moveDown();
            case 'k' -> moveUp();
            case 'l' -> moveRight();
            case 'd' -> {
                if (cellSelector.getSelectedCell().txt() == null)
                    infoBar.setInfobarTxt("CAN'T DELETE RIGHT NOW");
                else {
                    recordedCell.add(cellSelector.getSelectedCell());
                    sheet.deleteCell(coordsCell.getCoords());
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    recordedCell.add(cellSelector.getSelectedCell().copy());
                    infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                }
            }
            case 'm' -> sheet.unmergeCells(sheet.findCell(coordsCell.getCoords()));
            case 'u' -> {
                if (!recordedCell.isEmpty() && !(dCounter >= recordedCell.size())) undo();
                else infoBar.setInfobarTxt("CAN'T UNDO RIGHT NOW");
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case 'r' -> {
                if (!recordedCell.isEmpty() && !(dCounter <= 1)) redo();
                else infoBar.setInfobarTxt("CAN'T REDO RIGHT NOW");
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case 'a', 'i' -> {
                cellSelector.setSelectedCell(new Cell(
                    cellSelector.getXCoord(),
                    cellSelector.getYCoord(),
                    cellSelector.getSelectedCell().txt()
                ));
                recordedCell.add(cellSelector.getSelectedCell().copy());
                cellSelector.readCell(camera.picture.data());
                currMode = Mode.INSERT;
                if (cellSelector.getSelectedCell().txt() == null)
                    cellSelector.getSelectedCell().setTxt("");
                cellSelector.draw(gc);
            }
            case 'v' -> {
                currMode = Mode.VISUAL;
                selectedCoords.add(new int[]{cellSelector.getXCoord(), cellSelector.getYCoord()});
            }
        }
        expr = "";
    }
}
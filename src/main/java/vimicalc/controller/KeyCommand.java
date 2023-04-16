package vimicalc.controller;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static vimicalc.controller.Controller.*;
import static vimicalc.utils.Conversions.isNumber;

/* Les combos de keys qu'on peut entrer en mode NORMAL */
public class KeyCommand {

    /* Les fonctions A sont des fonctions qui doivent avoir des arguments, tandis que les fonctions B, non */
//    private final char[] Afuncs = {'d', 'y', 'p', 'q'};
    private final char[] Bfuncs = {'h', 'j', 'k', 'l', 'a', 'i'};
    private final HashMap<Character, ArrayList<String>> macros;
    private String expr;
    private ArrayList<String> currMacro;
    private boolean recordingMacro;

    public KeyCommand() {
        expr = "";
        macros = new HashMap<>();
        recordingMacro = false;
    }

    public void addChar(@NotNull KeyEvent event) {
        char c = 0;
        try {
            c = event.getText().charAt(0);
            System.out.println("Event text: " + c);
        } catch (Exception ignored) {}

        switch (event.getCode()) {
            case LEFT -> c = 'h';
            case DOWN, ENTER -> c = 'j';
            case UP -> c  = 'k';
            case RIGHT, BACK_SPACE -> c = 'l';
            case TAB -> {
                if (event.isShiftDown()) c = 'h';
                else c = 'l';
            }
            case INSERT -> c = 'i';
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

        if (c != 0) expr += c;
        verifyExprCompleteness(c);
    }

    public void verifyExprCompleteness(char c) {
        if (expr.length() > 1) {
            if (expr.charAt(0) == expr.charAt(1))
                evaluate();
            else if (expr.charAt(0) == 'q') {
                currMacro = new ArrayList<>();
                macros.put(expr.charAt(1), currMacro);
                recordingMacro = true;
            }
            else if (expr.charAt(0) == '@')
                runMacro(expr.charAt(1));
        }
        else if (recordingMacro) {
            try {
                if (expr.charAt(0) == 'q') {
                    System.out.println("Recorded macro: " + currMacro);
                    recordingMacro = false;
                    expr = "";
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        for (char f : Bfuncs)
            if (c == f) evaluate();
    }

    private void runMacro(char macroName) {
        try {
            System.out.println("Trying to run a macro...");
            String[] macro = macros.get(macroName).toArray(new String[0]);
            System.out.println("The macro: " + Arrays.toString(macro));
            for (String e : macro) {
                System.out.println("Macro expression: " + e);
                expr = e;
                evaluate();
            }
            System.out.println("Macro execution finished");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            expr = "";
        }
    }

    public void evaluate() {
        byte firstFuncIndex = 0;
        int multiplier = 1;
        String multiplierStr = "";

        System.out.println("recordingMacro = " + recordingMacro);
        if (recordingMacro && expr.charAt(0) != 'q')
            currMacro.add(expr);
        if (expr.equals("")) return;

        for (int i = 0; i < expr.length(); i++) {
            if (isNumber(""+expr.charAt(i))) {
                multiplierStr += expr.charAt(i);
                ++firstFuncIndex;
            }
            else {
                if (firstFuncIndex != 0)
                    multiplier = Integer.parseInt(multiplierStr);
                break;
            }
        }

        for (int i = 0; i < multiplier; i++) {
            switch (expr.charAt(firstFuncIndex)) {
                case '@' -> runMacro(expr.charAt(firstFuncIndex+1));
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
        }
        expr = "";
    }
}
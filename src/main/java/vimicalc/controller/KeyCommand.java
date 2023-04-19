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
import static vimicalc.utils.Booleans.intersect;
import static vimicalc.utils.Conversions.isNumber;

/* Les combos de keys qu'on peut entrer en mode NORMAL */
public class KeyCommand {
    private final char[] F_functions = {'y', 'p', 'd'};  // Fonctions répétables sur des plages de cellules
    private final char[] M_functions = {'h', 'j', 'k', 'l'};
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
        vimicalc.view.InfoBar.keyStroke = expr;
        evaluate(expr);
    }

    private void runMacro(char macroName) {
        try {
            System.out.println("Trying to run a macro...");
            String[] macro = macros.get(macroName).toArray(new String[0]);
            System.out.println("The macro: " + Arrays.toString(macro));
            for (String e : macro) {
                System.out.println("Macro expression: " + e);
                evaluate(e);
            }
            System.out.println("Macro execution finished");
        } catch (Exception e) {
            infoBar.setInfobarTxt("Macro '" + macroName + "' doesn't exist.");
            System.out.println(e.getMessage());
            expr = "";
        }
    }


    public void evaluate(String expr) {
        if (expr.equals("")) return;
        boolean evaluationFinished = false;
        byte frstFuncIndex = 0;
        int multiplier = 1;
        String multiplierStr = "";

        System.out.println("recordingMacro = " + recordingMacro);
        if (recordingMacro && expr.charAt(0) != 'q')
            currMacro.add(expr);

        for (int i = 0; i < expr.length(); i++) {
            if (isNumber(""+expr.charAt(i))) {
                multiplierStr += expr.charAt(i);
                ++frstFuncIndex;
            }
            else {
                System.out.println("Multiplier: ");
                if (frstFuncIndex != 0)
                    multiplier = Integer.parseInt(multiplierStr);
                break;
            }
        }

        if (frstFuncIndex >= expr.length())
            return;
        char func = expr.charAt(frstFuncIndex);

        if (intersect(new char[]{expr.charAt(expr.length()-1)}, M_functions) &&
            intersect(new char[]{expr.charAt(frstFuncIndex)}, F_functions)) {
            return;
        }

        for (int i = 0; i < multiplier; i++) {
            switch (func) {
                case 'q' -> {
                    if (!recordingMacro && expr.length() - 1 > frstFuncIndex) {
                        char arg = expr.charAt(frstFuncIndex +1);
                        infoBar.setInfobarTxt("Recording macro '" + arg + "' ...");
                        currMacro = new ArrayList<>();
                        macros.put(expr.charAt(frstFuncIndex +1), currMacro);
                        recordingMacro = true;
                        evaluationFinished = true;
                    }
                    else if (recordingMacro) {
                        infoBar.setInfobarTxt("Macro recorded");
                        System.out.println("Recorded macro: " + currMacro);
                        recordingMacro = false;
                        evaluationFinished = true;
                    }
                }
                case '@' -> {
                    if (expr.length() > 1) {
                        runMacro(expr.charAt(frstFuncIndex + 1));
                        evaluationFinished = true;
                    }
                }
                case 'h' -> {
                    moveLeft();
                    evaluationFinished = true;
                }
                case 'j' -> {
                    moveDown();
                    evaluationFinished = true;
                }
                case 'k' -> {
                    moveUp();
                    evaluationFinished = true;
                }
                case 'l' -> {
                    moveRight();
                    evaluationFinished = true;
                }
                case 'd' -> {
                    if (expr.charAt(expr.length()-1) == 'd' && expr.length() > 1) {
                        if (cellSelector.getSelectedCell().txt() == null)
                            infoBar.setInfobarTxt("CAN'T DELETE RIGHT NOW");
                        recordedCell.add(cellSelector.getSelectedCell());
                        sheet.deleteCell(coordsCell.getCoords());
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                        recordedCell.add(cellSelector.getSelectedCell().copy());
                        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                        evaluationFinished = true;
                    }
                }
                case 'm' -> {
                    sheet.unmergeCells(sheet.findCell(coordsCell.getCoords()));
                    evaluationFinished = true;
                }
                case 'u' -> {
                    if (!recordedCell.isEmpty() && !(dCounter >= recordedCell.size())) undo();
                    else infoBar.setInfobarTxt("CAN'T UNDO RIGHT NOW");
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    evaluationFinished = true;
                }
                case 'r' -> {
                    if (!recordedCell.isEmpty() && !(dCounter <= 1)) redo();
                    else infoBar.setInfobarTxt("CAN'T REDO RIGHT NOW");
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    evaluationFinished = true;
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
                    evaluationFinished = true;
                }
                case 'v' -> {
                    currMode = Mode.VISUAL;
                    selectedCoords.add(new int[]{cellSelector.getXCoord(), cellSelector.getYCoord()});
                    evaluationFinished = true;
                }
                case 'Z' -> {
                    if (expr.length() > 1) {
                        char arg = expr.charAt(frstFuncIndex + 1);
                        if (arg == 'Q')
                            command = new Command("q", 0, 0);
                        else if (arg == 'Z')
                            command = new Command("wq", 0, 0);
                        else {
                            this.expr = ""; return;
                        }
                        command.interpret(sheet);
                    }
                }
                default -> evaluationFinished = true;
            }
        }
        if (evaluationFinished) this.expr = "";
    }
}
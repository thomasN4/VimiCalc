package vimicalc.controller;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;

import java.util.*;

import static vimicalc.controller.Controller.*;
import static vimicalc.utils.Conversions.isNumber;

/* Les combos de keys qu'on peut entrer en mode NORMAL */
public class KeyCommand {
    // Les fonctions F sont celles qui sont répétables sur des plages de cellules
    private final HashSet<Character> Ffuncs = new HashSet<>(Set.of('d', 'y', 'p'));
    public static final HashSet<Character> Mfuncs = new HashSet<>(Set.of('h', 'j', 'k', 'l'));
    private final HashMap<Character, LinkedList<KeyEvent>> macros;
    private String expr;
    public static LinkedList<KeyEvent> currMacro;
    public static boolean recordingMacro;

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
//                cellSelector.setSelectedCell(new Cell(
//                    cellSelector.getXCoord(),
//                    cellSelector.getYCoord(),
//                    cellSelector.getSelectedCell().txt()
//                ));
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

    private String macroStr(LinkedList<KeyEvent> macro) {
        StringBuilder str = new StringBuilder();
        for (KeyEvent event : macro) {
            str.append(event.getText());
        }
        return str.toString();
    }

    private void runMacro(char macroName) {
        try {
            System.out.println("Trying to run a macro...");
            KeyEvent[] macro = macros.get(macroName).toArray(new KeyEvent[0]);
            System.out.println("The macro: " + macroStr(macros.get(macroName)));
            for (KeyEvent event : macro) onKeyPressed(event);
            System.out.println("Macro execution finished");
        } catch (Exception e) {
            infoBar.setInfobarTxt("Macro '" + macroName + "' doesn't exist.");
            System.out.println(e.getMessage());
            expr = "";
        }
    }

    public int[] parseFIndexAndMult(int subI, @NotNull String expr) {
        StringBuilder multStr = new StringBuilder();
        int i = subI;
        for (; isNumber(""+expr.charAt(i)); i++)
            multStr.append(expr.charAt(i));
        if (i == subI)
            return new int[]{i, 1};
        return new int[]{i, Integer.parseInt(multStr.toString())};
    }

    public void evaluate(@NotNull String expr) {
        if (expr.equals("") || isNumber(""+expr.charAt(expr.length()-1))) return;
        boolean evaluationFinished = false;
        int[] fstFIandM = parseFIndexAndMult(0, expr);  // item 1: indexe de la fonction, item 2: multiplicateur

        if (fstFIandM[0] >= expr.length())
            return;
        char fstFunc = expr.charAt(fstFIandM[0]);

        if (Ffuncs.contains(fstFunc) && Mfuncs.contains(expr.charAt(expr.length()-1))) {
            System.out.println("Executing one of *these* KeyCommand functions...");
            ArrayList<String> tempMacro = new ArrayList<>();
            tempMacro.add(fstFunc+""+fstFunc);

            int[] sndFIandM = parseFIndexAndMult(fstFIandM[0]+1, expr);
            char sndFunc = expr.charAt(sndFIandM[0]);
            if (sndFIandM[0] == expr.length()-1) {
                for (int i = 1; i <= sndFIandM[1]; i++) {
                    tempMacro.add(""+sndFunc);
                    tempMacro.add(fstFunc+""+fstFunc);
                }
            }
            else {
                int[] trdFIandM = parseFIndexAndMult(sndFIandM[0]+1, expr);
                char trdFunc = expr.charAt(expr.length()-1);
                fstFIandM[1] *= trdFIandM[1];
                sndFunc = Character.toLowerCase(sndFunc);
                char invSndFunc = 0;
                switch (sndFunc) {
                    case 'h' -> invSndFunc = 'l';
                    case 'j' -> invSndFunc = 'k';
                    case 'k' -> invSndFunc = 'j';
                    case 'l' -> invSndFunc = 'h';
                }
                for (int i = 1; i <= sndFIandM[1]; i++) {
                    tempMacro.add(""+sndFunc);
                    tempMacro.add(fstFunc+""+fstFunc);
                }
                tempMacro.add(""+trdFunc);
                tempMacro.add(sndFIandM[1]+""+ invSndFunc);
            }

            System.out.println("Special macro: " + tempMacro);
            for (int i = 0; i < fstFIandM[1]; i++) {
                for (int j = 0; j < tempMacro.size(); j++) {
                    if (i == fstFIandM[1]-1 && j == tempMacro.size()-1) {
                        this.expr = "";
                        return;
                    } else evaluate(tempMacro.get(j));
                }
            }
        }

        for (int i = 0; i < fstFIandM[1]; i++) {
            switch (fstFunc) {
                case 'q' -> {
                    if (!recordingMacro && expr.length()-1 > fstFIandM[0]) {
                        char arg = expr.charAt(fstFIandM[0]+1);
                        infoBar.setInfobarTxt("Recording macro '" + arg + "' ...");
                        currMacro = new LinkedList<>();
                        macros.put(expr.charAt(fstFIandM[0]+1), currMacro);
                        recordingMacro = true;
                        evaluationFinished = true;
                    }
                    else if (recordingMacro) {
                        infoBar.setInfobarTxt("Macro recorded");
                        currMacro.removeLast();
                        System.out.println("Recorded macro: " + macroStr(currMacro));
                        recordingMacro = false;
                        evaluationFinished = true;
                    }
                }
                case '@' -> {
                    if (expr.length() > fstFIandM[0]+1) {
                        char arg = expr.charAt(fstFIandM[0]+1);
                        this.expr = "";
                        runMacro(arg);
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
                    if (expr.length() > 1 && expr.charAt(fstFIandM[0]+1) == 'd') {
                        System.out.println("Trying to delete a cell's content...");
                        if (cellSelector.getSelectedCell().txt() == null)
                            infoBar.setInfobarTxt("CAN'T DELETE RIGHT NOW");
                        else {
                            recordedCell.add(cellSelector.getSelectedCell().copy());
                            sheet.deleteCell(cellSelector.getXCoord(), cellSelector.getYCoord());
                            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                            camera.ready();
                            cellSelector.readCell(camera.picture.data());
                            recordedCell.add(cellSelector.getSelectedCell().copy());
                            infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                        }
                        evaluationFinished = true;
                    }
                }
                case 'm' -> {
                    sheet.unmergeCells(sheet.findCell(coordsCell.getCoords()));
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
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
                case 'y' -> {
                    if (expr.length() > 1) {
                        char arg1 = expr.charAt(fstFIandM[0]+1);
                        if (arg1 == 'y' || arg1 == 'd') {
                            if (cellSelector.getSelectedCell().txt() == null)
                                infoBar.setInfobarTxt("CAN'T COPY, CELL IS EMPTY");
                            else {
                                copiedCell.put(cCounter++, cellSelector.getSelectedCell().copy());
                            }
                            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                            camera.ready();
                            cellSelector.readCell(camera.picture.data());
                            if (arg1 == 'd') evaluate("dd");
                            evaluationFinished = true;
                        }
                    }
                }
                case 'p' -> {
                    if (expr.length() > 1 && expr.charAt(fstFIandM[0]+1) == 'p') {
                        if (copiedCell == null) infoBar.setInfobarTxt("CAN'T PASTE, NOTHING HAS BEEN COPIED YET");
                        else {
                            if (visSize != 0) {
                                int tempCounter = 1;
                                while(visSize != 0) {
                                    paste(cCounter - visSize);
                                    /*int xCoord = cellSelector.getXCoord() +
                                            (copiedCell.get(cCounter - tempCounter).xCoord() -
                                                    copiedCell.get(cCounter - (tempCounter * visSize)).xCoord());
                                    int yCoord = cellSelector.getYCoord() +
                                            (copiedCell.get(cCounter - tempCounter).yCoord() -
                                                    copiedCell.get(cCounter - (tempCounter * visSize)).yCoord());
                                    goTo(xCoord, yCoord);
                                    tempCounter++;
                                    visSize--;*/
                                    if (copiedCell.get(cCounter - tempCounter).xCoord() <
                                            copiedCell.get(cCounter - 2).xCoord()) moveLeft();
                                    else if (copiedCell.get(cCounter - tempCounter).xCoord() >
                                            copiedCell.get(cCounter - 2).xCoord()) moveRight();
                                    else if (copiedCell.get(cCounter - tempCounter).yCoord()<
                                            copiedCell.get(cCounter - 2).yCoord()) moveUp();
                                    else if (copiedCell.get(cCounter - tempCounter).yCoord() >
                                            copiedCell.get(cCounter - 2).yCoord()) moveDown();
                                    tempCounter++;
                                    visSize--;
                                }
                                visSize = tempCounter - 1;
                            } else paste(cCounter - 1);
                        }
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                        evaluationFinished = true;
                    }
                }
                case 'a', 'i' -> {
                    reinitialiseCell();
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
                        char arg = expr.charAt(fstFIandM[0]+1);
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

        if (evaluationFinished)
            this.expr = "";
    }
}

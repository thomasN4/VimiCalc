package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.*;
import vimicalc.view.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ESCAPE;
import static vimicalc.Main.arg1;
import static vimicalc.controller.KeyCommand.currMacro;
import static vimicalc.controller.KeyCommand.recordingMacro;

public class Controller implements Initializable {
    private static int CANVAS_W;
    private static int CANVAS_H;
    private static final int DEFAULT_CELL_H = 24;
    private static final int DEFAULT_CELL_W = DEFAULT_CELL_H * 4;
    private static final Color DEFAULT_CELL_C = Color.WHITE;

    /*CD
    private static int MOUSE_X;
    private static int MOUSE_Y;*/

    public static GraphicsContext gc;
    @FXML
    private Canvas canvas;
    protected static LinkedList<Cell> recordedCellStates;
    protected static LinkedList<Cell> undoneCellStates;
    protected static int undoCounter;
    protected static ArrayList<Cell> clipboard;
    protected static Camera camera;
    protected static Command command;
    protected static CoordsInfo coordsInfo;
    private static FirstCol firstCol;
    private static FirstRow firstRow;
    public static HelpMenu helpMenu;
    public static InfoBar infoBar;
    public static KeyStrokeCell keyStrokeCell;
    protected static CellSelector cellSelector;
    protected static ArrayList<int[]> selectedCoords;
    protected static Sheet sheet;
    public static StatusBar statusBar;
    public static KeyCommand keyCommand;
    public static Mode currMode;
    public static HashMap<Character, LinkedList<KeyEvent>> macros;

    /*CD arranger avec les classes moves car sinon cause des bugs en utilisant clavier
    public static void onMouseClicked(@NotNull MouseEvent mouseEvent) {
        MOUSE_X = (int) mouseEvent.getX() / DEFAULT_CELL_W;
        MOUSE_Y = (int) mouseEvent.getY() / DEFAULT_CELL_H - 1;

        System.out.print(" Mouse CLicked "+ MOUSE_X + " // " + MOUSE_Y);

        cellSelector.setX(MOUSE_X);
        cellSelector.setY(MOUSE_Y);

        System.out.print(" Selected Cell "+ cellSelector.getX() + " // " + cellSelector.getY());
    }
    */

    public static boolean goingToMergeStart = false;
    private static void maybeGoToMergeStart() {
        System.out.println(cellSelector.getSelectedCell().getMergeDelimiter());
        if (cellSelector.getSelectedCell().getMergeDelimiter() != null &&
            !cellSelector.getSelectedCell().isMergeStart() &&
            !goingToMergeStart) {
            Cell currMergeStart = cellSelector.getSelectedCell().getMergeDelimiter();
            goingToMergeStart = true;
            goTo(currMergeStart.xCoord(), currMergeStart.yCoord());
            goingToMergeStart = false;
        }
    }
    protected static void moveLeft() {
        if (cellSelector.getXCoord() != 1) {
            cellSelector.updateXCoord(-1);
            cellSelector.readCell(camera.picture.data());
        } else {
            infoBar.setInfobarTxt("CAN'T GO LEFT");
            cellSelector.readCell(camera.picture.data());
            return;
        }
        cellContentToIBar();
        if (cellSelector.getX() != firstCol.getW()) {
            cellSelector.updateX(-cellSelector.getW());
            if (cellSelector.getX() < firstCol.getW()) {
                while (cellSelector.getX() != firstCol.getW()) {
                    cellSelector.updateX(1);
                    camera.updateAbsX(-1);
                }
                camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                firstRow.draw(gc);
            }
        }
        else {
            camera.updateAbsX(-cellSelector.getW());
            if (camera.getAbsX() < firstCol.getW()) {
                infoBar.setInfobarTxt("CAN'T GO LEFT");
                while (camera.getAbsX() != firstCol.getW())
                    camera.updateAbsX(1);
            }
            camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            firstRow.draw(gc);
            cellSelector.readCell(camera.picture.data());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        if (currMode != Mode.VISUAL) maybeGoToMergeStart();
    }
    protected static void moveDown() {
        int prevH;
        if (!cellSelector.getSelectedCell().isMergeStart() || currMode == Mode.VISUAL)
            prevH = cellSelector.getH();
        else {
            prevH = cellSelector.getMergedH();
            cellSelector.updateYCoord(
                cellSelector.getSelectedCell().getMergeDelimiter().yCoord() -
                cellSelector.getSelectedCell().yCoord()
            );
        }
        cellSelector.updateYCoord(1);
        cellSelector.readCell(camera.picture.data());
        cellContentToIBar();
        if (cellSelector.getY() + cellSelector.getH() != statusBar.getY()) {
            cellSelector.updateY(prevH);
            if (cellSelector.getY() + cellSelector.getH() > statusBar.getY()) {
                while (cellSelector.getY() + cellSelector.getH() != statusBar.getY()) {
                    cellSelector.updateY(-1);
                    camera.updateAbsY(1);
                }
                camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                firstCol.draw(gc);
            }
        }
        else {
            camera.updateAbsY(prevH);
            camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            firstCol.draw(gc);
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        if (currMode != Mode.VISUAL) maybeGoToMergeStart();
    }
    protected static void moveUp() {
        if (cellSelector.getYCoord() != 1) {
            cellSelector.updateYCoord(-1);
            cellSelector.readCell(camera.picture.data());
        } else {
            infoBar.setInfobarTxt("CAN'T GO UP");
            cellSelector.readCell(camera.picture.data());
            return;
        }
        cellContentToIBar();
        if (cellSelector.getY() != DEFAULT_CELL_H) {
            cellSelector.updateY(-cellSelector.getH());
            if (cellSelector.getY() < DEFAULT_CELL_H) {
                while (cellSelector.getY() != DEFAULT_CELL_H) {
                    cellSelector.updateY(1);
                    camera.updateAbsY(-1);
                }
                camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                firstCol.draw(gc);
            }
        }
        else {
            camera.updateAbsY(-cellSelector.getH());
            if (camera.getAbsY() < DEFAULT_CELL_H) {
                infoBar.setInfobarTxt("CAN'T GO UP");
                while (camera.getAbsY() != DEFAULT_CELL_H)
                    camera.updateAbsY(1);
            }
            camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            firstCol.draw(gc);
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        if (currMode != Mode.VISUAL) maybeGoToMergeStart();
    }
    protected static void moveRight() {
        int prevW;
        if (!cellSelector.getSelectedCell().isMergeStart() || currMode == Mode.VISUAL)
            prevW = cellSelector.getW();
        else {
            prevW = cellSelector.getMergedW();
            cellSelector.updateXCoord(
                cellSelector.getSelectedCell().getMergeDelimiter().xCoord() -
                cellSelector.getSelectedCell().xCoord()
            );
        }
        cellSelector.updateXCoord(1);
        cellSelector.readCell(camera.picture.data());
        if (cellSelector.getX() + cellSelector.getW() != CANVAS_W) {
            cellSelector.updateX(prevW);
            if (cellSelector.getX() + cellSelector.getW() > CANVAS_W) {
                while (cellSelector.getX() + cellSelector.getW() != CANVAS_W) {
                    cellSelector.updateX(-1);
                    camera.updateAbsX(1);
                }
                camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                firstRow.draw(gc);
            }
        }
        else {
            camera.updateAbsX(prevW);
            camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            firstRow.draw(gc);
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        cellContentToIBar();
        if (currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    protected static void cellContentToIBar() {
        if (cellSelector.getSelectedCell().value() != null)
            infoBar.setInfobarTxt("(="+cellSelector.getSelectedCell().value()+")");
        else {
            infoBar.setInfobarTxt(null);
            return;
        }
        if (cellSelector.getSelectedCell().formula() != null)
            infoBar.setInfobarTxt(
                infoBar.getInfobarTxt() + " " +
                "(f:"+cellSelector.getSelectedCell().formula().getTxt()+")"
            );
    }

    protected static void removeUltCStates() {
        Cell last = recordedCellStates.getLast().copy();
        recordedCellStates.removeLast();
        while (undoCounter != 0) {
            recordedCellStates.removeLast();
            undoCounter--;
        }
        recordedCellStates.add(last);
        undoneCellStates = new LinkedList<>();

        System.out.println("Recorded cell states: ");
        recordedCellStates.forEach(c -> System.out.println("xC = " + c.xCoord() + ", yC = " + c.yCoord()));
    }

    protected static void undo() {
        int listIndex = recordedCellStates.size() - 1 - undoCounter;
        undoCounter++;

        Cell substitute = recordedCellStates.get(listIndex).copy();
        goTo(substitute.xCoord(), substitute.yCoord());
        recordedCellStates.set(listIndex, sheet.findCell(substitute.xCoord(), substitute.yCoord()).copy());
        undoneCellStates.add(substitute);
        sheet.deleteCell(substitute.xCoord(), substitute.yCoord());

        if (substitute.formula() != null) {
            Formula f = substitute.formula();
            try {
                cellSelector.getSelectedCell().setFormulaResult(f.interpret(sheet), f);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Something went very wrong.");
            }
        } else cellSelector.setSelectedCell(substitute);

        cellContentToIBar();
        sheet.addCell(cellSelector.getSelectedCell());
        sheet.getCells().forEach(Cell::isEmpty);
    }

    protected static void redo() {
        int listIndex = recordedCellStates.size() - undoCounter;
        undoCounter--;

        Cell substitute = recordedCellStates.get(listIndex).copy();
        goTo(substitute.xCoord(), substitute.yCoord());
        recordedCellStates.set(listIndex, undoneCellStates.getLast().copy());
        undoneCellStates.removeLast();
        sheet.deleteCell(substitute.xCoord(), substitute.yCoord());

        if (substitute.formula() != null) {
            Formula f = substitute.formula();
            try {
                cellSelector.getSelectedCell().setFormulaResult(f.interpret(sheet), f);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Something went very wrong.");
            }
        } else cellSelector.setSelectedCell(substitute);

        cellContentToIBar();
        sheet.addCell(cellSelector.getSelectedCell());
        sheet.getCells().forEach(Cell::isEmpty);
    }

    public static int staticPrevXC, staticPrevYC;
    protected static void goTo(int xCoord, int yCoord) {
        while (xCoord - cellSelector.getXCoord() > 0)
            moveRight();
        while (xCoord - cellSelector.getXCoord() < 0)
            moveLeft();
        while (yCoord - cellSelector.getYCoord() > 0)
            moveDown();
        while (yCoord - cellSelector.getYCoord() < 0)
            moveUp();
        coordsInfo.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        updateVisualState();
    }
    protected static void goToAndRemember(int xCoord, int yCoord, int prevXC, int prevYC) {
        staticPrevXC = prevXC;
        staticPrevYC = prevYC;
        goTo(xCoord, yCoord);
    }

    protected static void paste(int index) {
        System.out.println("Pasting cell " + clipboard.get(index));
        if (clipboard.get(index).formula() != null) {
            Formula f = new Formula(
                clipboard.get(index).formula().getTxt(),
                cellSelector.getXCoord(),
                cellSelector.getYCoord()
            );
            try {
                recordedCellStates.add(cellSelector.getSelectedCell().copy());
                sheet.deleteCell(cellSelector.getXCoord(), cellSelector.getYCoord());
                cellSelector.getSelectedCell().setFormulaResult(f.interpret(sheet), f);
            } catch (Exception e) {
                infoBar.setInfobarTxt(e.getMessage());
                return;
            }
        } else {
            recordedCellStates.add(cellSelector.getSelectedCell().copy());
            sheet.deleteCell(cellSelector.getXCoord(), cellSelector.getYCoord());
            cellSelector.setSelectedCell(clipboard.get(index).copy());
        }
        cellSelector.getSelectedCell().setXCoord(cellSelector.getXCoord());
        cellSelector.getSelectedCell().setYCoord(cellSelector.getYCoord());
        cellContentToIBar();
        sheet.addCell(cellSelector.getSelectedCell());
        if (undoCounter != 0) removeUltCStates();
    }

    public static void onKeyPressed(@NotNull KeyEvent event) {
        keyStrokeCell.setKeyStroke(event.getCode().toString());
        if (recordingMacro) currMacro.add(event);
        if (currMode == Mode.NORMAL) keyCommand.addChar(event);
        else {
            switch (currMode) {
                case COMMAND -> commandInput(event);
                case FORMULA -> formulaInput(event);
                case HELP -> {
                    camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
                    updateVisualState();
                    cellSelector.readCell(camera.picture.data());
                    cellSelector.draw(gc);
                    helpMenu.navigate(event);
                    infoBar.setInfobarTxt(helpMenu.percentage());
                    infoBar.draw(gc);
                    if (event.getCode() == ESCAPE) {
                        camera.ready();
                        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
                        cellSelector.readCell(camera.picture.data());
                        cellSelector.draw(gc);
                        cellContentToIBar();
                        updateVisualState();
                    }
                }
                case INSERT -> textInput(event);
                case VISUAL -> {
                    goingToMergeStart = true;
                    visualSelection(event);
                }
            }
        }

        System.out.println("     sC.x: "+cellSelector.getX()     +", yCoord: "+cellSelector.getY());
        System.out.println("sC.xCoord: "+cellSelector.getXCoord()+", sC.yCoord: "+cellSelector.getYCoord());
        System.out.println(" cam.absX: "+camera.getAbsX()        +", cam.absY: "+camera.getAbsY());
        System.out.println("    Cells: "+sheet.getCells());
        System.out.println("Selected cell: " + cellSelector.getSelectedCell());
        System.out.println("========================================");

        if (currMode == Mode.NORMAL) {
            cellSelector.draw(gc);
            coordsInfo.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        }
        else if (currMode == Mode.VISUAL) {
            System.out.println("Selected coords = {");
            selectedCoords.forEach(c -> {
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                System.out.println('\t' + Arrays.toString(c));
            });
            camera.ready();
            System.out.println('}');
        }
        if (currMode != Mode.HELP) updateVisualState();
        keyStrokeCell.draw(gc);
    }

    protected static void updateVisualState() {
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.draw(gc);
        infoBar.draw(gc);
        coordsInfo.draw(gc);
    }

    protected static void commandInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (infoBar.isEnteringCommandInVISUAL()) {
                    selectedCoords = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);
                }
                currMode = Mode.NORMAL;
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
            }
            case ENTER -> {
                if (command.getTxt().equals("h") ||
                    command.getTxt().equals("help") ||
                    command.getTxt().equals("?")) {
                    try {
                        command.interpret(sheet);
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                        infoBar.draw(gc);
                    }
                    onKeyPressed(event);
                    return;
                }
                if (infoBar.isEnteringCommandInVISUAL()) {
                    try {
                        selectedCoords = new ArrayList<>();
                        infoBar.setEnteringCommandInVISUAL(false);

                        StringBuilder destinationCoord = new StringBuilder();
                        int i;
                        for (i = command.getTxt().length() - 1; i > 0; i--) {
                            if (command.getTxt().charAt(i) == ' ') break;
                            destinationCoord.append(command.getTxt().charAt(i));
                        }
                        destinationCoord.reverse();

                        Cell c = sheet.findCell(destinationCoord.toString());
                        recordedCellStates.add(c.copy());
                        Formula f = new Formula(
                            coordsInfo.getCoords() + ' ' + command.getTxt().substring(0, i),
                            c.xCoord(),
                            c.yCoord()
                        );
                        c = new Cell(
                            c.xCoord(),
                            c.yCoord(),
                            f.interpret(sheet),
                            f
                        );
                        sheet.addCell(c);
                        goTo(c.xCoord(), c.yCoord());
                        command.commandExists = true;
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                    }
                }
                currMode = Mode.NORMAL;
                int prevXC = cellSelector.getXCoord(), prevYC = cellSelector.getYCoord();
                if (cellSelector.getX() == 0) moveRight();
                else moveLeft();
                if (cellSelector.getY() == 0) moveDown();
                else moveUp();
                try {
                    command.interpret(sheet);
                } catch (Exception e) {
                    infoBar.setInfobarTxt(e.getMessage());
                }
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
                goTo(prevXC, prevYC);
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                if (!command.commandExists()) infoBar.setInfobarTxt("COMMAND OR FILE DOES NOT EXIST");
                System.out.println(recordedCellStates.size());
            }
            case BACK_SPACE -> {
                if (command.getTxt().equals("")) {
                    infoBar.setInfobarTxt("COMMAND IS EMPTY");
                } else {
                    command.setTxt(command.getTxt().substring(0, command.getTxt().length()-1));
                }
            }
            default -> {
                command.setTxt(command.getTxt() + event.getText());
                infoBar.setCommandTxt(command.getTxt() + event.getText());
            }
        }
        if(currMode == Mode.COMMAND || currMode == Mode.VISUAL)
            infoBar.setCommandTxt(command.getTxt());
    }

    public static String multiplierForVISUAL = "";
    private static void visualSelection(@NotNull KeyEvent event) {
        if (!multiplierForVISUAL.equals("") && (
                event.getText().equals("h") ||
                event.getText().equals("j") ||
                event.getText().equals("k") ||
                event.getText().equals("l"))) {
            int multiplier = Integer.parseInt(multiplierForVISUAL);
            multiplierForVISUAL = "";
            for (int i = 1; i < multiplier; ++i)
                onKeyPressed(event);
        }

        if (infoBar.isEnteringCommandInVISUAL()) {
            commandInput(event);
            return;
        }

        switch (event.getCode()) {
            case DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4, DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9 ->
                multiplierForVISUAL += event.getText();
            case D -> {
                selectedCoords.forEach(coord -> sheet.deleteCell(coord[0], coord[1]));
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case Y -> {
                clipboard.clear();
                selectedCoords.forEach(coord -> {
                    goTo(coord[0], coord[1]);
                    clipboard.add(cellSelector.getSelectedCell().copy());
                });
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case M -> {
                boolean mergedCsInside = false;

                for (int[] coord : selectedCoords) {
                    Cell c = sheet.findCell(coord[0], coord[1]);
                    if (c.getMergeDelimiter() != null) {
                        sheet.unmergeCells(c);
                        if (!mergedCsInside) mergedCsInside = true;
                    }
                }

                if (!mergedCsInside) {
                    System.out.println("Merging cells...");
                    int maxXC = Integer.MIN_VALUE, minXC = Integer.MAX_VALUE,
                        maxYC = Integer.MIN_VALUE, minYC = Integer.MAX_VALUE;
                    for (int[] c : selectedCoords) {
                        if (c[0] > maxXC) maxXC = c[0];
                        if (c[0] < minXC) minXC = c[0];
                        if (c[1] > maxYC) maxYC = c[1];
                        if (c[1] < minYC) minYC = c[1];
                    }

                    Cell mergeStart = sheet.findCell(minXC, minYC);
                    Cell mergeEnd = sheet.findCell(maxXC, maxYC);
                    if (mergeStart.isEmpty()) sheet.addCell(mergeStart);
                    if (mergeEnd.isEmpty()) sheet.addCell(mergeEnd);
                    else mergeEnd = new Cell(mergeEnd.xCoord(), mergeEnd.yCoord());
                    mergeStart.setMergeStart(true);
                    mergeStart.mergeWith(mergeEnd);
                    mergeEnd.mergeWith(mergeStart);

                    for (int i = mergeStart.xCoord(); i <= mergeEnd.xCoord() ; i++) {
                        for (int j = mergeStart.yCoord(); j <= mergeEnd.yCoord(); j++) {
                            Cell c = sheet.findCell(i, j);
                            if (c != mergeStart && c != mergeEnd) {
                                if (!c.isEmpty())
                                    c = new Cell(c.xCoord(), c.yCoord());
                                sheet.addCell(c);
                                c.mergeWith(mergeStart);
                            }
                        }
                    }

                    selectedCoords = new ArrayList<>();
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    currMode = Mode.NORMAL;
                    cellSelector.readCell(camera.picture.data());
                    goingToMergeStart = false;
                    maybeGoToMergeStart();
                }
            }
            case ESCAPE -> {
                goingToMergeStart = false;
                currMode = Mode.NORMAL;
                selectedCoords = new ArrayList<>();
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
                cellSelector.draw(gc);
            }
            case SEMICOLON -> {
                goingToMergeStart = false;
                infoBar.setEnteringCommandInVISUAL(true);
                infoBar.setCommandTxt(command.getTxt());
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
            }
            default -> {
                int originalXC = selectedCoords.get(0)[0];
                int originalYC = selectedCoords.get(0)[1];

                int prevXC = cellSelector.getXCoord();
                int prevYC = cellSelector.getYCoord();

                int maxXC;
                int minXC;
                int maxYC;
                int minYC;
                if (selectedCoords.size() > 1) {
                    maxXC = Integer.MIN_VALUE;
                    minXC = Integer.MAX_VALUE;
                    maxYC = Integer.MIN_VALUE;
                    minYC = Integer.MAX_VALUE;
                    for (int[] c : selectedCoords) {
                        if (c[0] > maxXC) maxXC = c[0];
                        if (c[0] < minXC) minXC = c[0];
                        if (c[1] > maxYC) maxYC = c[1];
                        if (c[1] < minYC) minYC = c[1];
                    }
                } else {
                    maxXC = originalXC;
                    minXC = maxXC;
                    maxYC = originalYC;
                    minYC = maxYC;
                }
                System.out.println("maxXC: " + maxXC);
                System.out.println("minXC: " + minXC);
                System.out.println("maxYC: " + maxYC);
                System.out.println("minYC: " + minYC);

                if (maxXC > camera.picture.metadata().getMaxXC() ||
                    maxYC > camera.picture.metadata().getMaxYC()) {
                    if (maxXC > camera.picture.metadata().getMaxXC()) camera.picture.metadata().setMaxXC(maxXC);
                    if (maxYC > camera.picture.metadata().getMaxYC()) camera.picture.metadata().setMaxYC(maxYC);
                    camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                }

                switch (event.getCode()) {
                    case H, LEFT, BACK_SPACE -> moveLeft();
                    case J, DOWN, ENTER -> moveDown();
                    case K, UP -> moveUp();
                    case L, RIGHT, TAB, SPACE -> moveRight();
                }
                int currXC = cellSelector.getXCoord();
                int currYC = cellSelector.getYCoord();
                System.out.println("currXC: " + currXC);
                System.out.println("currYC: " + currYC);

                System.out.println("originalXC = " + originalXC);
                System.out.println("originalYC = " + originalYC);

                if (currXC >= originalXC && currYC >= originalYC) {
                    if (currXC > prevXC) {
                        addSCs(true, currXC, minYC, maxYC);
                        maxXC = currXC;
                    }
                    else if (currXC < prevXC) {
                        purgeSCs(prevXC, -1);
                        maxXC = currXC;
                    }
                    else if (currYC > prevYC) {
                        addSCs(false, currYC, minXC, maxXC);
                        maxYC = currYC;
                    }
                    else if (currYC < prevYC){
                        purgeSCs(-1, prevYC);
                        maxYC = currYC;
                    }
                }
                else if (currXC >= originalXC) {
                    if (currXC > prevXC) {
                        addSCs(true, currXC, minYC, maxYC);
                        maxXC = currXC;
                    }
                    else if (currXC < prevXC) {
                        purgeSCs(prevXC, -1);
                        maxXC = currXC;
                    }
                    else if (currYC < prevYC) {
                        addSCs(false, currYC, minXC, maxXC);
                        minYC = currYC;
                    }
                    else if (currYC > prevYC) {
                        purgeSCs(-1, prevYC);
                        minYC = currYC;
                    }
                }
                else if (currYC >= originalYC) {
                    if (currXC < prevXC) {
                        addSCs(true, currXC, minYC, maxYC);
                        minXC = currXC;
                    }
                    else if (currXC > prevXC) {
                        purgeSCs(prevXC, -1);
                        minXC = currXC;
                    }
                    else if (currYC > prevYC) {
                        addSCs(false, currYC, minXC, maxXC);
                        maxYC = currYC;
                    }
                    else if (currYC < prevYC) {
                        purgeSCs(-1, prevYC);
                        maxYC = currYC;
                    }
                }
                else {
                    if (currXC < prevXC) {
                        addSCs(true, currXC, minYC, maxYC);
                        minXC = currXC;
                    }
                    else if (currXC > prevXC) {
                        purgeSCs(prevXC, -1);
                        minXC = currXC;
                    }
                    else if (currYC < prevYC) {
                        addSCs(false, currYC, minXC, maxXC);
                        minYC = currYC;
                    }
                    else if (currYC > prevYC) {
                        purgeSCs(-1, prevYC);
                        minYC = currYC;
                    }
                }

                if (currXC == originalXC && currXC > prevXC)
                    purgeSCs(prevXC, -1);
                else if (currYC == originalYC && currYC > prevYC)
                    purgeSCs(-1, prevYC);

                coordsInfo.setCoords(maxXC, minXC, maxYC, minYC);
            }
        }
    }
    private static void addSCs(boolean isAddingCol, int currC, int minC, int maxC) {
        if (isAddingCol) for (int i = minC; i <= maxC; i++) {
            selectedCoords.add(new int[]{currC, i});
            System.out.println(currC + ", " + i);
        }
        else for (int i = minC; i <= maxC; i++) {
            selectedCoords.add(new int[]{i, currC});
            System.out.println(currC + ", " + i);
        }
    }
    private static void purgeSCs(int col, int row) {
        if (col != -1)
            selectedCoords.removeIf(c -> c[0] == col);
        else
            selectedCoords.removeIf(c -> c[1] == row);
    }

    protected static void setSCTxtForTextInput() {  //improve for later use maybe, or delete
        if (cellSelector.getSelectedCell().value() != null &&
            !(""+cellSelector.getSelectedCell().value()).endsWith(".0"))
            cellSelector.getSelectedCell().setTxt(""+cellSelector.getSelectedCell().value());
        if (cellSelector.getSelectedCell().txt() == null)
            cellSelector.getSelectedCell().setTxt("");
    }
    private static void textInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (event.isShiftDown()) {
                    cellSelector.readCell(camera.picture.data());
                    currMode = Mode.NORMAL;
                    recordedCellStates.removeLast();
                }
                else {
                    if (cellSelector.getSelectedCell().txt() == null) {
                        infoBar.setInfobarTxt("CELL IS EMPTY");
                    } else {
                        if (undoCounter != 0) removeUltCStates();
                        if (cellSelector.getSelectedCell().formula() != null)
                            cellSelector.getSelectedCell().setFormula(null);
                        cellSelector.getSelectedCell().correctTxt(
                            cellSelector.getSelectedCell().txt()
                        );
                        sheet.addCell(cellSelector.getSelectedCell().copy());
                    }
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    currMode = Mode.NORMAL;
                }
            }
            case H, J, K, L -> {
                if (event.isAltDown()) {
                    if (cellSelector.getSelectedCell().txt() == null) {
                        infoBar.setInfobarTxt("CELL IS EMPTY");
                    } else {
                        if (undoCounter != 0) removeUltCStates();
                        if (cellSelector.getSelectedCell().formula() != null)
                            cellSelector.getSelectedCell().setFormula(null);
                        cellSelector.getSelectedCell().correctTxt(
                            cellSelector.getSelectedCell().txt()
                        );
                        sheet.addCell(cellSelector.getSelectedCell().copy());
                    }
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    switch (event.getCode()) {
                        case H -> moveLeft();
                        case J -> moveDown();
                        case K -> moveUp();
                        case L -> moveRight();
                    }
                    recordedCellStates.add(cellSelector.getSelectedCell().copy());
                    setSCTxtForTextInput();
                    cellSelector.draw(gc);
                } else {
                    cellSelector.draw(gc, event.getText());
                }
            }
            case LEFT, DOWN, UP, RIGHT, ENTER, TAB -> {
                if (cellSelector.getSelectedCell().txt() == null) {
                    infoBar.setInfobarTxt("CELL IS EMPTY");
                } else {
                    if (undoCounter != 0) removeUltCStates();
                    if (cellSelector.getSelectedCell().formula() != null)
                        cellSelector.getSelectedCell().setFormula(null);
                    cellSelector.getSelectedCell().correctTxt(
                        cellSelector.getSelectedCell().txt()
                    );
                    sheet.addCell(cellSelector.getSelectedCell().copy());
                }
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                switch (event.getCode()) {
                    case LEFT -> moveLeft();
                    case DOWN, ENTER -> moveDown();
                    case UP -> moveUp();
                    case RIGHT, TAB -> moveRight();
                }
                currMode = Mode.NORMAL;
            }
            case BACK_SPACE -> {
                if (cellSelector.getSelectedCell().txt().equals(""))
                    infoBar.setInfobarTxt("CELL IS EMPTY");
                else {
                    cellSelector.getSelectedCell().setTxt(
                        cellSelector.getSelectedCell().txt().substring(0,
                            cellSelector.getSelectedCell().txt().length() - 1)
                    );
                    cellSelector.draw(gc);
                }
            }
            default -> cellSelector.draw(gc, event.getText());
        }
    }

    private static void formulaInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.readCell(camera.picture.data());
                currMode = Mode.NORMAL;
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                recordedCellStates.removeLast();
            }
            case H, J, K, L -> {
                if (event.isAltDown()) {
                    try {
                        if (cellSelector.getSelectedCell().formula().getTxt().isEmpty())
                            infoBar.setInfobarTxt("CELL IS EMPTY");
                        else {
                            if (undoCounter != 0) removeUltCStates();
                            cellSelector.getSelectedCell().setFormulaResult(
                                cellSelector.getSelectedCell().formula().interpret(sheet),
                                cellSelector.getSelectedCell().formula()
                            );
                            sheet.addCell(cellSelector.getSelectedCell());
                        }
                        cellContentToIBar();
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        if (cellSelector.getSelectedCell().txt() == null) recordedCellStates.removeLast();
                        switch (event.getCode()) {
                            case H -> moveLeft();
                            case J -> moveDown();
                            case K -> moveUp();
                            case L -> moveRight();
                        }
                        recordedCellStates.add(cellSelector.getSelectedCell().copy());
                        currMode = Mode.FORMULA;
                        if (cellSelector.getSelectedCell().formula() == null)
                            cellSelector.getSelectedCell().setFormula(
                                    new Formula("", cellSelector.getXCoord(), cellSelector.getYCoord())
                            );
                        infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                    }
                } else {
                    cellSelector.getSelectedCell().formula().setTxt(
                        cellSelector.getSelectedCell().formula().getTxt() +
                            event.getText()
                    );
                    infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                }
            }
            case ENTER -> {
                try {
                    if (cellSelector.getSelectedCell().formula().getTxt().isEmpty())
                        infoBar.setInfobarTxt("CELL IS EMPTY");
                    else {
                        if (undoCounter != 0) removeUltCStates();
                        cellSelector.getSelectedCell().setFormulaResult(
                            cellSelector.getSelectedCell().formula().interpret(sheet),
                            cellSelector.getSelectedCell().formula()
                        );
                        sheet.addCell(cellSelector.getSelectedCell());
                    }
                    cellContentToIBar();
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    currMode = Mode.NORMAL;
                    cellSelector.readCell(camera.picture.data());
                    if (cellSelector.getSelectedCell().txt() == null) recordedCellStates.removeLast();
                } catch (Exception e) {
                    infoBar.setInfobarTxt(e.getMessage());
                }
            }
            case BACK_SPACE -> {
                if (cellSelector.getSelectedCell().formula().getTxt().isEmpty()) {
                    infoBar.setInfobarTxt("CELL IS EMPTY");
                } else {
                    cellSelector.getSelectedCell().formula().setTxt(
                        cellSelector.getSelectedCell().formula().getTxt().substring(
                            0, cellSelector.getSelectedCell().formula().getTxt().length()-1
                    ));
                    infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                }
            }
            default -> {
                cellSelector.getSelectedCell().formula().setTxt(
                    cellSelector.getSelectedCell().formula().getTxt() +
                        event.getText()
                );
                infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        sheet = new Sheet();
        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();
        macros = new HashMap<>();
        helpMenu = new HelpMenu(gc);
        reset(new HashMap<>(), new HashMap<>());
        if (arg1 != null) {
            try {
                sheet.readFile(arg1);
                updateVisualState();
            } catch (Exception e) {
                if (e.getMessage().equals("New file")) {
                    updateVisualState();
                    infoBar.setInfobarTxt("New file");
                    infoBar.draw(gc);
                }
                else infoBar.setInfobarTxt(e.getMessage());
            }
        }
    }

    public static void reset(HashMap<Integer, Integer> xOffsets, HashMap<Integer, Integer> yOffsets) {
        camera = new Camera(
            DEFAULT_CELL_W/2,
            DEFAULT_CELL_H,
            CANVAS_W-DEFAULT_CELL_W/2,
            CANVAS_H-3*DEFAULT_CELL_H-4,
            DEFAULT_CELL_C,
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            xOffsets,
            yOffsets
        );
        selectedCoords = new ArrayList<>();
        camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        cellSelector = new CellSelector(
            camera.picture.metadata().getCellAbsXs()[2],
            camera.picture.metadata().getCellAbsYs()[2],
            camera.picture.metadata().getCellAbsXs()[2] - camera.picture.metadata().getCellAbsYs()[1],
            camera.picture.metadata().getCellAbsYs()[2] - camera.picture.metadata().getCellAbsYs()[1],
            new Color(0.0, 0.67, 1, 1),
//            Color.LIMEGREEN,
            camera.picture.metadata()
        );
        coordsInfo = new CoordsInfo(
            CANVAS_W,
            CANVAS_H-2*DEFAULT_CELL_H-4,
            DEFAULT_CELL_H + 4
        );
        firstCol = new FirstCol(
            0,
            DEFAULT_CELL_H,
            DEFAULT_CELL_W/2,
            CANVAS_H-3*DEFAULT_CELL_H-4,
            Color.LIGHTBLUE,
            camera.picture.metadata()
        );
        firstRow = new FirstRow(
            DEFAULT_CELL_W/2,
            0,
            CANVAS_W-DEFAULT_CELL_W/2,
            DEFAULT_CELL_H,
            Color.LIGHTBLUE,
            camera.picture.metadata()
        );
        infoBar = new InfoBar(
            0,
            CANVAS_H-DEFAULT_CELL_H,
            CANVAS_W,
            DEFAULT_CELL_H,
            DEFAULT_CELL_C
        );
        statusBar = new StatusBar(
            0,
            CANVAS_H-2*DEFAULT_CELL_H-4,
            CANVAS_W,
            DEFAULT_CELL_H+4,
            Color.LIGHTGREEN
        );
        keyStrokeCell = new KeyStrokeCell(
            0,
            0,
            DEFAULT_CELL_W/2,
            DEFAULT_CELL_H,
            Color.LIGHTGREEN
        );

        currMode = Mode.NORMAL;
        keyCommand = new KeyCommand();
        command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
        staticPrevXC = cellSelector.getXCoord();
        staticPrevYC = cellSelector.getYCoord();
        recordedCellStates = new LinkedList<>();
        undoneCellStates = new LinkedList<>();
        undoCounter = 0;
        clipboard = new ArrayList<>();

        camera.ready();
        keyStrokeCell.draw(gc);
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.draw(gc);
        coordsInfo.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        coordsInfo.draw(gc);
        cellSelector.readCell(camera.picture.data());
        cellSelector.draw(gc);
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        infoBar.draw(gc);
        sheet.setPicMetadata(camera.picture.metadata());
    }
}
package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Cell;
import vimicalc.model.Command;
import vimicalc.model.Formula;
import vimicalc.model.Sheet;
import vimicalc.view.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static int CANVAS_W;
    private static int CANVAS_H;
    private static final int DEFAULT_CELL_H = 23;
    private static final int DEFAULT_CELL_W = DEFAULT_CELL_H * 4;
    private static final Color DEFAULT_CELL_C = Color.LIGHTGRAY;

    //CD
//    private static int MOUSE_X;
//    private static int MOUSE_Y;

    private static final String[] MODE = {"[COMMAND]", "[FORMULA]", "[INSERT]", "[NORMAL]", "[VISUAL]"};
    @FXML
    private Canvas canvas;
    private static final LinkedList<Cell> recordedCell = new LinkedList<>();
    private static final LinkedList<Formula> recordedFormula = new LinkedList<>();
    private static int dCounter = 1;
    private static int fCounter = 1;
    private static Camera camera;
    private static Command command;
    private static CoordsCell coordsCell;
    private static FirstCol firstCol;
    private static FirstRow firstRow;
    private static GraphicsContext gc;
    private static InfoBar infoBar;
    private static CellSelector cellSelector;
    private static ArrayList<int[]> selectedCoords;
    private static Sheet sheet;
    public static StatusBar statusBar;

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

    private static void maybeGoToMergeStart(Cell prevCell) {
        if (cellSelector.getSelectedCell().getMergeDelimiter() != null &&
            !cellSelector.getSelectedCell().getMergeDelimiter().isMergeStart()) {
            Cell cMergeStart = cellSelector.getSelectedCell().getMergeDelimiter(),
                 pMergeDel = prevCell.getMergeDelimiter();
            if (pMergeDel == null || pMergeDel != cMergeStart)
                goTo(cMergeStart.xCoord(), cMergeStart.yCoord());
        }
    }
    private static void moveLeft() {
        Cell prevCell = cellSelector.getSelectedCell().copy();
        if (cellSelector.getXCoord() != 1) {
            cellSelector.updateXCoord(-1);
            cellSelector.readCell(camera.picture.data());
        }
        if (cellSelector.getX() != cellSelector.getW()) {
            cellSelector.updateX(-cellSelector.getW());
            if (cellSelector.getX() < DEFAULT_CELL_W) {
                while (cellSelector.getX() != DEFAULT_CELL_W) {
                    cellSelector.updateX(1);
                    camera.updateAbsX(-1);
                }
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                firstRow.draw(gc);
            }
        }
        else {
            camera.updateAbsX(-cellSelector.getW());
            if (camera.getAbsX() < DEFAULT_CELL_W) {
                infoBar.setInfobarTxt("CAN'T GO LEFT");

                while (camera.getAbsX() != DEFAULT_CELL_W)
                    camera.updateAbsX(1);
            }
            firstRow.draw(gc);
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            cellSelector.readCell(camera.picture.data());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        if (cellSelector.getSelectedCell().formula() != null) {
            infoBar.setInfobarTxt(recordedFormula.getLast().getTxt());
        }
        maybeGoToMergeStart(prevCell);
    }
    private static void moveDown() {
        int prevH = cellSelector.getH();
        Cell prevCell = cellSelector.getSelectedCell().copy();
        cellSelector.updateYCoord(1);
        cellSelector.readCell(camera.picture.data());
        if (cellSelector.getSelectedCell().isMergeStart())
            goTo(cellSelector.getSelectedCell().xCoord(),
                 cellSelector.getSelectedCell().getMergeDelimiter().yCoord()+1);
        if (cellSelector.getY() != camera.picture.getH()) {
            cellSelector.updateY(prevH);
            if (cellSelector.getY() + cellSelector.getH() > statusBar.getY()) {
                while (cellSelector.getY() + cellSelector.getH() != statusBar.getY()) {
                    cellSelector.updateY(-1);
                    camera.updateAbsY(1);
                }
                firstCol.draw(gc);
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        }
        else {
            camera.updateAbsY(prevH);
            firstCol.draw(gc);
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        if (cellSelector.getSelectedCell().formula() != null) {
            infoBar.setInfobarTxt(recordedFormula.getLast().getTxt());
        }
        maybeGoToMergeStart(prevCell);
    }
    private static void moveUp() {
        Cell prevCell = cellSelector.getSelectedCell().copy();
        if (cellSelector.getYCoord() != 1) {
            cellSelector.updateYCoord(-1);
            cellSelector.readCell(camera.picture.data());
        }
        if (cellSelector.getY() != DEFAULT_CELL_H) {
            cellSelector.updateY(-cellSelector.getH());
            if (cellSelector.getY() < cellSelector.getH()) {
                while (cellSelector.getY() != cellSelector.getH()) {
                    cellSelector.updateY(1);
                    camera.updateAbsY(-1);
                }
                firstCol.draw(gc);
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        }
        else {
            camera.updateAbsY(-cellSelector.getH());
            if (camera.getAbsY() < DEFAULT_CELL_H) {
                infoBar.setInfobarTxt("CAN'T GO UP");
                while (camera.getAbsY() != DEFAULT_CELL_H)
                    camera.updateAbsY(1);
            }
            firstCol.draw(gc);
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        if (cellSelector.getSelectedCell().formula() != null) {
            infoBar.setInfobarTxt(recordedFormula.getLast().getTxt());
        }
        maybeGoToMergeStart(prevCell);
    }
    private static void moveRight() {
        int prevW = cellSelector.getW();
        Cell prevCell = cellSelector.getSelectedCell().copy();
        cellSelector.updateXCoord(1);
        cellSelector.readCell(camera.picture.data());
        if (cellSelector.getSelectedCell().isMergeStart())
            goTo(cellSelector.getSelectedCell().getMergeDelimiter().xCoord()+1,
                 cellSelector.getSelectedCell().yCoord());
        if (cellSelector.getX() != camera.picture.getW()) {
            cellSelector.updateX(prevW);
            if (cellSelector.getX() + cellSelector.getW() > CANVAS_W) {
                while (cellSelector.getX() + cellSelector.getW() != CANVAS_W) {
                    cellSelector.updateX(-1);
                    camera.updateAbsX(1);
                }
                firstRow.draw(gc);
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            }
        }
        else {
            camera.updateAbsX(prevW);
            firstRow.draw(gc);
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        }
        camera.picture.resend(gc, camera.getAbsX(), camera.getAbsY());
        cellSelector.readCell(camera.picture.data());
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        if (cellSelector.getSelectedCell().formula() != null) {
            infoBar.setInfobarTxt(recordedFormula.getLast().getTxt());
        }
        maybeGoToMergeStart(prevCell);
    }

    private static void undo() {
        goTo(recordedCell.get(recordedCell.size() - 1 - dCounter).xCoord(), recordedCell.get(recordedCell.size() - 1 - dCounter).yCoord());
        if (recordedCell.get(recordedCell.size() - 1 - dCounter).txt().equals("")) {
            sheet.deleteCell(coordsCell.getCoords());
        }
        cellSelector.setSelectedCell(new Cell(
            cellSelector.getXCoord(),
            cellSelector.getYCoord(),
            cellSelector.getSelectedCell().txt()
        ));
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        sheet.addCell(cellSelector.getSelectedCell());
        cellSelector.getSelectedCell().setTxt(recordedCell.get(recordedCell.size() - 1 - dCounter).txt());
        /*if (recordedCell.get(recordedCell.size() - dCounter).formula() != null) {
            fCounter--;
        }*/
        dCounter = dCounter + 2;
    }
    private static void redo() {
        dCounter = dCounter - 2;
        goTo(recordedCell.get(recordedCell.size() - dCounter).xCoord(), recordedCell.get(recordedCell.size() - dCounter).yCoord());
        if (recordedCell.get(recordedCell.size() - dCounter).txt().matches(".*\\d.*")) {
            cellSelector.setSelectedCell(new Cell(
                cellSelector.getXCoord(),
                cellSelector.getYCoord(),
                sheet.redoCell(cellSelector.getSelectedCell().xCoord(), cellSelector.getSelectedCell().yCoord(),
                    recordedCell.get(recordedCell.size() - dCounter).value())
            ));
            infoBar.setInfobarTxt(cellSelector.getSelectedCell().value() + "");
        } else {
            cellSelector.setSelectedCell(new Cell(
                cellSelector.getXCoord(),
                cellSelector.getYCoord(),
                cellSelector.getSelectedCell().txt()
            ));
            infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        }
        if (recordedCell.get(recordedCell.size() - dCounter).formula() != null) {
            infoBar.setInfobarTxt(recordedFormula.get(recordedFormula.size() - fCounter).getTxt());
            cellSelector.getSelectedCell().setFormula(recordedFormula.get(recordedFormula.size() - fCounter));
            //fCounter++;
        }
        sheet.addCell(cellSelector.getSelectedCell());
        cellSelector.getSelectedCell().setTxt(recordedCell.get(recordedCell.size() - dCounter).txt());
    }

    private static void goTo(int xCoord, int yCoord) {
        while (xCoord - cellSelector.getXCoord() > 0)
            moveRight();
        while (xCoord - cellSelector.getXCoord() < 0)
            moveLeft();
        while (yCoord - cellSelector.getYCoord() > 0)
            moveDown();
        while (yCoord - cellSelector.getYCoord() < 0)
            moveUp();
        firstRow.draw(gc);
        firstCol.draw(gc);
        coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        coordsCell.draw(gc);
        statusBar.draw(gc);
    }
    public Formula toFormula(Formula formula) {
        return formula;
    }
    public static void onKeyPressed(@NotNull KeyEvent event) {
        switch (statusBar.getMode().charAt(1)) {
            case 'C' -> commandInput(event);
            case 'F' -> formulaInput(event);
            case 'I' -> textInput(event);
            case 'N' -> {
                switch (event.getCode()) {
                    case H, LEFT, BACK_SPACE -> moveLeft();
                    case J, DOWN, ENTER -> moveDown();
                    case K, UP -> moveUp();
                    case L, RIGHT, TAB, SPACE -> moveRight();
                    case D, DELETE -> {
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
                    case M -> sheet.unmergeCells(sheet.findCell(coordsCell.getCoords()));
                    case P -> System.out.println(recordedCell.get(1).value());
                    case U -> {
                        if (!recordedCell.isEmpty() && !(dCounter >= recordedCell.size())) undo();
                        else infoBar.setInfobarTxt("CAN'T UNDO RIGHT NOW");
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    case R -> {
                        if (!recordedCell.isEmpty() && !(dCounter <= 1)) redo();
                        else infoBar.setInfobarTxt("CAN'T REDO RIGHT NOW");
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    case A, I -> {
                        cellSelector.setSelectedCell(new Cell(
                                cellSelector.getXCoord(),
                                cellSelector.getYCoord(),
                                cellSelector.getSelectedCell().txt()
                        ));
                        recordedCell.add(cellSelector.getSelectedCell().copy());
                        cellSelector.readCell(camera.picture.data());
                        statusBar.setMode(MODE[2]);
                        if (cellSelector.getSelectedCell().txt() == null)
                            cellSelector.getSelectedCell().setTxt("");
                        cellSelector.draw(gc);
                    }
                    case ESCAPE -> statusBar.setMode(MODE[3]);
                    case EQUALS -> {
                        cellSelector.setSelectedCell(new Cell(
                                cellSelector.getXCoord(),
                                cellSelector.getYCoord(),
                                cellSelector.getSelectedCell().txt()
                        ));
                        recordedCell.add(cellSelector.getSelectedCell().copy());
                        statusBar.setMode(MODE[1]);
                        if (cellSelector.getSelectedCell().formula() == null)
                            cellSelector.getSelectedCell().setFormula(
                                new Formula("", cellSelector.getXCoord(), cellSelector.getYCoord())
                            );
                        infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                    }
                    case V -> {
                        statusBar.setMode(MODE[4]);
                        selectedCoords.add(new int[]{cellSelector.getXCoord(), cellSelector.getYCoord()});
                    }
                    case SEMICOLON -> {
                        statusBar.setMode(MODE[0]);
                        command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                        infoBar.setCommandTxt(command.getTxt());
                    }
                }
            }
            case 'V' -> visualSelection(event);
        }

        System.out.println("     sC.x: "+ cellSelector.getX()     +", yCoord: "+ cellSelector.getY());
        System.out.println("sC.xCoord: "+ cellSelector.getXCoord()+", sC.yCoord: "+ cellSelector.getYCoord());
        System.out.println(" cam.absX: "+camera.getAbsX()         +", cam.absY: "+camera.getAbsY());
        System.out.println("    Cells: "+sheet.getCells());
        System.out.println("========================================");

        if (statusBar.getMode().equals(MODE[3])) {
            cellSelector.draw(gc);
            coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        }
        else if (statusBar.getMode().equals(MODE[4])) {
            System.out.println("Selected coords = {");
            selectedCoords.forEach(c -> {
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                System.out.println('\t' + Arrays.toString(c));
            });
            camera.ready();
            System.out.println('}');
        }
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.draw(gc);
        infoBar.setKeyStroke(event.getCode().toString());
        infoBar.draw(gc);
        coordsCell.draw(gc);
    }

    private static void commandInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (infoBar.isEnteringCommandInVISUAL()) {
                    selectedCoords = new ArrayList<>();
                    infoBar.setEnteringCommandInVISUAL(false);
                }
                statusBar.setMode(MODE[3]);
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
            }
            case ENTER -> {
                if (infoBar.isEnteringCommandInVISUAL()) {
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
                    Formula f = new Formula(
                        coordsCell.getCoords() + ' ' + command.getTxt().substring(0, i),
                        c.xCoord(),
                        c.yCoord()
                    );
                    sheet.addCell(new Cell(
                        c.xCoord(),
                        c.yCoord(),
                        f.interpret(sheet),
                        f
                    ));
                }
                statusBar.setMode(MODE[3]);
                int prevXC = cellSelector.getXCoord(), prevYC = cellSelector.getYCoord();
                if (cellSelector.getX() == 0)
                    moveRight();
                else
                    moveLeft();
                if (cellSelector.getY() == 0)
                    moveDown();
                else
                    moveUp();
                command.interpret(sheet);
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                goTo(prevXC, prevYC);
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                if (!command.commandExists()) {
                    infoBar.setInfobarTxt("COMMAND OR FILE DOES NOT EXIST");
                }
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
        if(!statusBar.getMode().equals(MODE[3]))
            infoBar.setCommandTxt(command.getTxt());
    }

    private static void visualSelection(@NotNull KeyEvent event) {
        if (infoBar.isEnteringCommandInVISUAL()) {
            commandInput(event);
        }
        else {
            switch (event.getCode()) {
                case D -> {
                    selectedCoords.forEach(coord -> sheet.deleteCell(coord[0], coord[1]));
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

                        mergeStart.setMergeStart(true);
                        mergeStart.mergeWith(mergeEnd);
                        mergeEnd.mergeWith(mergeStart);

                        sheet.addCell(mergeStart);
                        sheet.addCell(mergeEnd);

                        for (int i = mergeStart.xCoord(); i <= mergeEnd.xCoord() ; i++) {
                            for (int j = mergeStart.yCoord(); j <= mergeEnd.yCoord(); j++) {
                                Cell c = sheet.findCell(i, j);
                                if (c != mergeStart && c != mergeEnd) {
                                    c.mergeWith(mergeStart);
                                    if (c.txt() == null)
                                        sheet.addCell(c);
                                }
                            }
                        }

                        do {
                            moveRight();
                        } while (cellSelector.getSelectedCell().getMergeDelimiter() != null);
                        selectedCoords = new ArrayList<>();
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        statusBar.setMode(MODE[3]);
                        moveLeft();
                    }
                }
                case ESCAPE -> {
                    statusBar.setMode(MODE[3]);
                    selectedCoords = new ArrayList<>();
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    cellSelector.draw(gc);
                }
                case SEMICOLON -> {
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

                    coordsCell.setCoords(maxXC, minXC, maxYC, minYC);
                }
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

    private static void textInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.readCell(camera.picture.data());
                statusBar.setMode(MODE[3]);
            }
            case LEFT, DOWN, UP, RIGHT, ENTER, TAB -> {
                cellSelector.setSelectedCell(new Cell(
                    cellSelector.getXCoord(),
                    cellSelector.getYCoord(),
                    cellSelector.getSelectedCell().txt()
                ));
                sheet.addCell(cellSelector.getSelectedCell());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                recordedCell.add(cellSelector.getSelectedCell().copy());
                switch (event.getCode()) {
                    case LEFT -> moveLeft();
                    case DOWN, ENTER -> moveDown();
                    case UP -> moveUp();
                    case RIGHT, TAB -> moveRight();
                }
                statusBar.setMode(MODE[3]);
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
                    infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                }
            }
            default -> {
                cellSelector.draw(gc, event.getText());
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
            }
        }
    }

    private static void formulaInput(@NotNull KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                cellSelector.readCell(camera.picture.data());
                statusBar.setMode(MODE[3]);
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
            }
            case ENTER -> {
                if (cellSelector.getSelectedCell().formula().getTxt().isEmpty()) {
                    infoBar.setInfobarTxt("CELL IS EMPTY");
                } else {
                    cellSelector.setSelectedCell(new Cell(
                        cellSelector.getXCoord(),
                        cellSelector.getYCoord(),
                        cellSelector.getSelectedCell().formula().interpret(sheet),
                        cellSelector.getSelectedCell().formula()
                    ));
                    recordedCell.add(cellSelector.getSelectedCell().copy());
                    sheet.addCell(cellSelector.getSelectedCell());
                }
                if (recordedCell.getLast().formula() != null) {
                    recordedFormula.add(cellSelector.getSelectedCell().formula());
                    infoBar.setInfobarTxt(recordedFormula.getLast().getTxt());
                } else infoBar.setInfobarTxt(cellSelector.getSelectedCell().value() + "");
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                statusBar.setMode(MODE[3]);
                cellSelector.readCell(camera.picture.data());
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
        reset();
    }

    public static void reset() {
        camera = new Camera(
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            CANVAS_W-DEFAULT_CELL_W,
            CANVAS_H-3*DEFAULT_CELL_H-4,
            DEFAULT_CELL_C,
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            new HashMap<>(),
            new HashMap<>()
        );
        cellSelector = new CellSelector(
            2*DEFAULT_CELL_W,
            2*DEFAULT_CELL_H,
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            Color.DARKGRAY,
            camera.picture.metadata()
        );
        coordsCell = new CoordsCell(
            0,
            0,
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            Color.GRAY
        );
        firstCol = new FirstCol(
            0,
            DEFAULT_CELL_H,
            DEFAULT_CELL_W,
            CANVAS_H-3*DEFAULT_CELL_H-4,
            Color.SILVER,
            camera.picture.metadata()
        );
        firstRow = new FirstRow(
            DEFAULT_CELL_W,
            0,
            CANVAS_W-DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            Color.SILVER,
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
            Color.GRAY
        );

        command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
        selectedCoords = new ArrayList<>();

        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
        camera.ready();
        coordsCell.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        coordsCell.draw(gc);
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.draw(gc);
        cellSelector.readCell(camera.picture.data());
        cellSelector.draw(gc);
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        infoBar.draw(gc);
        sheet.setPicMetadata(camera.picture.metadata());
    }
}
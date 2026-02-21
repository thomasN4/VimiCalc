package vimicalc.controller;

import vimicalc.model.*;

import static vimicalc.controller.Controller.*;
import static vimicalc.view.Defaults.*;

/**
 * Contains movement, navigation, editing, and undo/redo operations
 * extracted from {@link Controller}.
 *
 * <p>Methods here were originally static methods on Controller.
 * They still access Controller's static fields directly; a future
 * phase will convert those to instance fields.</p>
 */
public class EditorOperations {

    /**
     * If the cursor has landed on a cell that is part of a merged range
     * (but not the merge-start), automatically navigates to the merge-start cell.
     */
    void maybeGoToMergeStart() {
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

    /**
     * Moves the cell selector one cell to the left. Handles viewport scrolling
     * when the cursor reaches the left edge, and boundary checks.
     */
    public void moveLeft() {
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

    /** Moves the cell selector one cell down, scrolling the viewport if necessary. */
    public void moveDown() {
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

    /** Moves the cell selector one cell up, scrolling the viewport if necessary. */
    public void moveUp() {
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

    /** Moves the cell selector one cell to the right, scrolling the viewport if necessary. */
    public void moveRight() {
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

    /**
     * Updates the info bar with the selected cell's value and formula
     * expression (if any).
     */
    public void cellContentToIBar() {
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

    /**
     * Cleans up the undo history after a new edit is made while in a
     * partially-undone state.
     */
    public void removeUltCStates() {
        Cell last = recordedCellStates.getLast().copy();
        recordedCellStates.removeLast();
        while (undoCounter != 0) {
            recordedCellStates.removeLast();
            undoCounter--;
        }
        recordedCellStates.add(last);
        undoneCellStates = new java.util.LinkedList<>();
    }

    /** Reverts the last cell edit by swapping the current state with the recorded previous state. */
    public void undo() {
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

    /** Re-applies a previously undone cell edit. */
    public void redo() {
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

    /**
     * Navigates the cell selector to the given coordinates by issuing
     * repeated move commands.
     *
     * @param xCoord the target column (one-based)
     * @param yCoord the target row (one-based)
     */
    public void goTo(int xCoord, int yCoord) {
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

    /**
     * Navigates to the given coordinates while remembering the previous position.
     *
     * @param xCoord the target column
     * @param yCoord the target row
     * @param prevXC the previous column to remember
     * @param prevYC the previous row to remember
     */
    public void goToAndRemember(int xCoord, int yCoord, int prevXC, int prevYC) {
        staticPrevXC = prevXC;
        staticPrevYC = prevYC;
        goTo(xCoord, yCoord);
    }

    /**
     * Pastes a cell from the clipboard at the current cursor position.
     *
     * @param index the index into the clipboard list
     */
    public void paste(int index) {
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

    /** Redraws all chrome UI elements (headers, status bar, info bar, coordinates). */
    public void updateVisualState() {
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.draw(gc);
        infoBar.draw(gc);
        coordsInfo.draw(gc);
    }

    /** Prepares the selected cell's text for INSERT mode editing. */
    public void setSCTxtForTextInput() {
        if (cellSelector.getSelectedCell().value() != null &&
            !(""+cellSelector.getSelectedCell().value()).endsWith(".0"))
            cellSelector.getSelectedCell().setTxt(""+cellSelector.getSelectedCell().value());
        if (cellSelector.getSelectedCell().txt() == null)
            cellSelector.getSelectedCell().setTxt("");
    }
}

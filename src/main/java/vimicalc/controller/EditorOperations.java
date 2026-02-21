package vimicalc.controller;

import vimicalc.model.*;

import static vimicalc.view.Defaults.*;

/**
 * Contains movement, navigation, editing, and undo/redo operations
 * extracted from {@link Controller}.
 *
 * <p>Methods here were originally static methods on Controller.
 * They access Controller's instance fields through a stored reference.</p>
 */
public class EditorOperations {

    private final Controller ctrl;

    /**
     * Creates a new EditorOperations that delegates field access to the given controller.
     *
     * @param ctrl the controller whose fields this instance operates on
     */
    public EditorOperations(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * If the cursor has landed on a cell that is part of a merged range
     * (but not the merge-start), automatically navigates to the merge-start cell.
     */
    void maybeGoToMergeStart() {
        System.out.println(ctrl.cellSelector.getSelectedCell().getMergeDelimiter());
        if (ctrl.cellSelector.getSelectedCell().getMergeDelimiter() != null &&
            !ctrl.cellSelector.getSelectedCell().isMergeStart() &&
            !ctrl.goingToMergeStart) {
            Cell currMergeStart = ctrl.cellSelector.getSelectedCell().getMergeDelimiter();
            ctrl.goingToMergeStart = true;
            goTo(currMergeStart.xCoord(), currMergeStart.yCoord());
            ctrl.goingToMergeStart = false;
        }
    }

    /**
     * Moves the cell selector one cell to the left. Handles viewport scrolling
     * when the cursor reaches the left edge, and boundary checks.
     */
    public void moveLeft() {
        if (ctrl.cellSelector.getXCoord() != 1) {
            ctrl.cellSelector.updateXCoord(-1);
            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        } else {
            ctrl.infoBar.setInfobarTxt("CAN'T GO LEFT");
            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
            return;
        }
        cellContentToIBar();
        if (ctrl.cellSelector.getX() != ctrl.firstCol.getW()) {
            ctrl.cellSelector.updateX(-ctrl.cellSelector.getW());
            if (ctrl.cellSelector.getX() < ctrl.firstCol.getW()) {
                while (ctrl.cellSelector.getX() != ctrl.firstCol.getW()) {
                    ctrl.cellSelector.updateX(1);
                    ctrl.camera.updateAbsX(-1);
                }
                ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.firstRow.draw(ctrl.gc);
            }
        }
        else {
            ctrl.camera.updateAbsX(-ctrl.cellSelector.getW());
            if (ctrl.camera.getAbsX() < ctrl.firstCol.getW()) {
                ctrl.infoBar.setInfobarTxt("CAN'T GO LEFT");
                while (ctrl.camera.getAbsX() != ctrl.firstCol.getW())
                    ctrl.camera.updateAbsX(1);
            }
            ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.firstRow.draw(ctrl.gc);
            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        }
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /** Moves the cell selector one cell down, scrolling the viewport if necessary. */
    public void moveDown() {
        int prevH;
        if (!ctrl.cellSelector.getSelectedCell().isMergeStart() || ctrl.currMode == Mode.VISUAL)
            prevH = ctrl.cellSelector.getH();
        else {
            prevH = ctrl.cellSelector.getMergedH();
            ctrl.cellSelector.updateYCoord(
                ctrl.cellSelector.getSelectedCell().getMergeDelimiter().yCoord() -
                ctrl.cellSelector.getSelectedCell().yCoord()
            );
        }
        ctrl.cellSelector.updateYCoord(1);
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        cellContentToIBar();
        if (ctrl.cellSelector.getY() + ctrl.cellSelector.getH() != ctrl.statusBar.getY()) {
            ctrl.cellSelector.updateY(prevH);
            if (ctrl.cellSelector.getY() + ctrl.cellSelector.getH() > ctrl.statusBar.getY()) {
                while (ctrl.cellSelector.getY() + ctrl.cellSelector.getH() != ctrl.statusBar.getY()) {
                    ctrl.cellSelector.updateY(-1);
                    ctrl.camera.updateAbsY(1);
                }
                ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.firstCol.draw(ctrl.gc);
            }
        }
        else {
            ctrl.camera.updateAbsY(prevH);
            ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.firstCol.draw(ctrl.gc);
        }
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /** Moves the cell selector one cell up, scrolling the viewport if necessary. */
    public void moveUp() {
        if (ctrl.cellSelector.getYCoord() != 1) {
            ctrl.cellSelector.updateYCoord(-1);
            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        } else {
            ctrl.infoBar.setInfobarTxt("CAN'T GO UP");
            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
            return;
        }
        cellContentToIBar();
        if (ctrl.cellSelector.getY() != DEFAULT_CELL_H) {
            ctrl.cellSelector.updateY(-ctrl.cellSelector.getH());
            if (ctrl.cellSelector.getY() < DEFAULT_CELL_H) {
                while (ctrl.cellSelector.getY() != DEFAULT_CELL_H) {
                    ctrl.cellSelector.updateY(1);
                    ctrl.camera.updateAbsY(-1);
                }
                ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.firstCol.draw(ctrl.gc);
            }
        }
        else {
            ctrl.camera.updateAbsY(-ctrl.cellSelector.getH());
            if (ctrl.camera.getAbsY() < DEFAULT_CELL_H) {
                ctrl.infoBar.setInfobarTxt("CAN'T GO UP");
                while (ctrl.camera.getAbsY() != DEFAULT_CELL_H)
                    ctrl.camera.updateAbsY(1);
            }
            ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.firstCol.draw(ctrl.gc);
        }
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /** Moves the cell selector one cell to the right, scrolling the viewport if necessary. */
    public void moveRight() {
        int prevW;
        if (!ctrl.cellSelector.getSelectedCell().isMergeStart() || ctrl.currMode == Mode.VISUAL)
            prevW = ctrl.cellSelector.getW();
        else {
            prevW = ctrl.cellSelector.getMergedW();
            ctrl.cellSelector.updateXCoord(
                ctrl.cellSelector.getSelectedCell().getMergeDelimiter().xCoord() -
                ctrl.cellSelector.getSelectedCell().xCoord()
            );
        }
        ctrl.cellSelector.updateXCoord(1);
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.cellSelector.getX() + ctrl.cellSelector.getW() != ctrl.CANVAS_W) {
            ctrl.cellSelector.updateX(prevW);
            if (ctrl.cellSelector.getX() + ctrl.cellSelector.getW() > ctrl.CANVAS_W) {
                while (ctrl.cellSelector.getX() + ctrl.cellSelector.getW() != ctrl.CANVAS_W) {
                    ctrl.cellSelector.updateX(-1);
                    ctrl.camera.updateAbsX(1);
                }
                ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                ctrl.firstRow.draw(ctrl.gc);
            }
        }
        else {
            ctrl.camera.updateAbsX(prevW);
            ctrl.camera.picture.metadata().generate(ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
            ctrl.firstRow.draw(ctrl.gc);
        }
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        cellContentToIBar();
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /**
     * Updates the info bar with the selected cell's value and formula
     * expression (if any).
     */
    public void cellContentToIBar() {
        if (ctrl.cellSelector.getSelectedCell().value() != null)
            ctrl.infoBar.setInfobarTxt("(="+ctrl.cellSelector.getSelectedCell().value()+")");
        else {
            ctrl.infoBar.setInfobarTxt(null);
            return;
        }
        if (ctrl.cellSelector.getSelectedCell().formula() != null)
            ctrl.infoBar.setInfobarTxt(
                ctrl.infoBar.getInfobarTxt() + " " +
                "(f:"+ctrl.cellSelector.getSelectedCell().formula().getTxt()+")"
            );
    }

    /**
     * Cleans up the undo history after a new edit is made while in a
     * partially-undone state.
     */
    public void removeUltCStates() {
        Cell last = ctrl.recordedCellStates.getLast().copy();
        ctrl.recordedCellStates.removeLast();
        while (ctrl.undoCounter != 0) {
            ctrl.recordedCellStates.removeLast();
            ctrl.undoCounter--;
        }
        ctrl.recordedCellStates.add(last);
        ctrl.undoneCellStates = new java.util.LinkedList<>();
    }

    /** Reverts the last cell edit by swapping the current state with the recorded previous state. */
    public void undo() {
        int listIndex = ctrl.recordedCellStates.size() - 1 - ctrl.undoCounter;
        ctrl.undoCounter++;

        Cell substitute = ctrl.recordedCellStates.get(listIndex).copy();
        goTo(substitute.xCoord(), substitute.yCoord());
        ctrl.recordedCellStates.set(listIndex, ctrl.sheet.findCell(substitute.xCoord(), substitute.yCoord()).copy());
        ctrl.undoneCellStates.add(substitute);
        ctrl.sheet.deleteCell(substitute.xCoord(), substitute.yCoord());

        if (substitute.formula() != null) {
            Formula f = substitute.formula();
            try {
                ctrl.cellSelector.getSelectedCell().setFormulaResult(f.interpret(ctrl.sheet), f);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Something went very wrong.");
            }
        } else ctrl.cellSelector.setSelectedCell(substitute);

        cellContentToIBar();
        ctrl.sheet.addCell(ctrl.cellSelector.getSelectedCell());
        ctrl.sheet.getCells().forEach(Cell::isEmpty);
    }

    /** Re-applies a previously undone cell edit. */
    public void redo() {
        int listIndex = ctrl.recordedCellStates.size() - ctrl.undoCounter;
        ctrl.undoCounter--;

        Cell substitute = ctrl.recordedCellStates.get(listIndex).copy();
        goTo(substitute.xCoord(), substitute.yCoord());
        ctrl.recordedCellStates.set(listIndex, ctrl.undoneCellStates.getLast().copy());
        ctrl.undoneCellStates.removeLast();
        ctrl.sheet.deleteCell(substitute.xCoord(), substitute.yCoord());

        if (substitute.formula() != null) {
            Formula f = substitute.formula();
            try {
                ctrl.cellSelector.getSelectedCell().setFormulaResult(f.interpret(ctrl.sheet), f);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Something went very wrong.");
            }
        } else ctrl.cellSelector.setSelectedCell(substitute);

        cellContentToIBar();
        ctrl.sheet.addCell(ctrl.cellSelector.getSelectedCell());
        ctrl.sheet.getCells().forEach(Cell::isEmpty);
    }

    /**
     * Navigates the cell selector to the given coordinates by issuing
     * repeated move commands.
     *
     * @param xCoord the target column (one-based)
     * @param yCoord the target row (one-based)
     */
    public void goTo(int xCoord, int yCoord) {
        while (xCoord - ctrl.cellSelector.getXCoord() > 0)
            moveRight();
        while (xCoord - ctrl.cellSelector.getXCoord() < 0)
            moveLeft();
        while (yCoord - ctrl.cellSelector.getYCoord() > 0)
            moveDown();
        while (yCoord - ctrl.cellSelector.getYCoord() < 0)
            moveUp();
        ctrl.coordsInfo.setCoords(ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
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
        ctrl.staticPrevXC = prevXC;
        ctrl.staticPrevYC = prevYC;
        goTo(xCoord, yCoord);
    }

    /**
     * Pastes a cell from the clipboard at the current cursor position.
     *
     * @param index the index into the clipboard list
     */
    public void paste(int index) {
        System.out.println("Pasting cell " + ctrl.clipboard.get(index));
        if (ctrl.clipboard.get(index).formula() != null) {
            Formula f = new Formula(
                ctrl.clipboard.get(index).formula().getTxt(),
                ctrl.cellSelector.getXCoord(),
                ctrl.cellSelector.getYCoord()
            );
            try {
                ctrl.recordedCellStates.add(ctrl.cellSelector.getSelectedCell().copy());
                ctrl.sheet.deleteCell(ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
                ctrl.cellSelector.getSelectedCell().setFormulaResult(f.interpret(ctrl.sheet), f);
            } catch (Exception e) {
                ctrl.infoBar.setInfobarTxt(e.getMessage());
                return;
            }
        } else {
            ctrl.recordedCellStates.add(ctrl.cellSelector.getSelectedCell().copy());
            ctrl.sheet.deleteCell(ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
            ctrl.cellSelector.setSelectedCell(ctrl.clipboard.get(index).copy());
        }
        ctrl.cellSelector.getSelectedCell().setXCoord(ctrl.cellSelector.getXCoord());
        ctrl.cellSelector.getSelectedCell().setYCoord(ctrl.cellSelector.getYCoord());
        cellContentToIBar();
        ctrl.sheet.addCell(ctrl.cellSelector.getSelectedCell());
        if (ctrl.undoCounter != 0) removeUltCStates();
    }

    /** Redraws all chrome UI elements (headers, status bar, info bar, coordinates). */
    public void updateVisualState() {
        ctrl.firstCol.draw(ctrl.gc);
        ctrl.firstRow.draw(ctrl.gc);
        ctrl.statusBar.draw(ctrl.gc);
        ctrl.infoBar.draw(ctrl.gc);
        ctrl.coordsInfo.draw(ctrl.gc);
    }

    /** Prepares the selected cell's text for INSERT mode editing. */
    public void setSCTxtForTextInput() {
        if (ctrl.cellSelector.getSelectedCell().value() != null &&
            !(""+ctrl.cellSelector.getSelectedCell().value()).endsWith(".0"))
            ctrl.cellSelector.getSelectedCell().setTxt(""+ctrl.cellSelector.getSelectedCell().value());
        if (ctrl.cellSelector.getSelectedCell().txt() == null)
            ctrl.cellSelector.getSelectedCell().setTxt("");
    }
}

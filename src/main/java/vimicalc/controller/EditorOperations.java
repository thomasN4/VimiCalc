package vimicalc.controller;

import vimicalc.model.*;
import vimicalc.view.Positions;

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
     * Scrolls the camera by exactly the overshoot needed to bring the selected
     * cell flush into the viewport, then redraws the grid and headers. Both
     * axes are checked so diagonal displacements (e.g. merge pulls) are
     * corrected in one step.
     *
     * <p>Positions are derived from the cell boundary arrays and the live
     * camera offset — the shared viewport formula (issue #30) — so repeated
     * scrolling cannot accumulate drift.</p>
     */
    private void scrollCursorIntoView() {
        int[] xs = ctrl.camera.picture.metadata().getCellAbsXs();
        int[] ys = ctrl.camera.picture.metadata().getCellAbsYs();
        int xc = ctrl.cellSelector.getXCoord(), yc = ctrl.cellSelector.getYCoord();
        int left   = xs[xc]   - ctrl.camera.getAbsX() + GUTTER_W;
        int right  = xs[xc+1] - ctrl.camera.getAbsX() + GUTTER_W;
        int top    = ys[yc]   - ctrl.camera.getAbsY() + HEADER_H;
        int bottom = ys[yc+1] - ctrl.camera.getAbsY() + HEADER_H;
        int dx = right > ctrl.CANVAS_W ? right - ctrl.CANVAS_W
               : left < GUTTER_W ? left - GUTTER_W : 0;
        int dy = bottom > ctrl.viewportBottom() ? bottom - ctrl.viewportBottom()
               : top < HEADER_H ? top - HEADER_H : 0;
        if (dx == 0 && dy == 0) return;
        ctrl.camera.scrollBy(dx, dy);
        ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.firstRow.draw(ctrl.gc);
        ctrl.firstCol.draw(ctrl.gc);
    }

    /** Zooms the grid view in by one step. */
    public void zoomIn() {
        applyZoom(ctrl.camera.picture.metadata().getZoom() * ZOOM_STEP);
    }

    /** Zooms the grid view out by one step. */
    public void zoomOut() {
        applyZoom(ctrl.camera.picture.metadata().getZoom() / ZOOM_STEP);
    }

    /** Resets the grid view zoom to 100%. */
    public void zoomReset() {
        applyZoom(DEFAULT_ZOOM);
    }

    /**
     * Applies a new zoom factor (clamped by {@link Positions#setZoom(double)}),
     * re-renders the grid, and keeps the cursor visible: the camera offset is
     * never mutated by zoom itself, but zooming in can push the selected cell
     * past the viewport edge, so the standard scroll correction runs after.
     *
     * @param newZoom the requested zoom factor (1.0 = 100%)
     */
    private void applyZoom(double newZoom) {
        Positions positions = ctrl.camera.picture.metadata();
        positions.setZoom(newZoom);
        ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.camera.ready();
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        scrollCursorIntoView();
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        ctrl.infoBar.setInfobarTxt("Zoom: " + Math.round(positions.getZoom() * 100) + "%");
    }

    /**
     * Applies a new canvas (grid viewport) size after a window resize and
     * re-renders everything: the layout is regenerated for the new dimensions,
     * the grid, headers, and cursor are repainted, and the standard scroll
     * correction keeps the selected cell visible when the viewport shrinks.
     *
     * <p>Unlike {@link #applyZoom(double)}, this can run in any editing mode,
     * so it uses {@link CellSelector#resync()} (which preserves an in-progress
     * INSERT/FORMULA edit buffer) instead of
     * {@link CellSelector#readCell(java.util.ArrayList)}, and it leaves the
     * info bar untouched (it may be showing a pending command or the help
     * scroll percentage).</p>
     *
     * @param w the new canvas width in pixels
     * @param h the new canvas height in pixels
     */
    public void applyViewportSize(int w, int h) {
        // Never let the grid degenerate below one default cell; unreachable
        // through the stage minimum size, but layout is not under our control.
        w = Math.max(w, GUTTER_W + DEFAULT_CELL_W);
        h = Math.max(h, HEADER_H + DEFAULT_CELL_H);
        if (w == ctrl.CANVAS_W && h == ctrl.CANVAS_H) return;
        ctrl.setCanvasSize(w, h);

        // Guarantee the boundary arrays cover the cursor after regeneration:
        // generate() only walks ~2 cells past the new viewport edge or to
        // maxXC/maxYC, so a sharp shrink while deep-scrolled would otherwise
        // leave the cursor column/row outside the arrays. Same grow-only bump
        // VISUAL mode uses.
        Positions positions = ctrl.camera.picture.metadata();
        positions.setMaxXC(Math.max(positions.getMaxXC(), ctrl.cellSelector.getXCoord()));
        positions.setMaxYC(Math.max(positions.getMaxYC(), ctrl.cellSelector.getYCoord()));

        positions.setViewport(w - GUTTER_W, h - HEADER_H);
        ctrl.camera.picture.setW(w - GUTTER_W);
        ctrl.camera.picture.setH(h - HEADER_H);
        ctrl.firstRow.setW(w - GUTTER_W);
        ctrl.firstCol.setH(h - HEADER_H);

        ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.camera.ready();
        ctrl.cellSelector.resync();
        scrollCursorIntoView();
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.resync();
        updateVisualState();
        ctrl.cellSelector.draw(ctrl.gc);
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
        scrollCursorIntoView();
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /** Moves the cell selector one cell down, scrolling the viewport if necessary. */
    public void moveDown() {
        if (ctrl.cellSelector.getSelectedCell().isMergeStart() && ctrl.currMode != Mode.VISUAL)
            ctrl.cellSelector.updateYCoord(
                ctrl.cellSelector.getSelectedCell().getMergeDelimiter().yCoord() -
                ctrl.cellSelector.getSelectedCell().yCoord()
            );
        ctrl.cellSelector.updateYCoord(1);
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        cellContentToIBar();
        scrollCursorIntoView();
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
        scrollCursorIntoView();
        ctrl.camera.picture.resend(ctrl.gc, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        if (ctrl.currMode != Mode.VISUAL) maybeGoToMergeStart();
    }

    /** Moves the cell selector one cell to the right, scrolling the viewport if necessary. */
    public void moveRight() {
        if (ctrl.cellSelector.getSelectedCell().isMergeStart() && ctrl.currMode != Mode.VISUAL)
            ctrl.cellSelector.updateXCoord(
                ctrl.cellSelector.getSelectedCell().getMergeDelimiter().xCoord() -
                ctrl.cellSelector.getSelectedCell().xCoord()
            );
        ctrl.cellSelector.updateXCoord(1);
        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
        scrollCursorIntoView();
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
     * <p>If the target lies inside a merged range (including the non-start
     * delimiter corner), it is remapped to the merge-start cell first — the
     * same convention as hjkl via {@link #maybeGoToMergeStart()}. Without
     * that remap, step-wise movement cannot settle on a merge interior:
     * each entry into the range is pulled back to the start, and the loop
     * exits beside the merge (issue #31).</p>
     *
     * <p>Both axes are re-checked after every single step: a move (or the
     * merge pull in {@link #maybeGoToMergeStart()}) can displace the axis
     * that was already corrected, so running one axis to completion before
     * the other lands on the wrong cell next to merged ranges. An iteration
     * cap guards against pathological non-convergence (e.g. sheet-boundary
     * blocks or residual merge skip/pull ping-pong en route).</p>
     *
     * @param xCoord the target column (one-based)
     * @param yCoord the target row (one-based)
     */
    public void goTo(int xCoord, int yCoord) {
        // Land on merge start when the target is a merge interior (issue #31).
        Cell resolved = ctrl.sheet.findCell(xCoord, yCoord);
        xCoord = resolved.xCoord();
        yCoord = resolved.yCoord();

        int guard = 3 * (Math.abs(xCoord - ctrl.cellSelector.getXCoord()) +
                         Math.abs(yCoord - ctrl.cellSelector.getYCoord())) + 32;
        boolean xFirst = true;
        int beforeLastXC = Integer.MIN_VALUE, beforeLastYC = Integer.MIN_VALUE;
        while ((ctrl.cellSelector.getXCoord() != xCoord ||
                ctrl.cellSelector.getYCoord() != yCoord) && guard-- > 0) {
            int prevXC = ctrl.cellSelector.getXCoord(), prevYC = ctrl.cellSelector.getYCoord();
            int dx = xCoord - prevXC, dy = yCoord - prevYC;
            if (dx != 0 && (xFirst || dy == 0)) {
                if (dx > 0) moveRight();
                else moveLeft();
            }
            else if (dy > 0) moveDown();
            else moveUp();
            int currXC = ctrl.cellSelector.getXCoord(), currYC = ctrl.cellSelector.getYCoord();
            if (currXC == prevXC && currYC == prevYC)
                break; // blocked (sheet boundary): no progress possible
            if (currXC == beforeLastXC && currYC == beforeLastYC)
                xFirst = !xFirst; // merge skip/pull ping-pong: try the other axis
            beforeLastXC = prevXC;
            beforeLastYC = prevYC;
        }
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

    /** Redraws canvas headers and refreshes the status-bar label (mode/filename). */
    public void updateVisualState() {
        ctrl.firstCol.draw(ctrl.gc);
        ctrl.firstRow.draw(ctrl.gc);
        ctrl.statusBar.refresh();
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

package vimicalc.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.*;
import vimicalc.view.*;

import static vimicalc.view.Defaults.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.N;
import static javafx.scene.input.KeyCode.P;
import static vimicalc.Main.arg1;

/**
 * The main controller that orchestrates all user interaction with the spreadsheet.
 *
 * <p>Implements {@link Initializable} to set up the UI on FXML load. Manages
 * the global application state including the current {@link Mode}, the
 * {@link Camera} viewport, the {@link CellSelector} cursor, undo/redo history,
 * clipboard, and macro recordings.</p>
 *
 * <p>Delegates keyboard input to mode-specific handlers:</p>
 * <ul>
 *   <li><b>NORMAL</b> → {@link KeyCommand#addChar(KeyEvent)}</li>
 *   <li><b>INSERT</b> → {@link #textInput(KeyEvent)}</li>
 *   <li><b>FORMULA</b> → {@link #formulaInput(KeyEvent)}</li>
 *   <li><b>COMMAND</b> → {@link #commandInput(KeyEvent)}</li>
 *   <li><b>VISUAL</b> → {@link #visualSelection(KeyEvent)}</li>
 *   <li><b>HELP</b> → {@link HelpMenu#navigate(KeyEvent)}</li>
 * </ul>
 */
public class Controller implements Initializable {
    int CANVAS_W;
    int CANVAS_H;

    /*CD
    private int MOUSE_X;
    private int MOUSE_Y;*/

    /** The graphics context used for all canvas drawing operations. */
    GraphicsContext gc;
    @FXML
    private Canvas canvas;
    /** Overlay host for recording indicator / help menu (parts 3–4 of chrome migration). */
    @FXML
    private StackPane canvasStack;
    /**
     * Absolute-positioned overlay on the canvas stack. Hosts the recording
     * indicator label and the help menu node.
     */
    @FXML
    private Pane overlayPane;
    @FXML
    private Label recordingIndicatorLabel;
    @FXML
    private Label helpLabel;
    @FXML
    private VBox completionLabelBox;
    @FXML
    private Label statusLabel;
    @FXML
    private Label coordsLabel;
    @FXML
    private Text infoText;
    @FXML
    private Region infoCaret;
    @FXML
    private Text infoCaretChar;
    @FXML
    private Label exprLabel;
    /**
     * Undo history. Each entry is one edit step, holding the pre-edit states
     * of every cell that step touched (a compound step for multi-cell
     * operations like merge, unmerge, or VISUAL delete; a singleton list for
     * plain cell edits). The first element is the cell the cursor returns to
     * when the step is undone.
     */
    LinkedList<List<Cell>> recordedCellStates;
    /** Stack of edit steps produced by undo, used for redo. */
    LinkedList<List<Cell>> undoneCellStates;
    /** How many undo steps have been taken since the last edit. */
    int undoCounter;
    /** The clipboard for yank/paste operations. */
    ArrayList<Cell> clipboard;
    /** The viewport camera tracking the visible portion of the sheet. */
    Camera camera;
    /** The current colon-command being composed or executed. */
    Command command;
    /** Completion state (fuzzy matches + cycle) for the colon-command being composed. */
    final CommandCompletion commandCompletion = new CommandCompletion();
    /** The popup listing completion matches while a command name is typed. */
    CompletionPopup completionPopup;
    /** Displays the current cell coordinates in the status area. */
    CoordsInfo coordsInfo;
    FirstCol firstCol;
    FirstRow firstRow;
    /** The help menu overlay shown in HELP mode. */
    HelpMenu helpMenu;
    /** The information bar at the bottom of the window. */
    InfoBar infoBar;
    /** Shows which macro register is being recorded into, hidden otherwise. */
    RecordingIndicator recordingIndicator;
    /** The cell selector (cursor) highlighting the active cell. */
    CellSelector cellSelector;
    /** The list of coordinates currently selected in VISUAL mode. */
    ArrayList<int[]> selectedCoords;
    /** The spreadsheet data model. */
    Sheet sheet;
    /** The status bar showing the current mode. */
    StatusBar statusBar;
    /** Handles key-command sequences in NORMAL mode. */
    KeyCommand keyCommand;
    /** The current input mode (NORMAL, INSERT, COMMAND, etc.). */
    Mode currMode;
    /** Recorded macros, keyed by their assigned character. */
    HashMap<Character, LinkedList<KeyEvent>> macros;
    /** Handles movement, navigation, editing, and undo/redo operations. */
    EditorOperations editorOps;
    /**
     * Whether a colon-command is being entered from VISUAL mode (via {@code ;}).
     * Controller flow state — not part of the info-bar view.
     */
    boolean enteringCommandInVISUAL;

    /*CD arranger avec les classes moves car sinon cause des bugs en utilisant clavier
    public void onMouseClicked(@NotNull MouseEvent mouseEvent) {
        MOUSE_X = (int) mouseEvent.getX() / DEFAULT_CELL_W;
        MOUSE_Y = (int) mouseEvent.getY() / DEFAULT_CELL_H - 1;

        System.out.print(" Mouse CLicked "+ MOUSE_X + " // " + MOUSE_Y);

        cellSelector.setX(MOUSE_X);
        cellSelector.setY(MOUSE_Y);

        System.out.print(" Selected Cell "+ cellSelector.getX() + " // " + cellSelector.getY());
    }
    */

    /** Flag to prevent recursive merge-start navigation. */
    boolean goingToMergeStart = false;

    /** Moves the cell selector one cell to the left. Delegates to {@link EditorOperations}. */
    private void moveLeft() { editorOps.moveLeft(); }
    /** Moves the cell selector one cell down. Delegates to {@link EditorOperations}. */
    private void moveDown() { editorOps.moveDown(); }
    /** Moves the cell selector one cell up. Delegates to {@link EditorOperations}. */
    private void moveUp() { editorOps.moveUp(); }
    /** Moves the cell selector one cell to the right. Delegates to {@link EditorOperations}. */
    private void moveRight() { editorOps.moveRight(); }
    /** Updates the info bar with the selected cell's content. Delegates to {@link EditorOperations}. */
    private void cellContentToIBar() { editorOps.cellContentToIBar(); }
    /** Cleans up undo history after a new edit in a partially-undone state. Delegates to {@link EditorOperations}. */
    private void removeUltCStates() { editorOps.removeUltCStates(); }

    /** Previous X coordinate, used for jump-back navigation. */
    int staticPrevXC;
    /** Previous Y coordinate, used for jump-back navigation. */
    int staticPrevYC;
    /** Navigates the cell selector to the given coordinates. Delegates to {@link EditorOperations}. */
    private void goTo(int xCoord, int yCoord) { editorOps.goTo(xCoord, yCoord); }

    /**
     * The global key event handler, wired to the scene in {@link vimicalc.Main}.
     * Dispatches to mode-specific handlers and updates the UI after each keystroke.
     *
     * @param event the key event
     */
    public void onKeyPressed(@NotNull KeyEvent event) {
        onKeyPressed(event, false);
    }

    /**
     * Key event handler distinguishing real keystrokes from paced macro
     * replay. While a paced replay is running ({@code :macroDelay > 0}),
     * real keystrokes are ignored — except ESC, which aborts the playback —
     * and replayed events are never recorded into a macro.
     *
     * @param event      the key event
     * @param fromReplay whether the event was dispatched by the macro replay
     *                   drainer rather than typed by the user
     */
    void onKeyPressed(@NotNull KeyEvent event, boolean fromReplay) {
        if (keyCommand.isReplaying() && !fromReplay) {
            if (event.getCode() == ESCAPE) keyCommand.abortReplay();
            return;
        }
        if (keyCommand.recordingMacro && !fromReplay) keyCommand.currMacro.add(event);
        if (currMode == Mode.NORMAL) keyCommand.addChar(event);
        else {
            switch (currMode) {
                case COMMAND -> commandInput(event);
                case FORMULA -> formulaInput(event);
                case HELP -> {
                    helpMenu.navigate(event);
                    infoBar.setInfobarTxt(helpMenu.percentage());
                    if (event.getCode() == ESCAPE) {
                        helpMenu.hide();
                        currMode = Mode.NORMAL;
                        cellContentToIBar();
                    }
                }
                case INSERT -> textInput(event);
                case VISUAL -> {
                    goingToMergeStart = true;
                    visualSelection(event);
                }
                default -> {}
            }
        }

        if (currMode == Mode.NORMAL) {
            cellSelector.draw(gc);
            coordsInfo.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        }
        else if (currMode == Mode.VISUAL) {
            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
            camera.ready();
        }
        if (currMode != Mode.HELP) updateVisualState();
    }

    /** Redraws all chrome UI elements. Delegates to {@link EditorOperations}. */
    private void updateVisualState() { editorOps.updateVisualState(); }

    /**
     * Handles keyboard input in COMMAND mode. Builds up the command string
     * character by character, executing it on ENTER. While the command name
     * itself is being typed (not a VISUAL-mode formula, no arguments yet),
     * the completion popup filters fuzzy matches live (see
     * {@link CommandCompletion}); TAB / Shift+TAB and Ctrl+N / Ctrl+P cycle
     * through them, writing the selection into the command line. ESC
     * dismisses the popup first and cancels COMMAND mode on a second press.
     * Also supports entering commands from VISUAL mode via {@code ;}
     * (SEMICOLON), for applying formulas to selections.
     *
     * <p>The command line is edited at a caret ({@link EditBuffer}) with
     * Emacs-style keys: Ctrl+B/F or Left/Right move by character, Alt+B/F by
     * word, Ctrl+A/E or Home/End jump to start/end, Ctrl+D forward-deletes,
     * Ctrl+K kills to end, and Alt+D kills the next word.</p>
     *
     * @param event the key event to process
     */
    private void commandInput(@NotNull KeyEvent event) {
        // Ctrl+N / Ctrl+P popup navigation is intercepted ahead of the switch
        // so the default branch never appends their "n"/"p" key text.
        if (event.isControlDown() && (event.getCode() == N || event.getCode() == P)) {
            event.consume();
            if (completingName()) cycleCompletion(event.getCode() == N);
            infoBar.setCommandTxt(command.getTxt(), command.getCaret());
            return;
        }
        // Emacs-style caret editing chords, intercepted the same way. Pure
        // caret movement must not refresh completion — re-filtering on
        // movement would reset the popup selection and make it jitter.
        if (event.isControlDown() || event.isAltDown()) {
            EditBuffer buffer = command.buffer();
            boolean handled = true, edited = false;
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case B -> buffer.left();
                    case F -> buffer.right();
                    case A -> buffer.home();
                    case E -> buffer.end();
                    case D -> { buffer.deleteAtCaret(); edited = true; }
                    case K -> { buffer.killToEnd(); edited = true; }
                    default -> handled = false;
                }
            } else {
                switch (event.getCode()) {
                    case B -> buffer.wordLeft();
                    case F -> buffer.wordRight();
                    case D -> { buffer.killWord(); edited = true; }
                    default -> handled = false;
                }
            }
            if (handled) {
                event.consume();
                if (edited) refreshCompletion();
                infoBar.setCommandTxt(command.getTxt(), command.getCaret());
                return;
            }
        }
        switch (event.getCode()) {
            case ESCAPE -> {
                if (completionPopup.isVisible()) {
                    // First ESC only dismisses the popup, keeping the typed
                    // text; the next one falls through and exits below.
                    completionPopup.hide();
                    commandCompletion.reset();
                    break;
                }
                if (enteringCommandInVISUAL) {
                    selectedCoords = new ArrayList<>();
                    enteringCommandInVISUAL = false;
                }
                currMode = Mode.NORMAL;
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
            }
            case ENTER -> {
                // Executing (or opening help) ends the completion session;
                // hide before the help early-return so the popup never
                // lingers over the help overlay.
                completionPopup.hide();
                commandCompletion.reset();
                if (command.getTxt().equals("h") ||
                    command.getTxt().equals("help") ||
                    command.getTxt().equals("?")) {
                    CommandResult result = CommandResult.NONE;
                    try {
                        result = command.execute(sheet);
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                    }
                    if (result == CommandResult.HELP) {
                        currMode = Mode.HELP;
                        helpMenu.show();
                        infoBar.setInfobarTxt(helpMenu.percentage());
                        statusBar.refresh();
                    }
                    command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                    return;
                }
                if (enteringCommandInVISUAL) {
                    try {
                        selectedCoords = new ArrayList<>();
                        enteringCommandInVISUAL = false;

                        StringBuilder destinationCoord = new StringBuilder();
                        int i;
                        for (i = command.getTxt().length() - 1; i > 0; i--) {
                            if (command.getTxt().charAt(i) == ' ') break;
                            destinationCoord.append(command.getTxt().charAt(i));
                        }
                        destinationCoord.reverse();

                        Cell c = sheet.findCell(destinationCoord.toString());
                        recordedCellStates.add(List.of(c.copy()));
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
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                    }
                }
                currMode = Mode.NORMAL;
                int prevXC = cellSelector.getXCoord(), prevYC = cellSelector.getYCoord();

                String commandError = null;
                CommandResult result = CommandResult.NONE;
                try {
                    result = command.execute(sheet);
                } catch (Exception e) {
                    commandError = e.getMessage();
                }

                if (result == CommandResult.QUIT) {
                    javafx.application.Platform.exit();
                    return;
                }

                // Relayout at the live camera offset (the command may have
                // resized columns/rows) and re-derive the cursor from it.
                camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
                goTo(prevXC, prevYC);
                if (commandError != null) infoBar.setInfobarTxt(commandError);
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
            }
            case BACK_SPACE -> {
                if (command.getTxt().equals("")) {
                    refreshCompletion();
                    infoBar.setInfobarTxt("COMMAND IS EMPTY");
                    // Return before the command-line repaint below, which
                    // would immediately overwrite the error with ":".
                    return;
                }
                command.buffer().backspace();
                refreshCompletion();
            }
            case TAB -> {
                event.consume();
                if (completingName()) cycleCompletion(!event.isShiftDown());
            }
            // Bare movement keys are free in COMMAND mode (unlike INSERT/
            // FORMULA, where they commit and move the selector); mirror the
            // Ctrl chords. Movement never refreshes completion.
            case LEFT -> command.buffer().left();
            case RIGHT -> command.buffer().right();
            case HOME -> command.buffer().home();
            case END -> command.buffer().end();
            default -> {
                // Other Ctrl/Alt chords and lone modifier presses (e.g. SHIFT
                // on its way to Shift+TAB) are not command text.
                if (event.isControlDown() || event.isAltDown() ||
                    event.getCode().isModifierKey()) break;
                command.buffer().insert(event.getText());
                refreshCompletion();
            }
        }
        if(currMode == Mode.COMMAND || currMode == Mode.VISUAL)
            infoBar.setCommandTxt(command.getTxt(), command.getCaret());
    }

    /**
     * Returns whether completion currently applies: the command name itself
     * is being typed — not a VISUAL-mode formula, and no arguments yet.
     */
    private boolean completingName() {
        return !enteringCommandInVISUAL && !command.getTxt().contains(" ");
    }

    /**
     * Refreshes the completion session and popup for the current command
     * text after it changed. While a name is being typed the popup filters
     * live with no match selected; on an empty line, an argument, or a
     * VISUAL-mode formula the session ends and the popup hides.
     */
    private void refreshCompletion() {
        if (completingName() && !command.getTxt().isEmpty()) {
            commandCompletion.update(command.getTxt());
            completionPopup.show(commandCompletion.getMatches(), -1);
        } else {
            commandCompletion.reset();
            completionPopup.hide();
        }
    }

    /**
     * Cycles the completion selection (TAB / Ctrl+N forward, Shift+TAB /
     * Ctrl+P backward), writing the selected match — or the original typed
     * text, once past the last match — into the command line.
     *
     * @param forward whether to step forward through the matches
     */
    private void cycleCompletion(boolean forward) {
        command.setTxt(forward
            ? commandCompletion.next(command.getTxt())
            : commandCompletion.previous(command.getTxt()));
        completionPopup.show(commandCompletion.getMatches(), commandCompletion.getSelectedIndex());
    }

    /** Accumulates digit characters to form a numeric multiplier in VISUAL mode. */
    String multiplierForVISUAL = "";
    /**
     * Handles keyboard input in VISUAL mode. Extends or shrinks the cell
     * selection using hjkl movement, supports numeric multipliers, and
     * provides actions on the selection: delete (d), yank (y), merge (m),
     * and entering commands (;).
     */
    private void visualSelection(@NotNull KeyEvent event) {
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

        if (enteringCommandInVISUAL) {
            commandInput(event);
            return;
        }

        switch (event.getCode()) {
            case DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4, DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9 ->
                multiplierForVISUAL += event.getText();
            case D -> {
                // Record the pre-images of every cell the delete will touch
                // as one undo step. deleteCell follows merge redirection, so
                // capture through findCell and de-duplicate: several selected
                // coords inside one merge all blank the same merge-start.
                List<Cell> deleteStep = new ArrayList<>();
                HashSet<List<Integer>> seen = new HashSet<>();
                for (int[] coord : selectedCoords) {
                    Cell c = sheet.findCell(coord[0], coord[1]);
                    if (!c.isEmpty() && seen.add(List.of(c.xCoord(), c.yCoord())))
                        deleteStep.add(c.copy());
                }
                if (!deleteStep.isEmpty()) {
                    recordedCellStates.add(deleteStep);
                    if (undoCounter != 0) editorOps.removeUltCStates();
                }
                selectedCoords.forEach(coord -> sheet.deleteCell(coord[0], coord[1]));
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case Y -> {
                clipboard.clear();
                selectedCoords.forEach(coord -> clipboard.add(sheet.findCell(coord[0], coord[1])));
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                camera.ready();
                cellSelector.readCell(camera.picture.data());
            }
            case M -> {
                boolean mergedCsInside = false;
                // Pre-images of every range dissolved below, recorded as one
                // undo step. Each range is captured just before its unmerge:
                // afterwards the cells' merge pointers are cleared, so the
                // remaining coords of the same range are skipped naturally.
                List<Cell> unmergeStep = new ArrayList<>();

                for (int[] coord : selectedCoords) {
                    Cell c = sheet.findCell(coord[0], coord[1]);
                    if (c.getMergeDelimiter() != null) {
                        int[] range = sheet.mergedRangeOf(c);
                        if (range != null)
                            unmergeStep.addAll(editorOps.captureRange(range[0], range[1], range[2], range[3]));
                        sheet.unmergeCells(c);
                        if (!mergedCsInside) mergedCsInside = true;
                    }
                }
                if (!unmergeStep.isEmpty()) {
                    recordedCellStates.add(unmergeStep);
                    if (undoCounter != 0) editorOps.removeUltCStates();
                }

                if (!mergedCsInside) {
                    int maxXC = Integer.MIN_VALUE, minXC = Integer.MAX_VALUE,
                        maxYC = Integer.MIN_VALUE, minYC = Integer.MAX_VALUE;
                    for (int[] c : selectedCoords) {
                        if (c[0] > maxXC) maxXC = c[0];
                        if (c[0] < minXC) minXC = c[0];
                        if (c[1] > maxYC) maxYC = c[1];
                        if (c[1] < minYC) minYC = c[1];
                    }

                    recordedCellStates.add(editorOps.captureRange(minXC, minYC, maxXC, maxYC));
                    if (undoCounter != 0) editorOps.removeUltCStates();
                    sheet.mergeCells(minXC, minYC, maxXC, maxYC);
                }

                selectedCoords = new ArrayList<>();
                camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                currMode = Mode.NORMAL;
                cellSelector.readCell(camera.picture.data());
                goingToMergeStart = false;
                editorOps.maybeGoToMergeStart();
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
                enteringCommandInVISUAL = true;
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
                    default -> {}
                }
                int currXC = cellSelector.getXCoord();
                int currYC = cellSelector.getYCoord();

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
    /** Adds a full column or row of coordinates to the visual selection. */
    private void addSCs(boolean isAddingCol, int currC, int minC, int maxC) {
        if (isAddingCol) for (int i = minC; i <= maxC; i++)
            selectedCoords.add(new int[]{currC, i});
        else for (int i = minC; i <= maxC; i++)
            selectedCoords.add(new int[]{i, currC});
    }
    /** Removes all selected coordinates in the given column or row ({@code -1} to skip). */
    private void purgeSCs(int col, int row) {
        if (col != -1)
            selectedCoords.removeIf(c -> c[0] == col);
        else
            selectedCoords.removeIf(c -> c[1] == row);
    }

    /** Prepares the selected cell's text for INSERT mode editing. Delegates to {@link EditorOperations}. */
    private void setSCTxtForTextInput() { editorOps.setSCTxtForTextInput(); }
    /**
     * Handles keyboard input in INSERT mode. Characters are appended to the
     * selected cell's text. ESC saves and exits, Shift-ESC cancels,
     * arrow keys/Enter/Tab save and move to an adjacent cell,
     * and Alt+hjkl saves and moves while staying in INSERT mode.
     */
    private void textInput(@NotNull KeyEvent event) {
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
                        default -> {}
                    }
                    recordedCellStates.add(List.of(cellSelector.getSelectedCell().copy()));
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
                    default -> {}
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

    /**
     * Handles keyboard input in FORMULA mode. Characters are appended to the
     * formula expression. ENTER evaluates the formula and saves the result,
     * ESC cancels, and Alt+hjkl evaluates, saves, moves, and re-enters
     * FORMULA mode on the next cell.
     */
    private void formulaInput(@NotNull KeyEvent event) {
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
                            default -> {}
                        }
                        recordedCellStates.add(List.of(cellSelector.getSelectedCell().copy()));
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

    /**
     * Shows the recording indicator for the given macro register. No-op when
     * the view is not wired up (headless unit tests).
     *
     * @param name the register name of the macro being recorded
     */
    void showRecordingIndicator(char name) {
        if (recordingIndicator != null) recordingIndicator.setRecording(name);
    }

    /** Hides the recording indicator. No-op when the view is not wired up. */
    void hideRecordingIndicator() {
        if (recordingIndicator != null) recordingIndicator.clearRecording();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        CANVAS_W = (int) canvas.getWidth();
        CANVAS_H = (int) canvas.getHeight();
        statusBar = new StatusBar(statusLabel, () -> currMode);
        infoBar = new InfoBar(infoText, infoCaret, infoCaretChar, exprLabel);
        coordsInfo = new CoordsInfo(coordsLabel);
        recordingIndicator = new RecordingIndicator(recordingIndicatorLabel);
        helpMenu = new HelpMenu(helpLabel);
        completionPopup = new CompletionPopup(completionLabelBox, overlayPane);
        sheet = new Sheet();
        sheet.setPositions(new Positions(
            CANVAS_W - GUTTER_W,
            CANVAS_H - HEADER_H,
            DEFAULT_CELL_W,
            DEFAULT_CELL_H,
            new HashMap<>(),
            new HashMap<>()
        ));
        sheet.setFileIOCallbacks(new FileIOCallbacks() {
            @Override
            public void onFileSaved(String filename) {
                statusBar.setFilename(filename);
            }

            @Override
            public void onFileLoaded(String filename) {
                macros = new HashMap<>();
                reset();
                statusBar.setFilename(filename);
            }
        });
        macros = new HashMap<>();
        reset();
        if (arg1 != null) {
            try {
                sheet.readFile(arg1);
                updateVisualState();
            } catch (Exception e) {
                if (e.getMessage().equals("New file")) {
                    updateVisualState();
                    infoBar.setInfobarTxt("New file");
                }
                else infoBar.setInfobarTxt(e.getMessage());
            }
        }
        // Track the available area: canvasStack has VBox.vgrow=ALWAYS, so its
        // laid-out size follows the window. At FXML-load time it is 0x0 and
        // nothing fires; the first layout pulse delivers the initial size,
        // which matches the FXML-seeded CANVAS_W/H and no-ops.
        canvasStack.widthProperty().addListener((obs, oldV, newV) -> onCanvasStackResized());
        canvasStack.heightProperty().addListener((obs, oldV, newV) -> onCanvasStackResized());
    }

    /** Applies the canvas stack's laid-out size to the grid viewport. */
    private void onCanvasStackResized() {
        int w = (int) canvasStack.getWidth(), h = (int) canvasStack.getHeight();
        if (w <= 0 || h <= 0) return;
        editorOps.applyViewportSize(w, h);
    }

    /**
     * Updates the tracked canvas dimensions and resizes the canvas node.
     * Must run before any repaint: resizing a {@link Canvas} clears it.
     *
     * @param w the new canvas width in pixels
     * @param h the new canvas height in pixels
     */
    void setCanvasSize(int w, int h) {
        CANVAS_W = w;
        CANVAS_H = h;
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    /**
     * Returns the y coordinate of the bottom edge of the grid viewport.
     * With bottom chrome on the scene graph, this is the canvas height.
     *
     * @return the grid viewport's bottom edge, in canvas pixels
     */
    int viewportBottom() {
        return CANVAS_H;
    }

    /**
     * Resets all UI components and state to their defaults. Called on
     * initialization, after loading a file, and from {@link Sheet#readFile(String)}.
     */
    void reset() {
        camera = new Camera(
            GUTTER_W,
            HEADER_H,
            CANVAS_W-GUTTER_W,
            viewportBottom()-HEADER_H,
            DEFAULT_CELL_C,
            sheet.getPositions(),
            sheet.getCellsFormatting()
        );

        int maxXC = Integer.MIN_VALUE, maxYC = Integer.MIN_VALUE;
        for (Cell c : sheet.getCells()) {
            if (c.isMergeStart()) {
                if (c.getMergeDelimiter().xCoord() > maxXC) maxXC = c.getMergeDelimiter().xCoord();
                if (c.getMergeDelimiter().yCoord() > maxYC) maxYC = c.getMergeDelimiter().yCoord();
            } else {
                if (c.xCoord() > maxXC) maxXC = c.xCoord();
                if (c.yCoord() > maxYC) maxYC = c.yCoord();
            }
        }
        camera.picture.metadata().setMaxXC(maxXC);
        camera.picture.metadata().setMaxYC(maxYC);
        selectedCoords = new ArrayList<>();
        camera.picture.metadata().generate(camera.getAbsX(), camera.getAbsY());
        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());

        cellSelector = new CellSelector(
            SELECT_TINT,
            camera,
            camera.picture.metadata(),
            sheet.getCellsFormatting(),
            () -> currMode
        );
        firstCol = new FirstCol(
            0,
            HEADER_H,
            GUTTER_W,
            viewportBottom()-HEADER_H,
            HEADER_FILL_H,
            camera,
            camera.picture.metadata()
        );
        firstRow = new FirstRow(
            GUTTER_W,
            0,
            CANVAS_W-GUTTER_W,
            HEADER_H,
            HEADER_FILL_V,
            camera,
            camera.picture.metadata()
        );

        currMode = Mode.NORMAL;
        enteringCommandInVISUAL = false;
        editorOps = new EditorOperations(this);
        keyCommand = new KeyCommand(editorOps, this);
        command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
        staticPrevXC = cellSelector.getXCoord();
        staticPrevYC = cellSelector.getYCoord();
        recordedCellStates = new LinkedList<>();
        undoneCellStates = new LinkedList<>();
        undoCounter = 0;
        clipboard = new ArrayList<>();

        camera.ready();
        firstCol.draw(gc);
        firstRow.draw(gc);
        statusBar.setFilename("new_file");
        statusBar.refresh();
        coordsInfo.setCoords(cellSelector.getXCoord(), cellSelector.getYCoord());
        cellSelector.readCell(camera.picture.data());
        cellSelector.draw(gc);
        infoBar.setIBarExpr("");
        infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
        sheet.setPositions(camera.picture.metadata());
    }
}

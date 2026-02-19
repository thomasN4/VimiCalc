package vimicalc.controller;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Command;
import vimicalc.model.Formula;

import java.util.*;

import static vimicalc.controller.Controller.*;
import static vimicalc.utils.Conversions.coordsStrToInts;
import static vimicalc.utils.Conversions.isNumber;
import static vimicalc.view.InfoBar.iBarExpr;

/**
 * Processes multi-key command sequences entered in NORMAL mode.
 *
 * <p>Accumulates characters into an expression string and evaluates it
 * when a complete command is recognized. Supports:</p>
 * <ul>
 *   <li><b>Movement:</b> h, j, k, l (with numeric multipliers like "5j")</li>
 *   <li><b>Cell operations:</b> dd (delete), yy/yd (yank/cut), pp (paste)</li>
 *   <li><b>Navigation:</b> g{coords} (go-to), Ctrl-O (jump back)</li>
 *   <li><b>Editing:</b> i/a (enter INSERT), = (enter FORMULA), v (enter VISUAL)</li>
 *   <li><b>Undo/redo:</b> u (undo), r (redo)</li>
 *   <li><b>Macros:</b> q{letter} (record), @{letter} (play), . (repeat last)</li>
 *   <li><b>Conditionals:</b> &lt;formula{then}{else} (conditional execution)</li>
 *   <li><b>Cell ref:</b> ${coords}{movement} (use cell value as multiplier; last char must be a movement key)</li>
 *   <li><b>Range operations:</b> {multiplier}{func}{multiplier}{dir}{dir} (e.g. "d5j" to delete 5 cells down)</li>
 *   <li><b>Quit shortcuts:</b> ZZ (save+quit), ZQ (quit without saving)</li>
 * </ul>
 */
public class KeyCommand {
    /** "Field functions" — operations that can be repeated across cell ranges (e.g. delete, paste).
     *  Note: 'y' (yank) is excluded because range-yank is handled separately in VISUAL mode. */
    private final HashSet<Character> Ffuncs = new HashSet<>(Set.of('d', 'p'));
    /** "Movement functions" — the four directional keys used in Vim-style navigation. */
    public static final HashSet<Character> Mfuncs = new HashSet<>(Set.of('h', 'j', 'k', 'l'));
    /** The current key-command expression being built. */
    private String expr;
    /** The previous completed expression, replayed by the {@code .} command. */
    private String prevExpr;
    /** The macro currently being recorded. */
    protected static LinkedList<KeyEvent> currMacro;
    /** Whether a macro is currently being recorded. */
    protected static boolean recordingMacro;
    /** Whether the info bar expression display should be updated on each keystroke. */
    protected static boolean canChangeIBarExpr;

    /** Creates a new KeyCommand handler with empty expression state. */
    public KeyCommand() {
        expr = "";
        prevExpr = "";
        recordingMacro = false;
        canChangeIBarExpr = true;
    }

    /**
     * Processes a key event in NORMAL mode. Maps special keys to their
     * Vim equivalents, appends the character to the expression, updates
     * the info bar display, and triggers evaluation.
     *
     * @param event the key event from the scene
     */
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
            case RIGHT, BACK_SPACE -> c = 'l';  // BACK_SPACE maps to move-right ('l') in NORMAL mode
            case TAB -> {
                if (event.isShiftDown()) c = 'h';
                else c = 'l';
            }
            case INSERT -> c = 'i';
            case DELETE -> c = 'd';
            case SEMICOLON -> {
                currMode = Mode.COMMAND;
                command = new Command("", cellSelector.getXCoord(), cellSelector.getYCoord());
                infoBar.setCommandTxt(command.getTxt());
                expr = ""; return;
            }
            case O -> {
                if (event.isControlDown())
                    goTo(staticPrevXC, staticPrevYC);
            }
            case C -> {
                if (event.isControlDown()) {
                    expr = "";
                    iBarExpr = "";
                    return;
                }
            }
        }

        if (c != 0) expr += c;
        if (canChangeIBarExpr) {
            iBarExpr = expr;
            infoBar.draw(gc);
        }
        evaluate(expr);
    }

    private @NotNull String macroStr(@NotNull LinkedList<KeyEvent> macro) {
        StringBuilder str = new StringBuilder();
        for (KeyEvent event : macro) {
            str.append(event.getText());
        }
        return str.toString();
    }

    /**
     * Replays a previously recorded macro by re-dispatching its key events.
     * Temporarily pauses macro recording if one is in progress to avoid
     * recording the replay into itself.
     *
     * @param macroName the single-character macro identifier
     */
    private void runMacro(char macroName) {
        try {
            boolean aMacroIsInFactBeingRecorded = recordingMacro;
            if (aMacroIsInFactBeingRecorded) recordingMacro = false;
            System.out.println("Trying to run a macro...");
            KeyEvent[] macro = macros.get(macroName).toArray(new KeyEvent[0]);
            System.out.println("The macro: " + macroStr(macros.get(macroName)));
            for (KeyEvent event : macro) onKeyPressed(event);
            if (aMacroIsInFactBeingRecorded) recordingMacro = true;
            System.out.println("Macro execution finished");
        } catch (Exception e) {
            infoBar.setInfobarTxt("Macro '" + macroName + "' doesn't exist.");
            System.out.println(e.getMessage());
            expr = "";
        }
    }

    /**
     * Parses a numeric multiplier prefix starting at the given index.
     *
     * @param subI the starting index in the expression
     * @param expr the expression string
     * @return {@code int[]{functionIndex, multiplier}} — the index of the first
     *         non-digit character and the parsed multiplier (1 if none)
     */
    public int[] parseFIndexAndMult(int subI, @NotNull String expr) {
        StringBuilder multStr = new StringBuilder();
        int i = subI;
        for (; isNumber(""+expr.charAt(i)); i++)
            multStr.append(expr.charAt(i));
        if (i == subI)
            return new int[]{i, 1};
        return new int[]{i, Integer.parseInt(multStr.toString())};
    }

    /**
     * Evaluates a (possibly partial) key-command expression. If the expression
     * forms a complete command, executes it and resets. If not yet complete
     * (e.g. waiting for more characters), returns and waits for the next keystroke.
     *
     * <p>Handles range operations (e.g. "d5j3l" — delete across a 5x3 area)
     * by building a temporary macro of individual operations, then executing it.</p>
     *
     * @param expr the expression to evaluate
     */
    public void evaluate(@NotNull String expr) {
        boolean evaluationFinished = false;
        if (expr.equals("")) return;
        char lastChar = expr.charAt(expr.length() - 1), beforeLastChar = 0;
        if (expr.length() > 1) beforeLastChar = expr.charAt(expr.length() - 2);
        if (isNumber("" + lastChar)) return;
        int[] fstFIandM = parseFIndexAndMult(0, expr);  // item 1 : indexe de la fonction, item 2 : multiplicateur

        if (fstFIandM[0] >= expr.length()) return;
        char fstFunc = expr.charAt(fstFIandM[0]);

        if (Ffuncs.contains(fstFunc) && Mfuncs.contains(expr.charAt(expr.length() - 1))) {
            System.out.println("Executing one of *these* KeyCommand functions...");
            ArrayList<String> tempMacro = new ArrayList<>();
            int toIgnore = 0;
            tempMacro.add(fstFunc + "" + fstFunc);

            int[] sndFIandM = parseFIndexAndMult(fstFIandM[0] + 1, expr);
            char sndFunc = expr.charAt(sndFIandM[0]);
            if (sndFIandM[0] == expr.length() - 1) {
                for (int i = 1; i <= sndFIandM[1]; i++) {
                    tempMacro.add("" + sndFunc);
                    tempMacro.add(fstFunc + "" + fstFunc);
                }
            } else {
                int[] trdFIandM = parseFIndexAndMult(sndFIandM[0] + 1, expr);
                char trdFunc = expr.charAt(expr.length() - 1);
                fstFIandM[1] *= trdFIandM[1] + 1;
                sndFunc = Character.toLowerCase(sndFunc);
                char invSndFunc = 0;
                switch (sndFunc) {
                    case 'h' -> invSndFunc = 'l';
                    case 'j' -> invSndFunc = 'k';
                    case 'k' -> invSndFunc = 'j';
                    case 'l' -> invSndFunc = 'h';
                }
                for (int i = 1; i <= sndFIandM[1]; i++) {
                    tempMacro.add("" + sndFunc);
                    tempMacro.add(fstFunc + "" + fstFunc);
                }
                tempMacro.add("" + trdFunc);
                tempMacro.add(sndFIandM[1] + "" + invSndFunc);
                toIgnore = 2;
            }

            System.out.println("Special macro: " + tempMacro);
            for (int i = 0; i < fstFIandM[1]; i++) {
                for (int j = 0; j < tempMacro.size(); j++) {
                    if (i == fstFIandM[1] - 1 && j == tempMacro.size() - toIgnore) {
                        this.expr = "";
                        return;
                    } else evaluate(tempMacro.get(j));
                }
            }
        }

        for (int i = 0; i < fstFIandM[1]; i++) {
            switch (fstFunc) {
                case '=' -> {
                    recordedCellStates.add(cellSelector.getSelectedCell().copy());
                    currMode = Mode.FORMULA;
                    cellSelector.readCell(camera.picture.data());
                    if (cellSelector.getSelectedCell().formula() == null)
                        cellSelector.getSelectedCell().setFormula(
                            new Formula("", cellSelector.getXCoord(), cellSelector.getYCoord())
                        );
                    infoBar.setEnteringFormula(cellSelector.getSelectedCell().formula().getTxt());
                    evaluationFinished = true;
                }
                case '$' -> {
                    if (expr.length() > 3 &&
                            isNumber(""+beforeLastChar) &&
                            !isNumber("" + lastChar)) {
                        try {
                            this.expr = "" +
                                (int) Math.floor(sheet.findCell(expr.substring(1, expr.length() - 1)).value()) +
                                lastChar;
                            evaluate(this.expr);
                        } catch (Exception ignored) {
                            evaluationFinished = true;
                        }
                    }
                }
                case '<' -> {
                    System.out.println("Entering conditional keyCommand...");
                    try {
                        if (expr.length() > 5 && lastChar == '}') {
                            StringBuilder cond = new StringBuilder(),
                                    thenBlock = new StringBuilder(),
                                    elseBlock = new StringBuilder();
                            int pos = 1;
                            for (; expr.charAt(pos) != '{'; ++pos)
                                cond.append(expr.charAt(pos));
                            for (++pos; expr.charAt(pos) != '}' && expr.charAt(pos) != '{'; ++pos)
                                thenBlock.append(expr.charAt(pos));
                            if ((new Formula(cond.toString(), 0, 0)).interpret(sheet) != 0)
                                evaluate(thenBlock.toString());
                            else if (expr.charAt(pos) == '{') {
                                for (++pos; expr.charAt(pos) != '}'; ++pos)
                                    elseBlock.append(expr.charAt(pos));
                                evaluate(elseBlock.toString());
                            }
                            evaluationFinished = true;
                        }
                    } catch (Exception e) {
                        infoBar.setInfobarTxt(e.getMessage());
                        evaluationFinished = true;
                    }
                }
                case 'q' -> {
                    if (!recordingMacro && expr.length() - 1 > fstFIandM[0]) {
                        char arg = expr.charAt(fstFIandM[0] + 1);
                        infoBar.setInfobarTxt("Recording macro '" + arg + "' ...");
                        currMacro = new LinkedList<>();
                        macros.put(expr.charAt(fstFIandM[0] + 1), currMacro);
                        recordingMacro = true;
                        evaluationFinished = true;
                    } else if (recordingMacro) {
                        infoBar.setInfobarTxt("Macro recorded");
                        currMacro.removeLast();
                        System.out.println("Recorded macro: " + macroStr(currMacro));
                        recordingMacro = false;
                        evaluationFinished = true;
                    }
                }
                case '@' -> {
                    if (expr.length() > fstFIandM[0] + 1) {
                        char arg = expr.charAt(fstFIandM[0] + 1);
                        this.expr = "";
                        canChangeIBarExpr = false;
                        runMacro(arg);
                        canChangeIBarExpr = true;
                        prevExpr = expr;
                        this.expr = "";
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
                case 'g' -> {  // Aller vers une coordonnée précise, par exemple, 'gA3'
                    if (!Character.isAlphabetic(lastChar) &&
                        !isNumber(""+lastChar) && !isNumber(""+beforeLastChar))
                        evaluationFinished = true;
                    else if (expr.length() > 2 && isNumber(""+beforeLastChar) && !isNumber("" + lastChar)) {
                        int[] coords = coordsStrToInts(expr.substring(1, expr.length() - 1));
                        goToAndRemember(coords[0], coords[1], cellSelector.getXCoord(), cellSelector.getYCoord());
                        cellContentToIBar();
                        this.expr = ""+lastChar;
                        evaluate(this.expr);
                    }
                }
                case 'd' -> {
                    if (expr.length() > 1 && expr.charAt(fstFIandM[0] + 1) == 'd') {
                        System.out.println("Trying to delete a cell's content...");
                        if (cellSelector.getSelectedCell().txt() == null) {
                            infoBar.setInfobarTxt("CAN'T DELETE RIGHT NOW");
                            sheet.deleteDependency(cellSelector.getXCoord(), cellSelector.getYCoord());
                        } else {
                            recordedCellStates.add(cellSelector.getSelectedCell().copy());
                            sheet.deleteCell(cellSelector.getXCoord(), cellSelector.getYCoord());
                            camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                            camera.ready();
                            cellSelector.readCell(camera.picture.data());
                            infoBar.setInfobarTxt(cellSelector.getSelectedCell().txt());
                            if (undoCounter != 0) removeUltCStates();
                        }
                        evaluationFinished = true;
                    }
                }
                case 'm' -> {
                    sheet.unmergeCells(sheet.findCell(coordsInfo.getCoords()));
                    camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                    camera.ready();
                    cellSelector.readCell(camera.picture.data());
                    evaluationFinished = true;
                }
                case 'u' -> {
                    if (undoCounter >= recordedCellStates.size())
                        infoBar.setInfobarTxt("Already at earliest change.");
                    else {
                        undo();
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    System.out.println("Recorded cell states: ");
                    recordedCellStates.forEach(c -> System.out.println("xC = " + c.xCoord() + ", yC = " + c.yCoord()));
                    evaluationFinished = true;
                }
                case 'r' -> {
                    if (undoCounter == 0 || recordedCellStates.size() == 0)
                        infoBar.setInfobarTxt("Already at latest change.");
                    else {
                        redo();
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                    }
                    System.out.println("Recorded cell states: ");
                    recordedCellStates.forEach(c -> System.out.println("xC = " + c.xCoord() + ", yC = " + c.yCoord()));
                    evaluationFinished = true;
                }
                case 'y' -> {
                    if (expr.length() > 1) {
                        char arg1 = expr.charAt(fstFIandM[0] + 1);
                        if (arg1 == 'y' || arg1 == 'd') {
                            if (cellSelector.getSelectedCell().txt() == null)
                                infoBar.setInfobarTxt("CAN'T COPY, CELL IS EMPTY");
                            else {
                                clipboard.clear();
                                clipboard.add(cellSelector.getSelectedCell().copy());
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
                    if (expr.length() > 1 && expr.charAt(fstFIandM[0] + 1) == 'p') {
                        if (clipboard == null) infoBar.setInfobarTxt("CAN'T PASTE, NOTHING HAS BEEN COPIED YET");
                        else {
                            if (clipboard.size() == 1) paste(0);
                            else {
                                int xCStart = cellSelector.getXCoord(), yCStart = cellSelector.getYCoord();
                                for (int j = 0; j < clipboard.size()-1; j++) {
                                    paste(j);
                                    goTo(
                                        xCStart + (clipboard.get(j+1).xCoord() - clipboard.get(0).xCoord()),
                                        yCStart + (clipboard.get(j+1).yCoord() - clipboard.get(0).yCoord())
                                    );
                                }
                                paste(clipboard.size()-1);
                            }
                        }
                        camera.picture.take(gc, sheet, selectedCoords, camera.getAbsX(), camera.getAbsY());
                        camera.ready();
                        cellSelector.readCell(camera.picture.data());
                        evaluationFinished = true;
                    }
                }
                case 'a', 'i' -> {
                    recordedCellStates.add(cellSelector.getSelectedCell().copy());
                    setSCTxtForTextInput();
                    currMode = Mode.INSERT;
                    cellSelector.draw(gc);
                    evaluationFinished = true;
                }
                case 'v' -> {
                    currMode = Mode.VISUAL;
                    cellSelector.readCell(camera.picture.data());
                    selectedCoords.add(new int[]{cellSelector.getXCoord(), cellSelector.getYCoord()});
                    evaluationFinished = true;
                }
                case 'Z' -> {
                    if (expr.length() > 1) {
                        char arg = expr.charAt(fstFIandM[0] + 1);
                        if (arg == 'Q')
                            command = new Command("q", 0, 0);
                        else if (arg == 'Z')
                            command = new Command("wq", 0, 0);
                        else {
                            this.expr = "";
                            return;
                        }
                        try {
                            command.interpret(sheet);
                        } catch (Exception e) {
                            infoBar.setInfobarTxt(e.getMessage());
                            evaluationFinished = true;
                        }
                    }
                }
                case '.' -> {
                    iBarExpr = prevExpr;
                    evaluate(prevExpr);
                    this.expr = "";
                }
                default -> evaluationFinished = true;
            }
        }

        if (evaluationFinished) {
            prevExpr = expr;
            this.expr = "";
        }
    }
}

package vimicalc.controller;

import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import vimicalc.model.Command;
import vimicalc.model.Formula;

import java.util.*;

import static vimicalc.utils.Conversions.coordsStrToInts;
import static vimicalc.utils.Conversions.isNumber;


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
 *   <li><b>Range operations:</b> {func}{multiplier}{dir} (e.g. "d5j" to delete 5 cells down)</li>
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
    protected LinkedList<KeyEvent> currMacro;
    /** Whether a macro is currently being recorded. */
    protected boolean recordingMacro;
    /** Whether the info bar expression display should be updated on each keystroke. */
    protected boolean canChangeIBarExpr;
    /** The editor operations handler for movement, navigation, and editing. */
    private final EditorOperations ops;
    /** The controller whose fields this instance operates on. */
    private final Controller ctrl;

    /**
     * Creates a new KeyCommand handler with empty expression state.
     *
     * @param ops the editor operations handler to delegate movement/editing to
     * @param ctrl the controller whose fields this instance operates on
     */
    public KeyCommand(EditorOperations ops, Controller ctrl) {
        this.ops = ops;
        this.ctrl = ctrl;
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
                ctrl.currMode = Mode.COMMAND;
                ctrl.command = new Command("", ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
                ctrl.infoBar.setCommandTxt(ctrl.command.getTxt());
                expr = ""; return;
            }
            case O -> {
                if (event.isControlDown())
                    ops.goTo(ctrl.staticPrevXC, ctrl.staticPrevYC);
            }
            case C -> {
                if (event.isControlDown()) {
                    expr = "";
                    ctrl.infoBar.setIBarExpr("");
                    return;
                }
            }
            default -> {}
        }

        if (c != 0) expr += c;
        if (canChangeIBarExpr) {
            ctrl.infoBar.setIBarExpr(expr);
            ctrl.infoBar.draw(ctrl.gc);
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
            KeyEvent[] macro = ctrl.macros.get(macroName).toArray(new KeyEvent[0]);
            System.out.println("The macro: " + macroStr(ctrl.macros.get(macroName)));
            for (KeyEvent event : macro) ctrl.onKeyPressed(event);
            if (aMacroIsInFactBeingRecorded) recordingMacro = true;
            System.out.println("Macro execution finished");
        } catch (Exception e) {
            ctrl.infoBar.setInfobarTxt("Macro '" + macroName + "' doesn't exist.");
            System.out.println(e.getMessage());
            expr = "";
        }
    }

    record Prefix(int funcIndex, int multiplier) {}

    public Prefix parsePrefix(int startIndex, @NotNull String expr) {
        int i = startIndex;
        while (i < expr.length() && Character.isDigit(expr.charAt(i)))
            i++;
        int multiplier = (i == startIndex) ? 1 : Integer.parseInt(expr.substring(startIndex, i));
        return new Prefix(i, multiplier);
    }

    /**
     * Evaluates a (possibly partial) key-command expression. If the expression
     * forms a complete command, executes it and resets. If not yet complete
     * (e.g. waiting for more characters), returns and waits for the next keystroke.
     *
     * <p>Handles range operations (e.g. "d5j" — delete 5 cells down)
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
        Prefix first = parsePrefix(0, expr);

        if (first.funcIndex >= expr.length()) return;
        char fstFunc = expr.charAt(first.funcIndex);

        if (Ffuncs.contains(fstFunc) && Mfuncs.contains(expr.charAt(expr.length() - 1))) {
            Prefix second = parsePrefix(first.funcIndex + 1, expr);
            char direction = expr.charAt(second.funcIndex);

            ArrayList<String> tempMacro = new ArrayList<>();
            tempMacro.add(fstFunc + "" + fstFunc);
            for (int i = 1; i <= second.multiplier; i++) {
                tempMacro.add("" + direction);
                tempMacro.add(fstFunc + "" + fstFunc);
            }

            for (int i = 0; i < first.multiplier; i++)
                for (String cmd : tempMacro)
                    evaluate(cmd);
            this.expr = "";
            return;
        }

        for (int i = 0; i < first.multiplier; i++) {
            switch (fstFunc) {
                case '=' -> {
                    ctrl.recordedCellStates.add(ctrl.cellSelector.getSelectedCell().copy());
                    ctrl.currMode = Mode.FORMULA;
                    ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                    if (ctrl.cellSelector.getSelectedCell().formula() == null)
                        ctrl.cellSelector.getSelectedCell().setFormula(
                            new Formula("", ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord())
                        );
                    ctrl.infoBar.setEnteringFormula(ctrl.cellSelector.getSelectedCell().formula().getTxt());
                    evaluationFinished = true;
                }
                case '$' -> {
                    if (expr.length() > 3 &&
                            isNumber(""+beforeLastChar) &&
                            !isNumber("" + lastChar)) {
                        try {
                            this.expr = "" +
                                (int) Math.floor(ctrl.sheet.findCell(expr.substring(1, expr.length() - 1)).value()) +
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
                            if ((new Formula(cond.toString(), 0, 0)).interpret(ctrl.sheet) != 0)
                                evaluate(thenBlock.toString());
                            else if (expr.charAt(pos) == '{') {
                                for (++pos; expr.charAt(pos) != '}'; ++pos)
                                    elseBlock.append(expr.charAt(pos));
                                evaluate(elseBlock.toString());
                            }
                            evaluationFinished = true;
                        }
                    } catch (Exception e) {
                        ctrl.infoBar.setInfobarTxt(e.getMessage());
                        evaluationFinished = true;
                    }
                }
                case 'q' -> {
                    if (!recordingMacro && expr.length() - 1 > first.funcIndex) {
                        char arg = expr.charAt(first.funcIndex + 1);
                        ctrl.infoBar.setInfobarTxt("Recording macro '" + arg + "' ...");
                        currMacro = new LinkedList<>();
                        ctrl.macros.put(expr.charAt(first.funcIndex + 1), currMacro);
                        recordingMacro = true;
                        evaluationFinished = true;
                    } else if (recordingMacro) {
                        ctrl.infoBar.setInfobarTxt("Macro recorded");
                        currMacro.removeLast();
                        System.out.println("Recorded macro: " + macroStr(currMacro));
                        recordingMacro = false;
                        evaluationFinished = true;
                    }
                }
                case '@' -> {
                    if (expr.length() > first.funcIndex + 1) {
                        char arg = expr.charAt(first.funcIndex + 1);
                        this.expr = "";
                        canChangeIBarExpr = false;
                        runMacro(arg);
                        canChangeIBarExpr = true;
                        prevExpr = expr;
                        this.expr = "";
                    }
                }
                case 'h' -> {
                    ops.moveLeft();
                    evaluationFinished = true;
                }
                case 'j' -> {
                    ops.moveDown();
                    evaluationFinished = true;
                }
                case 'k' -> {
                    ops.moveUp();
                    evaluationFinished = true;
                }
                case 'l' -> {
                    ops.moveRight();
                    evaluationFinished = true;
                }
                case 'g' -> {  // Aller vers une coordonnée précise, par exemple, 'gA3'
                    if (!Character.isAlphabetic(lastChar) &&
                        !isNumber(""+lastChar) && !isNumber(""+beforeLastChar))
                        evaluationFinished = true;
                    else if (expr.length() > 2 && isNumber(""+beforeLastChar) && !isNumber("" + lastChar)) {
                        int[] coords = coordsStrToInts(expr.substring(1, expr.length() - 1));
                        ops.goToAndRemember(coords[0], coords[1], ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
                        ops.cellContentToIBar();
                        this.expr = ""+lastChar;
                        evaluate(this.expr);
                    }
                }
                case 'd' -> {
                    if (expr.length() > 1) {
                        if (expr.charAt(first.funcIndex + 1) == 'd') {
                            System.out.println("Trying to delete a cell's content...");
                            if (ctrl.cellSelector.getSelectedCell().txt() == null) {
                                ctrl.infoBar.setInfobarTxt("CAN'T DELETE RIGHT NOW");
                                ctrl.sheet.deleteDependency(ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
                            } else {
                                ctrl.recordedCellStates.add(ctrl.cellSelector.getSelectedCell().copy());
                                ctrl.sheet.deleteCell(ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord());
                                ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                                ctrl.camera.ready();
                                ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                                ctrl.infoBar.setInfobarTxt(ctrl.cellSelector.getSelectedCell().txt());
                                if (ctrl.undoCounter != 0) ops.removeUltCStates();
                            }
                        } else
                            ctrl.infoBar.setInfobarTxt("Invalid command: " + expr);
                        evaluationFinished = true;
                    }
                }
                case 'm' -> {
                    ctrl.sheet.unmergeCells(ctrl.sheet.findCell(ctrl.coordsInfo.getCoords()));
                    ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                    ctrl.camera.ready();
                    ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                    evaluationFinished = true;
                }
                case 'u' -> {
                    if (ctrl.undoCounter >= ctrl.recordedCellStates.size())
                        ctrl.infoBar.setInfobarTxt("Already at earliest change.");
                    else {
                        ops.undo();
                        ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                        ctrl.camera.ready();
                        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                    }
                    System.out.println("Recorded cell states: ");
                    ctrl.recordedCellStates.forEach(c -> System.out.println("xC = " + c.xCoord() + ", yC = " + c.yCoord()));
                    evaluationFinished = true;
                }
                case 'r' -> {
                    if (ctrl.undoCounter == 0 || ctrl.recordedCellStates.size() == 0)
                        ctrl.infoBar.setInfobarTxt("Already at latest change.");
                    else {
                        ops.redo();
                        ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                        ctrl.camera.ready();
                        ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                    }
                    System.out.println("Recorded cell states: ");
                    ctrl.recordedCellStates.forEach(c -> System.out.println("xC = " + c.xCoord() + ", yC = " + c.yCoord()));
                    evaluationFinished = true;
                }
                case 'y' -> {
                    if (expr.length() > 1) {
                        char arg1 = expr.charAt(first.funcIndex + 1);
                        if (arg1 == 'y' || arg1 == 'd') {
                            if (ctrl.cellSelector.getSelectedCell().txt() == null)
                                ctrl.infoBar.setInfobarTxt("CAN'T COPY, CELL IS EMPTY");
                            else {
                                ctrl.clipboard.clear();
                                ctrl.clipboard.add(ctrl.cellSelector.getSelectedCell().copy());
                            }
                            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                            ctrl.camera.ready();
                            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                            if (arg1 == 'd') evaluate("dd");
                        } else
                            ctrl.infoBar.setInfobarTxt("Invalid command: " + expr);
                        evaluationFinished = true;
                    }
                }
                case 'p' -> {
                    if (expr.length() > 1) {
                        if (expr.charAt(first.funcIndex + 1) == 'p') {
                            if (ctrl.clipboard == null || ctrl.clipboard.isEmpty()) ctrl.infoBar.setInfobarTxt("CAN'T PASTE, NOTHING HAS BEEN COPIED YET");
                            else {
                                if (ctrl.clipboard.size() == 1) ops.paste(0);
                                else {
                                    int xCStart = ctrl.cellSelector.getXCoord(), yCStart = ctrl.cellSelector.getYCoord();
                                    for (int j = 0; j < ctrl.clipboard.size()-1; j++) {
                                        ops.paste(j);
                                        ops.goTo(
                                            xCStart + (ctrl.clipboard.get(j+1).xCoord() - ctrl.clipboard.get(0).xCoord()),
                                            yCStart + (ctrl.clipboard.get(j+1).yCoord() - ctrl.clipboard.get(0).yCoord())
                                        );
                                    }
                                    ops.paste(ctrl.clipboard.size()-1);
                                }
                            }
                            ctrl.camera.picture.take(ctrl.gc, ctrl.sheet, ctrl.selectedCoords, ctrl.camera.getAbsX(), ctrl.camera.getAbsY());
                            ctrl.camera.ready();
                            ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                        } else
                            ctrl.infoBar.setInfobarTxt("Invalid command: " + expr);
                        evaluationFinished = true;
                    }
                }
                case 'a', 'i' -> {
                    ctrl.recordedCellStates.add(ctrl.cellSelector.getSelectedCell().copy());
                    ops.setSCTxtForTextInput();
                    ctrl.currMode = Mode.INSERT;
                    ctrl.cellSelector.draw(ctrl.gc);
                    evaluationFinished = true;
                }
                case 'v' -> {
                    ctrl.currMode = Mode.VISUAL;
                    ctrl.cellSelector.readCell(ctrl.camera.picture.data());
                    ctrl.selectedCoords.add(new int[]{ctrl.cellSelector.getXCoord(), ctrl.cellSelector.getYCoord()});
                    evaluationFinished = true;
                }
                case 'Z' -> {
                    if (expr.length() > 1) {
                        char arg = expr.charAt(first.funcIndex + 1);
                        if (arg == 'Q')
                            ctrl.command = new Command("q", 0, 0);
                        else if (arg == 'Z')
                            ctrl.command = new Command("wq", 0, 0);
                        else {
                            this.expr = "";
                            return;
                        }
                        try {
                            ctrl.command.interpret(ctrl.sheet);
                        } catch (Exception e) {
                            ctrl.infoBar.setInfobarTxt(e.getMessage());
                            evaluationFinished = true;
                        }
                    }
                }
                case '.' -> {
                    ctrl.infoBar.setIBarExpr(prevExpr);
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

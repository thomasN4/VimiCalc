package vimicalc.model;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import vimicalc.controller.Mode;
import vimicalc.view.Formatting;

import static vimicalc.controller.Controller.*;

public class Command extends Interpretable {
    public Command(String txt, int xC, int yC) {
        super(txt, xC, yC);
    }
    public boolean commandExists = true;
    public void readFile(Sheet sheet, Lexeme[] command) throws Exception {
        sheet.readFile(command[1].getFunc());
    }

    public void writeFile(Sheet sheet, Lexeme[] command) throws Exception {
        if (command.length == 1) sheet.writeFile();
        else sheet.writeFile(command[1].getFunc());
    }

    public Lexeme[] interpret(Lexeme[] command, Sheet sheet) throws Exception {
        commandExists = true;
        switch (command[0].getFunc()) {
            case "h", "help", "?" -> {
                infoBar.setInfobarTxt(helpMenu.percentage());
                infoBar.draw(gc);
                currMode = Mode.HELP;
            }
            case "e" -> readFile(sheet, command);
            case "resCol" -> sheet.getPositions().generate(
                new int[]{xC, (int) command[1].getVal()},
                true
            );
            case "resRow" -> sheet.getPositions().generate(
                new int[]{yC, (int) command[1].getVal()},
                false
            );
            case "purgeDeps" -> sheet.purgeDependencies();
            case "w" -> writeFile(sheet, command);
            case "wq" -> {
                try {
                    writeFile(sheet, command);
                } catch (Exception ignored) {}
                Platform.exit();
            }
            case "q" -> Platform.exit();
            case "cellColor" -> {
                if (command.length == 1) cellColor("", sheet);
                else cellColor(command[1].getFunc(), sheet);
            }
            case "txtColor" -> {
                if (command.length == 1) txtColor("", sheet);
                else txtColor(command[1].getFunc(), sheet);
            }
            default -> {
                commandExists = false;
                throw new Exception("Command \"" + command[0].getFunc() + "\" doesn't exist.");
            }
        }
        return new Lexeme[]{new Lexeme(0)};
    }

    private void cellColor(@NotNull String color, Sheet sheet) {
        Color cC = switch (color) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "vLGray" -> Color.DIMGRAY;
            case "lGray" -> Color.LIGHTGRAY;
            case "gray" -> Color.GRAY;
            case "dGray" -> Color.DARKGRAY;
            case "black" -> Color.BLACK;
            default -> DEFAULT_CELL_C;
        };

        Formatting f;
        System.out.println("Executing command 'cellColor'...");
        f = sheet.getFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addCellFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setCellColor(cC);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.getFormatting(xC, yC));
    }

    private void txtColor(String color, Sheet sheet) {
        Color tC = switch (color) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "white" -> Color.WHITE;
            case "vLGray" -> Color.DIMGRAY;
            case "lGray" -> Color.LIGHTGRAY;
            case "gray" -> Color.GRAY;
            case "dGray" -> Color.DARKGRAY;
            default -> DEFAULT_TXT_C;
        };

        Formatting f;
        System.out.println("Executing command 'txtColor'...");
        f = sheet.getFormatting(xC, yC);
        if (f == null) {
            System.out.println("Generating new Formatting...");
            f = new Formatting();
            sheet.addCellFormatting(xC, yC, f);
        }
        System.out.println("New formatting: " + f);
        f.setTxtColor(tC);

        if (f.isDefault()) sheet.deleteFormatting(xC, yC);
        System.out.println("Cell formats: " + sheet.getFormatting(xC, yC));
    }
}

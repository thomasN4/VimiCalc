package vimicalc.model;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import vimicalc.controller.Mode;
import vimicalc.view.Formatting;

import java.util.List;

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
            case "resCol" -> sheet.getPicMetadata().generate(
                new int[]{xC, (int) command[1].getVal()},
                true
            );
            case "resRow" -> sheet.getPicMetadata().generate(
                new int[]{yC, (int) command[1].getVal()},
                false
            );
            case "purgeDeps" -> sheet.purgeDependencies();
            case "w" -> writeFile(sheet, command);
            case "wq" -> {
                writeFile(sheet, command);
                Platform.exit();
            }
            case "q" -> Platform.exit();
            case "cellColor" -> cellColor(command[1].getFunc(), sheet);
            default -> {
                commandExists = false;
                System.out.println("Command \"" + command[0] + "\" doesn't exist.");
            }
        }
        return new Lexeme[]{new Lexeme(0)};
    }

    private void cellColor(String color, Sheet sheet) {
        Color cC;
        switch (color) {
            case "red" -> cC = Color.RED;
            case "green" -> cC = Color.GREEN;
            case "blue" -> cC = Color.BLUE;
            case "lGray" -> cC = Color.LIGHTGRAY;
            case "gray" -> cC = Color.GRAY;
            case "dGray" -> cC = Color.DARKGRAY;
            case "black" -> cC = Color.BLACK;
            default -> cC = DEFAULT_CELL_C;
        }

        Formatting f;
        try {
            f = sheet.getCellsFormatting().get(List.of(xC, yC));
            f.setCellColor(cC);
        } catch (Exception ignored) {
            f = new Formatting(cC, DEFAULT_TXT_C, DEFAULT_VPOS, DEFAULT_ALIGNMENT, DEFAULT_FONT, xC, yC);
        }

        sheet.addCellFormatting(xC, yC, f);
        System.out.println("Cell formats: " + sheet.getCellsFormatting());
    }

    public boolean commandExists() {
        return commandExists;
    }
}

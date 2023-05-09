package vimicalc.model;

import javafx.application.Platform;
import vimicalc.controller.Mode;

import static vimicalc.controller.Controller.*;

public class Command extends Interpretable {
    public Command(String txt, int xC, int yC) {
        super(txt, xC, yC);
    }
    public boolean commandExists = true;
    public void readFile(Sheet sheet, Lexeme[] command) {
        try {
            sheet.readFile(command[1].getFunc());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeFile(Sheet sheet, Lexeme[] command) {
        try {
            if (command.length == 1) sheet.writeFile();
            else sheet.writeFile(command[1].getFunc());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Lexeme[] interpret(Lexeme[] command, Sheet sheet) {
        commandExists = true;
        switch (command[0].getFunc()) {
            case "h", "help", "?" -> {
                helpMenu.drawText();
                currMode = Mode.HELP;
                infoBar.setInfobarTxt(
                    (int)((float)((helpMenu.getPosition()+1.0)/helpMenu.getText().length) * 100.0) + "%"
                );
                infoBar.draw(gc);
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
            default -> {
                commandExists = false;
                System.out.println("Command \"" + command[0] + "\" doesn't exist.");
            }
        }
        return new Lexeme[]{new Lexeme(0)};
    }

    public boolean commandExists() {
        return commandExists;
    }
}

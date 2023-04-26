package vimicalc.model;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import vimicalc.view.Camera;
import vimicalc.view.CellSelector;

public class Command extends Interpretable {
    private static CellSelector cellSelector;
    private static Camera camera; private static GraphicsContext gc;
    private static Color c;

    public Command(String txt, int xC, int yC) {
        super(txt, xC, yC);
    }
    private static boolean commandExists = true;
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
            case "e" -> readFile(sheet, command);
            case "resCol" -> sheet.getPicMetadata().generate(
                new int[]{xC, (int) command[1].getVal()},
                true
            );
            case "resRow" -> sheet.getPicMetadata().generate(
                new int[]{yC, (int) command[1].getVal()},
                false
            );
            case "w" -> writeFile(sheet, command);
            case "wq" -> {
                writeFile(sheet, command);
                Platform.exit();
            }
            case "q" -> Platform.exit();
            case "c" -> cellSelector = new CellSelector(
                cellSelector.getSelectedCell().xCoord(), cellSelector.getSelectedCell().yCoord(),
                cellSelector.getW(), cellSelector.getH(), Color.RED, camera.picture.metadata()
            );
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

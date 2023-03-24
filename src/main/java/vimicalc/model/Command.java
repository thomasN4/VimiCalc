package vimicalc.model;

import javafx.application.Platform;
public class Command extends Interpretable {
    public Command(String txt) {
        super(txt);
    }
    public static boolean commandExists = true;
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
}

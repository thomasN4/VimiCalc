package vimicalc.model;

import javafx.application.Platform;

import java.util.ArrayList;

public class Command extends Interpretable {
    public Command(String txt) {
        super(txt);
    }

    public void readFile(Sheet sheet, ArrayList<String> command) {
        try {
            sheet.readFile(command.get(1));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeFile(Sheet sheet, ArrayList<String> command) {
        try {
            if (command.size() == 1) sheet.writeFile();
            else sheet.writeFile(command.get(1));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String interpret(String raw, Sheet sheet) {
        ArrayList<String> lexed = lexer(raw);
        switch (lexed.get(0)) {
            case "e" -> readFile(sheet, lexed);
            case "w" -> writeFile(sheet, lexed);
            case "q" -> Platform.exit();
            default -> System.out.println("Command \"" + lexed.get(0) + "\" doesn't exist.");
        }
        return null;
    }
}

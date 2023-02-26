package vimicalc.model;

import javafx.application.Platform;

public class Command {

    private String txt;

    public Command (String txt) {
        this.txt = txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public void interpret() {
        if (txt.length() == 1) {
            switch (txt.charAt(0)) {
                case 'w' -> System.out.println("Command 'w' is not yet implemented.");
                case 'q' -> Platform.exit();
            }
        }
    }
}

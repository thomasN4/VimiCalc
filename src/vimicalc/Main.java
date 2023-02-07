package vimicalc;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
    }

    public static void save() throws IOException {
        String locationAndName = "";
        String fileName = locationAndName + ".vicalc";
        try {
            FileWriter f = new FileWriter(fileName);
        } catch (Exception ignored) {}
    }

}
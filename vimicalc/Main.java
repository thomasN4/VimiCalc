package vimicalc;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Table newTable = new Table();
    }

    public static void save() throws IOException {
        String locationAndName = "";
        String fileName = locationAndName + ".vicalc";
        FileWriter f = new FileWriter(fileName);
    }

}
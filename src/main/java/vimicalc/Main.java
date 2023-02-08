package vimicalc;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

import static javafx.application.Application.launch;

public class Main {

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("VimiCalc");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void save() throws IOException {
        String locationAndName = "";
        String fileName = locationAndName + ".vmclc";
        try {
            FileWriter f = new FileWriter(fileName);
        } catch (Exception ignored) {}
    }

}
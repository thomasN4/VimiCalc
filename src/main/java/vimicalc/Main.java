package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        primaryStage.setTitle("VimiCalc");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

//    public static void save() throws IOException {
//        String locationAndName = "";
//        String fileName = locationAndName + ".vmclc";
//        try {
//            FileWriter f = new FileWriter(fileName);
//        } catch (Exception ignored) {}
//    }

}
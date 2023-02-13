package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vimicalc.controller.Controller;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        scene.setOnKeyPressed(Controller::onKeyPressed);
        primaryStage.setTitle("VimiCalc");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // basically le même algorithme qu'on a appris en maths discrètes pour la conversion de bases
    public static String toAlpha(int num) {
        int divRes = num / 26;
        int rem = num % 26;
        if (num > 26) return (char)(rem+65) + toAlpha(divRes);
        else return ""+(char)(num+65);
    }
}
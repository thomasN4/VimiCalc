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
        System.out.println();
    }

//public class Main  {
//    public static void main(String[] args) {
//        System.out.println(toAlpha(26));
//    }

    public static String toAlpha(int num) {
        if (num > 25) {
            return (char)(num%26+65) + toAlpha(num/26);
        }
        else return ""+(char)(num/26+65);
    }
}
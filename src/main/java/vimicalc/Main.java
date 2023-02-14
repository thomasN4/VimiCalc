package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vimicalc.controller.Controller;

import java.io.IOException;

import static java.lang.Math.pow;

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

    public static String toAlpha(int num) {
        int rem = num % 26;
        if (num > 25) return toAlpha((num-rem-1)/26) + (char)(rem+65);
        else return ""+(char)(num+65);
    }

    public static int fromAlpha(String alpha) {
        int num = 0;
        for (int i = 0; i <= alpha.length(); i++) {
            num += (alpha.charAt(i)-64)*pow(26, alpha.length()-i-1);
        }
        return num;
    }
}
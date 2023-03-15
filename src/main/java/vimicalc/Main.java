package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;
import vimicalc.controller.Controller;

import static java.lang.Math.pow;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));

        //CD: Scrollbar Horizontal & ses parameters (to-do fix avec camera)
        ScrollBar bar1 = new ScrollBar();
        bar1.setLayoutX(0);
        bar1.setLayoutY(478);
        bar1.setMin(0);
        bar1.setMax(100);
        bar1.setValue(1);
        bar1.setOrientation(Orientation.HORIZONTAL);
        bar1.setPrefSize(640,10);

        //CD: Scrollbar Vertical & ses parameters (to-do fix avec camera)
        ScrollBar bar2 = new ScrollBar();
        bar2.setLayoutX(640);
        bar2.setLayoutY(0);
        bar2.setMin(0);
        bar2.setMax(100);
        bar2.setValue(1);
        bar2.setOrientation(Orientation.VERTICAL);
        bar2.setPrefSize(10,478);

        Group root = new Group((Node) fxmlLoader.load(), bar1, bar2); //CD: regroupe le fxml et les scrollbars

        Scene scene = new Scene(root, 640, 480); //CD: Modif (root)
        scene.setOnKeyPressed(Controller::onKeyPressed);
        //scene.setOnMouseClicked(Controller::onMouseClicked); //CD: pour les cliques avec souris
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
        for (int i = 0; i < alpha.length(); i++) {
            num += (alpha.charAt(i)-64)*pow(26, alpha.length()-i-1);
        }
        return num;
    }

   public static boolean isNumber(String s) {
        boolean b = true;
        try {
            Double.parseDouble(s);
        } catch (Exception ignored) {
            b = false;
        }
        return b;
    }
}
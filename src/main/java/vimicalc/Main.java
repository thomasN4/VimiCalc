package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;
import vimicalc.controller.Controller;

public class Main extends Application {

    public static String arg1 = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));

        //CD: Scrollbar Horizontal & ses parameters (to-do fix avec camera)
        ScrollBar bar1 = new ScrollBar();
        bar1.setLayoutX(0);
        bar1.setLayoutY(600);
        bar1.setMin(0);
        bar1.setMax(100);
        bar1.setValue(1);
        bar1.setOrientation(Orientation.HORIZONTAL);
        bar1.setPrefSize(640,10);

        //CD: Scrollbar Vertical & ses parameters (to-do fix avec camera)
        ScrollBar bar2 = new ScrollBar();
        bar2.setLayoutX(800);
        bar2.setLayoutY(0);
        bar2.setMin(0);
        bar2.setMax(100);
        bar2.setValue(1);
        bar2.setOrientation(Orientation.VERTICAL);
        bar2.setPrefSize(10,600);

        Group root = new Group(fxmlLoader.load(), bar1, bar2); //CD: regroupe le fxml et les scrollbars

        Scene scene = new Scene(root, 800, 600); //CD: Modif (root)
        scene.setOnKeyPressed(Controller::onKeyPressed);
        //scene.setOnMouseClicked(Controller::onMouseClicked); //CD: pour les cliques avec souris
        primaryStage.setTitle("VimiCalc");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length > 1) arg1 = args[1];
        launch(args);
        System.out.println();
    }

}
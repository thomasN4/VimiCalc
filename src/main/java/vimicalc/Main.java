package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    static final double DEFAULT_CELL_HEIGHT = 23;
    static final double DEFAULT_CELL_WIDTH = DEFAULT_CELL_HEIGHT*4;
    static final Color DEFAULT_CELL_COLOR = Color.WHITE;

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

}
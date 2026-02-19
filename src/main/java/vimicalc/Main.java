package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vimicalc.controller.Controller;

import java.util.Arrays;

/**
 * Application entry point for VimiCalc (WeSpreadSheet).
 *
 * <p>Sets up the JavaFX stage, loads the FXML layout,
 * and wires keyboard input to the {@link Controller}. An optional file path
 * can be passed as a command-line argument to open a {@code .wss} file on launch.</p>
 */
public class Main extends Application {

    /** The first command-line argument (file path), or {@code null} if none was provided. */
    public static String arg1 = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));

        Group root = new Group((Parent) fxmlLoader.load());

        Scene scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(Controller::onKeyPressed);
        primaryStage.setTitle("WeSpreadSheet");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("args: " + Arrays.toString(args));
        if (args.length > 0) arg1 = args[0];
        launch(args);
        System.out.println();
    }

}

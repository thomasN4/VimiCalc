package vimicalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vimicalc.controller.Controller;
import vimicalc.model.Settings;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Application entry point for VimiCalc (WeSpreadSheet).
 *
 * <p>Sets up the JavaFX stage, loads the FXML layout,
 * and wires keyboard input to the {@link Controller}. An optional file path
 * can be passed as a command-line argument to open a {@code .json} file on launch.</p>
 */
public class Main extends Application {

    /** The first command-line argument (file path), or {@code null} if none was provided. */
    public static String arg1 = null;

    /**
     * Startup configuration parsed from the user's {@code vimicalcrc}, or all
     * defaults when no config file exists. Set once in {@link #main} before
     * launch and read by {@code Controller.initialize}; like {@link #arg1},
     * static because the {@code Controller} is instantiated by the FXML loader
     * (UI tests that load the FXML directly bypass {@code main} and see defaults).
     */
    public static Settings settings = Settings.defaults();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI.fxml"));

        Parent root = fxmlLoader.load();
        Controller ctrl = fxmlLoader.getController();

        Scene scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(ctrl::onKeyPressed);
        primaryStage.setTitle("WeSpreadSheet");
        primaryStage.setMinWidth(320);
        primaryStage.setMinHeight(240);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments; the first is an optional file path
     */
    public static void main(String[] args) {
        System.out.println("args: " + Arrays.toString(args));
        if (args.length > 0) arg1 = args[0];
        settings = Settings.loadFrom(Settings.candidatePaths(
            System.getenv("XDG_CONFIG_HOME"),
            Path.of(System.getProperty("user.home"))));
        settings.getWarnings().forEach(w -> System.err.println("vimicalcrc: " + w));
        launch(args);
        System.out.println();
    }

}

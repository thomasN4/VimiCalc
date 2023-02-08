package vimicalc;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

abstract class Cell {

    protected int x_pos;
    protected int y_pos;
    protected Rectangle shape;
    protected Text content;
    protected StackPane stackPane;

    Cell () {
        shape = new Rectangle(Main.DEFAULT_CELL_WIDTH, Main.DEFAULT_CELL_HEIGHT);
        shape.setFill(Main.DEFAULT_CELL_COLOR);
        stackPane = new StackPane();
        stackPane.getChildren().addAll(shape, content);
        content.toFront();
    }

}

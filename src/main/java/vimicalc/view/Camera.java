package vimicalc.view;

public class Camera {
    private int table_xPos;
    private int table_yPos;
    private Picture picture;

    public Camera(int x, int y, double w, double h, int table_xPos, int table_yPos) {
        picture = new Picture(x, y, w, h);
        this.table_xPos = table_xPos;
        this.table_yPos = table_yPos;
    }

    public Picture getPicture() {
        return picture;
    }
}

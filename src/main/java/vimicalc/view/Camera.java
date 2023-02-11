package vimicalc.view;

public class Camera {
    protected int x_pos;
    protected int y_pos;
    protected double w;
    protected double h;
    private Picture picture;

    public Camera(int x_pos, int y_pos, double w, double h) {
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.w = w;
        this.h = h;
        picture = new Picture(x_pos, y_pos, w, h);
    }

}

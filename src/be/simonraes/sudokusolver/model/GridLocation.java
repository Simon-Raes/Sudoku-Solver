package be.simonraes.sudokusolver.model;

/**
 * Created by Simon Raes on 27/07/2014.
 */
public class GridLocation {
    private int x;
    private int y;

    public GridLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

}

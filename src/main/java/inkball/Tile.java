package inkball;
import processing.core.PImage;

public class Tile {
    protected int x, y; // Grid coordinates
    protected PImage Image;
    protected App app;

    public static final int TILE_SIZE = 32;

    public Tile(int x, int y, App app) {
        this.x = x;
        this.y = y;
        this.app = app;
    }

    // Getter for x coordinate
    public int getX() {
        return x;
    }

    // Getter for y coordinate
    public int getY() {
        return y;
    }

    // Method to determine if the tile is solid (collidable)
    public boolean canBeHit() {
        return false; // By default, tiles are not solid
    }

    // The draw method accepts a PApplet instance
    public void draw(App app) {
        if (Image != null) {
            app.image(Image, x * TILE_SIZE, y * TILE_SIZE + App.TOPBAR);
        }
    }
}

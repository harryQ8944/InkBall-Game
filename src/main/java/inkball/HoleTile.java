package inkball;

import processing.core.PApplet;

public class HoleTile extends Tile {
    private int colour;
    private boolean isDefult;
    
    public HoleTile(int x, int y, int colour, App app, boolean isDefult) {
        super(x, y, app);
        this.colour = colour;
        this.isDefult = isDefult;

        if (isDefult) {
            this.Image = app.loadImage("src/main/resources/inkball/hole" + colour + ".png");
        } else {
            this.Image = null;
        }
    }

    public void draw(PApplet app) {
        if (isDefult && Image != null) {
            app.image(Image, getX() * App.TILE_SIZE, getY() * App.TILE_SIZE + App.TOPBAR);
        }
    }

    public int getColour() {
        return colour;
    }

    public boolean isDefult() {
        return isDefult;
    }
}

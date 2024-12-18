package inkball;

public class EntryPoint extends Tile {

    public EntryPoint(int x, int y, App app) {
        super(x, y, app);
        this.Image = app.loadImage("src/main/resources/inkball/entrypoint.png");
    }

    @Override
    public boolean canBeHit() {
        return false;
    }
}
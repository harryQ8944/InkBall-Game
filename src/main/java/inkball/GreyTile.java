package inkball;

public class GreyTile extends Tile {
    protected int damageLevel = 0;
    protected App app;
    protected static final int MAX_DAMAGE_LEVEL = 3; // Maximum damage level before the wall is destroyed
    protected boolean isDestroyed = false; // Track if the wall is destroyed
    private boolean canBeHit = true;
    private static final int HIT_COOLDOWN_FRAMES = 5; // Number of frames to wait before the wall can be hit again
    private int cooldownCounter = 0;


    public GreyTile(int x, int y, App app) {
        super(x, y, app);
        this.app = app;
        this.Image = app.loadImage("src/main/resources/inkball/wall0.png"); // Initial wall image
    }

    @Override
    public boolean canBeHit() {
        return !isDestroyed; // Wall is solid unless it is destroyed
    }

    public void updateCooldown() {
        if (!canBeHit) {
            cooldownCounter--;
            if (cooldownCounter <= 0) {
                canBeHit = true;
            }
        }
    }

    public void getsHit(Ball ball) {
        if (!canBeHit || isDestroyed) {
            return; // Skip if the wall is still in cooldown or destroyed
        }

        damageLevel++;
        System.out.println("WallTile at (" + x + ", " + y + ") hit. Damage Level: " + damageLevel);

        if (damageLevel < MAX_DAMAGE_LEVEL) {
            // Update the wall's sprite based on the damage level
            updateImage(app);
        } else {
            // Remove the wall after reaching the maximum damage level
            removeDamagedWall();
        }
        canBeHit = false; // Set the wall as temporarily invulnerable
        cooldownCounter = HIT_COOLDOWN_FRAMES; // Reset the cooldown counter
    }

    private void updateImage(App app) {
        switch (damageLevel) {
            case 1:
                break;
            case 2:
                // Reload the same image and apply transparency
                this.Image = app.loadImage("src/main/resources/inkball/walld0.png");
                break;
            default:
                break;
        }
        
        // Redraw the tile with the updated sprite
        app.getTile(x, y).draw(app);
        // Reset tint to default after applying it to avoid affecting other drawings
        app.noTint();
    }
    
    public void removeDamagedWall() {
        isDestroyed = true; // Mark the wall as destroyed
        this.Image = null; // Remove the sprite to indicate the wall is gone
        app.getGrid()[y][x] = null; // Remove the tile from the game board
        System.out.println("Wall at (" + x + ", " + y + ") destroyed.");
    }

    // Getter for the damage level
    public int getDamageLevel() {
        return damageLevel;
    }
}

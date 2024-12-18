package inkball;

public class ColourTile extends GreyTile {
    private int colour;
    private boolean canBeHit = true;
    private static final int HIT_COOLDOWN_FRAMES = 5; // Cooldown frames before the wall can be hit again
    private int cooldownCounter = 0;

    public ColourTile(int x, int y, int colour, App app) {
        super(x, y, app);
        this.colour = colour;
        this.Image = app.loadImage("src/main/resources/inkball/wall" + colour + ".png");
        System.out.println("Created ColouredWallTile at (" + x + ", " + y + ") with colour index: " + colour);
    }

    public void updateCooldown() {
        if (!canBeHit) {
            cooldownCounter--;
            if (cooldownCounter <= 0) {
                canBeHit = true;
            }
        }
    }
    
    @Override
    public void getsHit(Ball ball) {
        // Check if the wall can be hit and if the ball color matches or is grey
        if (!canBeHit || isDestroyed || (colour != 0 && ball.getColour() != colour)) {
            if (colour != 0 && ball.getColour() != colour) {
                System.out.println("ColouredWallTile get hit at (" + getX() + ", " + getY() + ") hit, Different colour!");
            }
            return; // No action if it cannot be hit or color does not match
        }


        // Check if the ball's color matches the wall's color
        if (ball.getColour() != this.colour && this.colour != 0) {
            System.out.println("ColouredWallTile get hit at (" + getX() + ", " + getY() + ") , Different Colour!");
            return; // Exit without applying any damage if the color doesn't match
        }

        // If the colors match or if it's a grey wall (colour index 0), proceed with damage
        damageLevel++;
        System.out.println("ColouredWallTile get hit at (" + getX() + ", " + getY() + "), Same colour Apply damage! Damage Level: " + damageLevel);

        if (damageLevel < MAX_DAMAGE_LEVEL) {
            // Update the wall's sprite based on the damage level
            updateImages(app);
        } else {
            // Remove the wall after reaching the maximum damage level
            removeDamagedWall();
        }
        canBeHit = false; // Set the wall as temporarily invulnerable
        cooldownCounter = HIT_COOLDOWN_FRAMES; // Reset the cooldown counter
    }

    protected void updateImages(App app) {
        // Change the sprite based on the damage level and color
        switch (damageLevel) {
            case 1:
                break;
            case 2:
                this.Image = app.loadImage("src/main/resources/inkball/walld" + colour + ".png");
                break;
            default:
                break;
        }

        // Redraw the tile in the next draw call
        app.getTile(x, y).draw(app);
    }
    
    public int getColour() {
        return colour;
    }

    // Getters and setters for testing purposes
    public boolean isCanBeHit() {
        return canBeHit;
    }

    public void setCanBeHit(boolean canBeHit) {
        this.canBeHit = canBeHit;
    }

    public int getCooldownCounter() {
        return cooldownCounter;
    }

    public void setCooldownCounter(int cooldownCounter) {
        this.cooldownCounter = cooldownCounter;
    }
}


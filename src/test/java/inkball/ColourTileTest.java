package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;

import static org.junit.jupiter.api.Assertions.*;

public class ColourTileTest {

    private App app;
    private ColourTile colourTile;
    private Ball testBall;

    @BeforeEach
    public void setUp() {
        // Initialize the app and load necessary settings
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();
        app.noLoop(); // Ensure the game loop is not running during tests

        // Initialize a ColourTile and a Ball for testing
        colourTile = new ColourTile(5, 5, 1, app); // Create a ColourTile with a non-grey color
        testBall = new Ball(160, 160 + App.TOPBAR, 1, app); // Position ball at the same grid coordinates
    }

    @Test
    public void testGetsHitSameColor() {
        // Test when the ball color matches the tile color
        colourTile.getsHit(testBall);
        assertEquals(1, colourTile.damageLevel, "Damage level should increase when ball color matches the tile color.");
        assertFalse(colourTile.isCanBeHit(), "Tile should be invulnerable after getting hit.");
    }

    @Test
    public void testGetsHitDifferentColor() {
        // Test when the ball color does not match the tile color
        Ball differentColorBall = new Ball(160, 160 + App.TOPBAR, 2, app); // Ball with a different color
        colourTile.getsHit(differentColorBall);
        assertEquals(0, colourTile.damageLevel, "Damage level should not increase when ball color does not match the tile color.");
        assertTrue(colourTile.isCanBeHit(), "Tile should remain hittable when ball color does not match.");
    }

    @Test
    public void testGetsHitGreyTile() {
        // Test when the tile is grey (color index 0) and any ball hits it
        ColourTile greyTile = new ColourTile(5, 5, 0, app);
        greyTile.getsHit(testBall);
        assertEquals(1, greyTile.damageLevel, "Damage level should increase when ball hits a grey tile.");
        assertFalse(greyTile.isCanBeHit(), "Grey tile should be invulnerable after getting hit.");
    }

    @Test
    public void testUpdateImages() {
        // Case 1: Test with damage level 1 (no image update expected)
        colourTile.damageLevel = 1;
        PImage initialImage = colourTile.Image; // Store initial image reference
        colourTile.updateImages(app);
        assertEquals(initialImage, colourTile.Image, "Image should remain unchanged when damage level is 1.");

        // Case 2: Test with damage level 2 (image should update)
        colourTile.damageLevel = 2;
        colourTile.updateImages(app);
        assertNotNull(colourTile.Image, "Image should be updated when damage level is 2.");
        assertTrue(colourTile.Image != initialImage, "Image should change when damage level is 2.");

        // Case 3: Test with damage level beyond expected values (no update expected)
        colourTile.damageLevel = 3; // Any value beyond handled cases
        PImage previousImage = colourTile.Image; // Store the image after the last valid update
        colourTile.updateImages(app);
        assertEquals(previousImage, colourTile.Image, "Image should remain unchanged when damage level is beyond handled cases.");
    }


    @Test
    public void testUpdateCooldown() {
        // Test the cooldown mechanism
        colourTile.setCanBeHit(false); // Simulate that the tile was hit
        colourTile.setCooldownCounter(1); // Set cooldown counter to a positive value
        colourTile.updateCooldown();
        assertFalse(colourTile.isCanBeHit(), "Tile should remain invulnerable when cooldown is still active.");
        
        // Update cooldown again to reach zero
        colourTile.updateCooldown();
        assertTrue(colourTile.isCanBeHit(), "Tile should be hittable again when cooldown reaches zero.");
    }

    @Test
    public void testGetColour() {
        // Test the getColour method
        assertEquals(1, colourTile.getColour(), "Tile color should match the initialized value.");
    }

    @Test
    public void testUpdateCooldownCounter() {
        // Test that the cooldown counter decreases properly when the tile is hit
        colourTile.setCanBeHit(false); // Set the tile as temporarily invulnerable
        colourTile.setCooldownCounter(3); // Set a cooldown value of 3

        // Call updateCooldown and check if the cooldown counter decreases
        colourTile.updateCooldown();
        assertEquals(2, colourTile.getCooldownCounter(), "Cooldown counter should decrease by 1.");
        assertFalse(colourTile.isCanBeHit(), "Tile should still be invulnerable when cooldown counter is greater than 0.");

        // Call updateCooldown until the counter reaches zero
        colourTile.updateCooldown();
        colourTile.updateCooldown();

        // Verify that the tile can be hit again once the cooldown counter is zero
        assertTrue(colourTile.isCanBeHit(), "Tile should be hittable again once the cooldown counter reaches zero.");
        assertEquals(0, colourTile.getCooldownCounter(), "Cooldown counter should be zero when tile becomes hittable.");
    }
}

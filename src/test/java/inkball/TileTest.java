package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;

public class TileTest {
    private Tile tile;
    private App app;

    @BeforeEach
    public void setUp() {
        // Initialize the App instance properly
        app = new App();
        PApplet.runSketch(new String[] {"App"}, app);
        app.setup();

        // Initialize the Tile instance at grid position (2, 3)
        tile = new Tile(2, 3, app);
    }

    @Test
    public void testGetX() {
        // Test that the x coordinate is correctly returned
        assertEquals(2, tile.getX(), "X coordinate should be 2.");
    }

    @Test
    public void testGetY() {
        // Test that the y coordinate is correctly returned
        assertEquals(3, tile.getY(), "Y coordinate should be 3.");
    }

    @Test
    public void testCanBeHit() {
        // Test that the default tile cannot be hit
        assertFalse(tile.canBeHit(), "Default Tile should not be hit.");
    }

    @Test
    public void testDraw() {
        // Set up a mock image for testing
        PImage mockImage = app.createImage(Tile.TILE_SIZE, Tile.TILE_SIZE, PApplet.ARGB);
        tile.Image = mockImage;

        // Call the draw method to ensure no exceptions are thrown
        assertDoesNotThrow(() -> tile.draw(app), "Drawing the tile should not throw an exception.");

        // If the image is null, the draw method should not throw an exception either
        tile.Image = null;
        assertDoesNotThrow(() -> tile.draw(app), "Drawing a tile with no image should not throw an exception.");
    }
}

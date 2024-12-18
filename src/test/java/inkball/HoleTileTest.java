package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;

public class HoleTileTest {
    private HoleTile holeTile;
    private App app;
    private int x, y, colour;
    private boolean isDefult;

    @BeforeEach
    public void setUp() {
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        x = 5;
        y = 5;
        colour = 2; // Example color
        isDefult = true; // Setting it as default

        // Initialize the HoleTile instance
        holeTile = new HoleTile(x, y, colour, app, isDefult);
    }

    @Test
    public void testGetColour() {
        // Test that the color is correctly returned
        assertEquals(colour, holeTile.getColour(), "The color of the HoleTile should match the initialized value.");
    }

    @Test
    public void testIsDefult() {
        // Test that the isDefult flag is correctly returned
        assertTrue(holeTile.isDefult(), "The HoleTile should be marked as default.");
    }

    @Test
    public void testDrawWithDefault() {
        // Test that the draw method works when isDefult is true and an image is loaded
        PImage imageBeforeDraw = holeTile.Image;
        holeTile.draw(app);
        // Validate that the image used is the same as the one initially loaded
        assertEquals(imageBeforeDraw, holeTile.Image, "The image used for the HoleTile should remain the same.");
    }

    @Test
    public void testDrawWithoutDefault() {
        // Test drawing behavior when isDefult is false
        HoleTile nonDefaultHoleTile = new HoleTile(x, y, colour, app, false);
        nonDefaultHoleTile.draw(app);
        assertNull(nonDefaultHoleTile.Image, "Non-default HoleTile should not have an image assigned.");
    }
}

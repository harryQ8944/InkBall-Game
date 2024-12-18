package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PVector;

public class HoleTest {
    private Hole hole;
    private PVector position;
    private PVector dimensions;
    private int colour;

    @BeforeEach
    public void setUp() {
        // Set up a position and dimensions for the hole
        position = new PVector(50, 50);
        dimensions = new PVector(32, 32);
        colour = 1; // Example colour

        // Initialize the Hole instance
        hole = new Hole(position, dimensions, colour);
    }

    @Test
    public void testGetCenter() {
        // Calculate the expected center
        PVector expectedCenter = new PVector(position.x + dimensions.x / 2, position.y + dimensions.y / 2);
        
        // Test that the center of the hole is correctly calculated
        assertEquals(expectedCenter.x, hole.getCenter().x, "The x-coordinate of the center should match.");
        assertEquals(expectedCenter.y, hole.getCenter().y, "The y-coordinate of the center should match.");
    }

    @Test
    public void testGetColour() {
        // Test that the color is correctly returned
        assertEquals(colour, hole.getColour(), "The color of the hole should match the initialized value.");
    }

    @Test
    public void testGetPosition() {
        // Test that the position is correctly returned
        assertEquals(position, hole.getPosition(), "The position of the hole should match the initialized value.");
    }

    @Test
    public void testGetDimensions() {
        // Test that the dimensions are correctly returned
        assertEquals(dimensions, hole.getDimensions(), "The dimensions of the hole should match the initialized value.");
    }
}

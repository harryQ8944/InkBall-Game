package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

public class EntryPointTest {
    private EntryPoint entryPoint;
    private App app;

    @BeforeEach
    public void setUp() {
        // Initialize the App instance properly
        app = new App();
        PApplet.runSketch(new String[] {"App"}, app);
        app.setup();
        
        // Initialize the EntryPoint instance
        entryPoint = new EntryPoint(1, 1, app);
    }

    @Test
    public void testCanBeHit() {
        // Check that EntryPoint's canBeHit method returns false as expected
        assertFalse(entryPoint.canBeHit(), "EntryPoint should not be hit.");
    }
}

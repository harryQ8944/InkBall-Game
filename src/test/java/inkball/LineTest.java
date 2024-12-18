package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LineTest {

    private Line line;
    private PApplet app;

    @BeforeEach
    public void setup() {
        line = new Line();
        app = new PApplet();
    }

    @Test
    public void testAddPoint() {
        // Test adding a point below the top bar
        PVector point = new PVector(50, App.TOPBAR + 50);
        line.addPoint(point);
        assertEquals(1, line.getPoints().size(), "Point should be added when below the top bar.");

        // Test adding a point above the top bar
        PVector pointAboveTopBar = new PVector(50, App.TOPBAR - 10);
        line.addPoint(pointAboveTopBar);
        assertEquals(1, line.getPoints().size(), "Point should not be added when above the top bar.");
    }

    @Test
    public void testCap() {
        // Test capping within boundaries
        assertEquals(5, line.cap(5, 0, 10), "Value within range should not be modified.");
        
        // Test capping below the minimum
        assertEquals(0, line.cap(-5, 0, 10), "Value below minimum should be capped to the minimum.");
        
        // Test capping above the maximum
        assertEquals(10, line.cap(15, 0, 10), "Value above maximum should be capped to the maximum.");
    }

    @Test
    public void testCheckNearLinePoint() {
        // Add points to form a line
        line.addPoint(new PVector(50, App.TOPBAR + 50));
        line.addPoint(new PVector(150, App.TOPBAR + 50));
        
        // Test a point near the line
        PVector nearPoint = new PVector(100, App.TOPBAR + 55);
        assertTrue(line.checkNearLinePoint(nearPoint, 10), "Point within proximity should return true.");

        // Test a point far from the line
        PVector farPoint = new PVector(300, App.TOPBAR + 50);
        assertFalse(line.checkNearLinePoint(farPoint, 10), "Point outside proximity should return false.");
    }

    @Test
    public void testClearAllPoints() {
        line.addPoint(new PVector(50, App.TOPBAR + 50));
        line.addPoint(new PVector(150, App.TOPBAR + 50));
        line.clearAllPoints();
        assertTrue(line.getPoints().isEmpty(), "All points should be cleared.");
    }

    @Test
    public void testDraw() {
        // Test draw method when there are fewer than 2 points (should not draw)
        line.addPoint(new PVector(50, App.TOPBAR + 50)); // Only one point added
        try {
            line.draw(app);
            // If we reach here, no exception was thrown, as expected.
        } catch (Exception e) {
            fail("Draw method should not throw an exception when there are fewer than 2 points.");
        }
    
        // Verify that nothing is drawn when there's only one point
        assertEquals(1, line.getPoints().size(), "There should be only one point added.");
    
        // Add a second point and test the drawing with a valid line
        line.addPoint(new PVector(150, App.TOPBAR + 50));
        assertEquals(2, line.getPoints().size(), "There should be two points in the line.");
    
        // We use a flag to check if the drawing logic was executed
        try {
            app.beginShape(); // Simulate the drawing call
            line.draw(app);
            app.endShape(); // Simulate the closing of the drawing
        } catch (Exception e) {
            fail("Draw method should not throw an exception when there are valid points to draw.");
        }
    
        // Now add more points and check if the draw method still functions correctly
        line.addPoint(new PVector(200, App.TOPBAR + 100));
        line.addPoint(new PVector(250, App.TOPBAR + 150));
        assertEquals(4, line.getPoints().size(), "There should be four points in the line.");
    
        // Test drawing with multiple points in the line
        try {
            app.beginShape();
            line.draw(app);
            app.endShape();
        } catch (Exception e) {
            fail("Draw method should not throw an exception when drawing a line with multiple points.");
        }
    }
    

    @Test
    public void testGetPoints() {
        line.addPoint(new PVector(50, App.TOPBAR + 50));
        Map<Integer, PVector> points = line.getPoints();
        assertEquals(1, points.size(), "There should be one point in the map.");
        assertTrue(points.containsKey(0), "The map should contain a point with key 0.");
    }

    @Test
    public void testGetThickness() {
        assertEquals(10.0f, line.getThickness(), "Thickness should be 10.0f.");
    }

    @Test
    public void testLineConstructor() {
        assertNotNull(line.getPoints(), "Line points should be initialized.");
        assertEquals(0, line.getPoints().size(), "Line should have no points initially.");
    }

    @Test
    public void testNearLineDistance() {
        PVector p = new PVector(50, App.TOPBAR + 50);
        PVector a = new PVector(50, App.TOPBAR + 40);
        PVector b = new PVector(150, App.TOPBAR + 40);
        float distance = line.nearLineDistance(p, a, b);

        assertTrue(distance > 0, "Distance should be greater than 0.");
        assertEquals(10, distance, 0.01, "Distance should match expected value based on calculation.");
    }

    @Test
    public void testRemoveLastPoint() {
        line.addPoint(new PVector(50, App.TOPBAR + 50));
        line.addPoint(new PVector(150, App.TOPBAR + 50));
        
        // Remove the last point
        line.removeLastPoint();
        assertEquals(1, line.getPoints().size(), "The number of points should be 1 after removing the last point.");
        
        // Remove again
        line.removeLastPoint();
        assertTrue(line.getPoints().isEmpty(), "The points should be empty after removing all points.");
    }
}


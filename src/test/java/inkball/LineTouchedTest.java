package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PVector;

public class LineTouchedTest {
    private LineTouched lineTouched;
    private PVector startPoint;
    private PVector endPoint;

    @BeforeEach
    public void setUp() {
        // Initialize start and end points for the line segment
        startPoint = new PVector(10, 20);
        endPoint = new PVector(30, 40);
        lineTouched = new LineTouched(startPoint, endPoint);
    }

    @Test
    public void testLineTouchedConstructor() {
        // Test if the LineTouched object is created correctly with given points
        assertNotNull(lineTouched, "LineTouched object should be initialized.");
        assertEquals(startPoint, lineTouched.getP1(), "Start point should match the provided start point.");
        assertEquals(endPoint, lineTouched.getP2(), "End point should match the provided end point.");
    }

    @Test
    public void testGetP1() {
        // Test that getP1() returns the correct starting point
        assertEquals(startPoint, lineTouched.getP1(), "getP1 should return the correct starting point.");
    }

    @Test
    public void testGetP2() {
        // Test that getP2() returns the correct ending point
        assertEquals(endPoint, lineTouched.getP2(), "getP2 should return the correct ending point.");
    }
}


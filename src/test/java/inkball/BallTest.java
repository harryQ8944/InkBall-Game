package inkball;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PVector;
import processing.core.PApplet;

public class BallTest {

    static App app;

    @BeforeAll
    public static void setup() {
        app = new App();
        PApplet.runSketch(new String[] { "inkball.App" }, app);
        app.noLoop(); // Ensure the game loop does not interfere with the tests
    }

    @BeforeEach
    public void beforeEach() {
        // Reload the level and reset the state before each test
        app.loadLevel(0);
    }
    
    

    @Test
    public void testMovingIntoHole() {
        Ball ball = new Ball(0, 0, 0, app);
        ball.movingIntoHole();
        assertTrue(ball.isCaptured(), "Ball should be captured after moving into a hole.");
    }

    @Test
    public void testGetProjection() {
        Ball ball = new Ball(0, 0, 0, app);
        PVector p = new PVector(10, 10);
        PVector a = new PVector(0, 0);
        PVector b = new PVector(20, 20);
        PVector projection = ball.getProjectPoint(p, a, b);
        assertNotNull(projection, "Projection point should not be null.");
    }

    @Test
    public void testDraw() {
        Ball ball = new Ball(0, 0, 0, app);
        assertDoesNotThrow(() -> ball.draw(), "draw() should not throw an exception.");
    }

    @Test
    public void testSetColour() {
        Ball ball = new Ball(0, 0, 0, app);
        ball.changeColour(3);
        assertEquals(3, ball.getColour(), "Ball color should be updated correctly.");
    }

    @Test
    public void testFinishCatching() {
        Ball ball = new Ball(0, 0, 0, app);
        ball.finishCatching();
        assertTrue(ball.isCaptured(), "Ball should be marked as captured after finishing catching.");
    }


    @Test
    public void testUpdate() {
        Ball ball = new Ball(0, 0, 0, app);
        ball.update();
        assertTrue(true, "update() should execute without errors.");
    }

    @Test
    public void testGetColour() {
        Ball ball = new Ball(0, 0, 2, app);
        assertEquals(2, ball.getColour(), "Ball's color should be retrieved correctly.");
    }

    @Test
    public void testCircleIntersectsRectangle() {
        Ball ball = new Ball(0, 0, 0, app);
        PVector center = new PVector(50, 50);
        PVector rectPos = new PVector(0, App.TOPBAR);
        PVector rectSize = new PVector(100, 100);
        assertTrue(ball.circleIntersectsRectangle(center, 10, rectPos, rectSize), "Circle should intersect with the rectangle.");
    }

    @Test
    public void testIsCaptured() {
        Ball ball = new Ball(0, 0, 0, app);
        assertFalse(ball.isCaptured(), "Ball should not be captured initially.");
        ball.finishCatching();
        assertTrue(ball.isCaptured(), "Ball should be captured after finishCatching is called.");
    }

    @Test
    public void testCheckWallCollide() {
        Ball ball = new Ball(0, 0, 0, app);
        Tile tile = new GreyTile(0, 0, app);
        assertTrue(ball.checkWallCollide(tile), "Ball should collide with the wall.");
    }

    @Test
    public void testWallCollideDetections() {
        Ball ball = new Ball(0, 0, 0, app);
        ball.wallCollideDetections();
        assertTrue(true, "wallCollideDetections() should execute without errors.");
    }

    @Test
    public void testBallConstructor() {
        Ball ball = new Ball(100, 200, 1, app);
        assertNotNull(ball, "Ball should be created successfully.");
        assertEquals(100, ball.getPosition().x, "X position should be set correctly.");
        assertEquals(200, ball.getPosition().y, "Y position should be set correctly.");
    }


    @Test
    public void testCap() {
        Ball ball = new Ball(0, 0, 0, app);
        float result = ball.cap(5.5f, 1.0f, 5.0f);
        assertEquals(5.0f, result, "Value should be capped at the max limit.");
    }

    @Test
    public void testGetPosition() {
        Ball ball = new Ball(100, 200, 0, app);
        assertEquals(new PVector(100, 200), ball.getPosition(), "Position should be retrieved correctly.");
    }
}


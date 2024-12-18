package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PConstants; // Import PConstants for LEFT, RIGHT, etc.

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    static App app;

    @BeforeAll
    public static void setupClass() {
        app = new App();
        PApplet.runSketch(new String[] {"App"}, app);
        app.noLoop(); // Prevents the sketch from looping for testing purposes
    }

    @BeforeEach
    public void setup() {
        app.setup(); // Call setup before each test to reset the state
        app.loadLevel(0); // Load the initial level before each test
    }

    @Test
    public void testRestart() {
        app.restart();
        assertEquals(0, app.getScore(), "game should reset to after restart.");
    }

    @Test
    public void testDrawTopBar() {
        assertDoesNotThrow(() -> app.drawTopBar(), "drawTopBar() should not throw an exception.");
    }

    @Test
    public void testMousePressed() {
        app.mouseY = App.TOPBAR + 10;
        app.mouseButton = PConstants.LEFT;
        app.mousePressed();
        assertFalse(app.getLines().isEmpty(), "A line should be added when the left mouse button is pressed below the top bar.");
    }

    @Test
    public void testCreateEdges() {
        List<PVector> edges = app.createEdges();
        assertEquals(2 * (App.GRID_WIDTH + App.GRID_HEIGHT) - 4, edges.size(), "The number of edges should match the perimeter of the grid.");
    }

    @Test
    public void testCheckCompleteOrNot() {
        // Set up the game to near the end of a level
        app.loadLevel(0);  // Ensure the level is loaded
        app.getBalls().clear();  // Simulate that all balls are captured
        app.getBallsToSpawn().clear(); // No more balls to spawn

        // Manually invoke the method that checks for level completion
        app.checkWinOrNot();

        // Assert that the level is marked as completing
        assertTrue(app.isCompleting(), "Level should be marked as completing when all conditions are met.");
    }

    @Test
    public void testCheckWinOrNot() {
        // Clear the balls and ballsToSpawn lists to simulate level completion
        app.getBalls().clear();
        app.getBallsToSpawn().clear();

        // Manually invoke the method that checks for level completion
        app.checkWinOrNot();

        // Assert that the level is marked as completing
        assertTrue(app.isCompleting(), "The level should be marked as completing when conditions are met.");
    }


    @Test
    public void testDrawYellowTiles() {
        assertDoesNotThrow(() -> app.drawYellowTiles(), "drawYellowTiles() should not throw an exception.");
    }

    @Test
    public void testScoreUpdateDetection() {
        // Set timeLeft to a value to allow score update
        app.setTimeLeft(App.FPS);
        
        // Call the method that should update the score
        app.scoreUpdateDetection();
        
        // Assert that the score has increased
        assertTrue(app.getScore() > 0, "Score should increase when scoreUpdateDetection() is called.");
    }


    @Test
    public void testIsLevelFinished() {
        // Set timeLeft to 0 to simulate the end of the level
        app.setTimeLeft(0);
        
        // Call the method that checks if the level is finished
        app.checkIfLevelFinished();
        
        // Assert that the level is marked as finished
        assertTrue(app.hasEnded(), "The level should be marked as finished when timeLeft is zero.");
    }


    @Test
    public void testLineRemoveDistance() {
        Line line = new Line();
        line.addPoint(new PVector(10, App.TOPBAR + 10));
        float distance = app.lineRemoveDistance(new PVector(12, App.TOPBAR + 12), line);
        assertTrue(distance >= 0, "Distance should be a non-negative value.");
    }

    @Test
    public void testRemoveLine() {
        Line line = new Line();
        line.addPoint(new PVector(10, App.TOPBAR + 20));
        app.getLines().add(line);
        app.removeLine(new PVector(12, App.TOPBAR + 22));
        assertTrue(app.getLines().isEmpty(), "Line should be removed when point is close enough.");
    }

    @Test
    public void testMouseDragged() {
        app.mouseY = App.TOPBAR + 10;
        app.mouseButton = PConstants.LEFT;
        app.mousePressed();
        int initialPoints = app.getLines().get(0).getPoints().size();
        app.mouseDragged();
        assertTrue(app.getLines().get(0).getPoints().size() > initialPoints, "New points should be added to the line when mouse is dragged.");
    }

    @Test
    public void testColourToString() {
        assertEquals("grey", app.colourToString(0), "0 should correspond to 'grey'.");
    }

    @Test
    public void testPause() {
        boolean initialState = app.isPaused();
        app.Pause();
        assertNotEquals(initialState, app.isPaused(), "Pause state should toggle.");
    }

    @Test
    public void testFinishDrawing() {
        app.mouseY = App.TOPBAR + 10;
        app.mouseButton = PConstants.LEFT;
        app.mousePressed();
        app.finishDrawing();
        assertFalse(app.getLines().isEmpty(), "A line should be added to the list when finishDrawing() is called.");
    }

    @Test
    public void testUpdateBalls() {
        Ball ball = new Ball(100, 100, 0, app);
        app.getBalls().add(ball);
        app.updateBalls();
        assertEquals(1, app.getBalls().size(), "Ball count should remain consistent after update.");
    }

    @Test
    public void testMoveYellowTiles() {
        int initialIndex1 = app.getYellowTile1();
        app.moveYellowTiles();
        assertNotEquals(initialIndex1, app.getYellowTile1(), "Yellow tile index should change after movement.");
    }

    @Test
    public void testHitAnimation() {
        assertDoesNotThrow(() -> app.HitAnimation(), "HitAnimation() should not throw an exception.");
    }

    @Test
    public void testKeyPressed() {
        app.key = 'r';
        app.keyPressed();
        assertEquals(0, app.getScore(), "Score should reset if 'r' is pressed to restart.");
    }

    @Test
    public void testScoreUpdates() {
        int initialScore = app.getScore();
        app.scoreUpdates();
        assertTrue(app.getScore() >= initialScore, "Score should update correctly.");
    }

    @Test
    public void testBallRefilled() {
        int initialSize = app.getBallsToSpawn().size();
        app.ballRefilled("blue");
        assertEquals(initialSize + 1, app.getBallsToSpawn().size(), "A ball should be added to the spawn queue.");
    }

    @Test
    public void testGetBallColourIndex() {
        assertNotNull(app.getBallColourIndex(0), "Valid index should return a sprite.");
    }

    @Test
    public void testLoadImages() {
        assertDoesNotThrow(() -> app.loadImages(), "loadImages() should not throw an exception.");
    }

    @Test
    public void testSpawnCountdown() {
        app.spawnCountdown();
        assertTrue(true, "spawnCountdown() should execute without errors.");
    }

    @Test
    public void testCheckLoseOrNot() {
        // Set timeLeft to 0 to simulate the condition where time has run out
        app.setTimeLeft(0);

        // Call the method that checks if the level is lost
        app.checkLoseOrNot();

        // Assert that the level is marked as ended
        assertTrue(app.hasEnded(), "The level should end when checkLoseOrNot() is called with timeLeft = 0.");
    }


    @Test
    public void testMouseReleased() {
        MouseEvent event = new MouseEvent(null, 0, 0, 0, 0, 0, 0, 0);
        app.mouseReleased(event);
        assertTrue(true, "mouseReleased() should not throw any exception.");
    }

    @Test
    public void testGetScoreWon() {
        assertEquals(10, app.getScoreWon("blue"), "Default score for 'blue' should be 10.");
    }

    @Test
    public void testGetScoreLost() {
        assertEquals(5, app.getScoreLost("green"), "Default penalty for 'green' should be 5.");
    }

    @Test
    public void testColourToInt() {
        assertEquals(2, app.colourToInt("blue"), "'blue' should correspond to index 2.");
    }

    @Test
    public void testDrawLines() {
        assertDoesNotThrow(() -> app.drawLines(), "drawLines() should not throw an exception.");
    }

    @Test
    public void testKeyReleased() {
        app.keyReleased();
        assertTrue(true, "keyReleased() should execute without errors.");
    }

    @Test
    public void testRemoveLines() {
        Line line = new Line();
        app.getLines().add(line);
        app.removeLines(line);
        assertTrue(app.getLines().isEmpty(), "Line should be removed.");
    }

    @Test
    public void testBallCatched() {
        Ball ball = new Ball(0, 0, 0, app);
        int initialCount = app.getBallsToRemove().size();
        app.ballCatched(ball);
        assertEquals(initialCount + 1, app.getBallsToRemove().size(), "Captured ball count should increase.");
    }

    @Test
    public void testGetIncreaseMultiplier() {
        assertEquals(1.0f, app.getIncreaseMultiplier(), "Default increase multiplier should be 1.0f.");
    }

    @Test
    public void testGetDecreaseMultiplier() {
        assertEquals(1.0f, app.getDecreaaseMultiplier(), "Default decrease multiplier should be 1.0f.");
    }

    @Test
    public void testLoadLevel() {
        assertDoesNotThrow(() -> app.loadLevel(0), "loadLevel() should execute without errors.");
    }

    @Test
    public void testTimeDisplay() {
        assertDoesNotThrow(() -> app.timeDisplay(), "timeDisplay() should execute without errors.");
    }

    @Test
    public void testDrawGrid() {
        assertDoesNotThrow(() -> app.drawGrid(), "drawGrid() should execute without errors.");
    }

    @Test
    public void testGetGrid() {
        assertNotNull(app.getGrid(), "Grid should not be null.");
    }

    @Test
    public void testGetBallImages() {
        assertNotNull(app.getBallImages(), "Ball images should not be null.");
    }

    @Test
    public void testGetCurrentLevelIndex() {
        assertEquals(0, app.getCurrentLevelIndex(), "Initial level index should be 0.");
    }

    @Test
    public void testGetBallsToSpawn() {
        assertNotNull(app.getBallsToSpawn(), "Balls to spawn list should not be null.");
    }

    @Test
    public void testGetTimeLeft() {
        assertTrue(app.getTimeLeft() > 0, "Time left should be positive at the start of a level.");
    }

    @Test
    public void testGetYellowTile1() {
        assertEquals(0, app.getYellowTile1(), "Initial yellow tile index 1 should be 0.");
    }

    @Test
    public void testGetYellowTileIndex2() {
        assertEquals(app.getTotalEdgeTiles() / 2, app.getYellowTile2(), "Initial yellow tile index 2 should be half the total edge tiles.");
    }

    @Test
    public void testGetBallsToRemove() {
        assertNotNull(app.getBallsToRemove(), "Balls to remove list should not be null.");
    }

    @Test
    public void testMain() {
        assertDoesNotThrow(() -> App.main(new String[]{}), "Main method should not throw an exception.");
    }

    @Test
    public void testGetTile() {
        assertNotNull(app.getTile(0, 0), "Tile at (0, 0) should not be null.");
    }

    @Test
    public void testLevelCompleteAnimation() {
        assertDoesNotThrow(() -> app.levelCompleteAnimation(), "levelCompleteAnimation() should execute without errors.");
    }
}

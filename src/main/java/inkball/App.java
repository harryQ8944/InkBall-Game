package inkball;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;

/**
 * The {@code App} class is the main application class for the Inkball game.
 * It extends {@link PApplet} to utilize the Processing library for graphics and interactions.
 * This class handles game setup, rendering, user input, game logic, and state management.
 * <p>
 * Key functionalities include:
 * <ul>
 *     <li>Loading game configurations and resources</li>
 *     <li>Managing game states such as score, levels, and time</li>
 *     <li>Handling user inputs for drawing lines and controlling the game</li>
 *     <li>Rendering game elements like the grid, balls, tiles, and UI components</li>
 *     <li>Managing game entities including balls, tiles, spawners, and holes</li>
 * </ul>
 * </p>
 *
 * @author HanchengQiu
 * @version 1.0
 * @since 2024-10-27
 */
public class App extends PApplet {

    // Constants for game dimensions and settings
    public static final int TILE_SIZE = 32;
    public static final int GRID_WIDTH = 18; // 576 / 32
    public static final int GRID_HEIGHT = 18; // 576 / 32
    public static final int TOPBAR = 64;
    public static final int WIDTH = TILE_SIZE * GRID_WIDTH; // 576
    public static final int HEIGHT = TILE_SIZE * GRID_HEIGHT + TOPBAR; // 576 + TOPBAR
    public static final int FPS = 30;
    private static final int NEW_FPS = 2; // Number of frames per update for timer and level.
    // One frame is 1/30 ~~ 0.333 per/s, 2 frames is 2x0.33 ~~ 0.067

    // Configuration and Game Resources
    public String configPath;
    public JSONObject config;
    public JSONArray levels;
    public JSONObject currentLevel;
    public PImage background;
    public PImage[] ballImages; // Array to hold the ball images
    public PImage yellowTileImage; // Sprite for yellow tiles

    // Game State Variables
    private boolean isPaused = false;
    private boolean isRestarting = false;
    public boolean isDrawing;
    public boolean levelFinish = false; // Indicates if the level has ended
    private boolean isLevelCompleting = false; // Indicates if the level is in the completion phase

    public int score = 0; // Persistent score across levels
    public int levelStartScore = 0; // Score before the current level started
    public int currentLevelIndex;
    private int spawnFrames; // How many frames between spawns
    private int spawnCount; // Current timer for spawning balls
    private int timeSeconds; // Total time for the level in seconds
    public int timeLeft; // Time remaining in frames
    private int remainTime = 0;
    private int updateScoreFrames = NEW_FPS; // Counter to track frames until the next update

    // Game Entities
    private Tile[][] grid; // 2D array of tiles
    private List<Ball> balls; // Active balls in the game
    private List<Ball> ballsToRemove; // Balls that are marked for removal
    public List<EntryPoint> spawners; // Entry points for spawning balls
    private List<String> ballsToSpawn; // Balls that still need to be spawned
    private List<Hole> holes; // List of holes in the game
    private List<Ball> ballsLoaded; // Balls loaded from file

    // Player Input and Interaction
    public List<Line> Lines; // List to manage multiple player-drawn lines
    public Line drawingLine; // The line currently being drawn

    // Score and Level Configuration
    private Map<String, Integer> scoreWon;
    private Map<String, Integer> scoreLost;

    // Animation and Tile Movement Variables
    public List<PVector> edgePositions;
    private int yellowTile1 = 0;
    private int yellowTile2 = 0;
    private int totalEdgeTiles;

    // Utility Fields
    public static Random random = new Random();

    // Offset for shifting balls in the top bar
    private float ballOffset = 0;
    private final float ballWidth = 30.0f; // Width of one ball as a float
    private final int totalShiftFrames = 30; // Total frames to complete the shift
    private int shiftRemaining = 0; // Frames remaining in the shift animation

    // Timer Font
    PFont monoFont;

    // Load Level
    private String levelLayout = "level1.txt";

    /**
     * Constructs a new {@code App} instance with the default configuration path.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Configures the initial size of the game window.
     * This method is called once when the program starts.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Initializes the game by setting the frame rate, loading resources,
     * initializing game entities, and loading the first level.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        loadImages();
        loadConfig();
        background = loadImage("src/main/resources/inkball/tile.png");
        yellowTileImage = loadImage("src/main/resources/inkball/wall4.png");

        balls = new ArrayList<>();
        ballsToRemove = new ArrayList<>();
        spawners = new ArrayList<>();
        Lines = new ArrayList<>();
        drawingLine = null;
        holes = new ArrayList<>();

        monoFont = createFont("Courier", 20);
        textFont(monoFont);
        loadLevel(currentLevelIndex = 0);
    }

    /**
     * Loads all necessary images for the game, including ball and wall sprites.
     * Images are loaded from the specified resource paths.
     */
    public void loadImages() {
        // Preload ball images (indices 0 to 4)
        ballImages = new PImage[5];
        for (int i = 0; i <= 4; i++) {
            String path = "src/main/resources/inkball/ball" + i + ".png";
            ballImages[i] = loadImage(path);
        }

        // Preload wall images (indices 0 to 4)
        for (int i = 0; i <= 4; i++) {
            String path = "src/main/resources/inkball/wall" + i + ".png";
            PImage wallImage = loadImage(path);
            if (wallImage == null) {
                System.err.println("Failed to load wall from: " + path);
            } else {
                System.out.println("Loaded wall from: " + path);
            }
        }
    }

    /**
     * Loads the game configuration from a JSON file.
     * Initializes scoring maps based on the configuration.
     */
    public void loadConfig() {
        config = loadJSONObject(configPath);
        levels = config.getJSONArray("levels");

        // Initialize scoring maps
        scoreWon = new HashMap<>();
        JSONObject increaseScores = config.getJSONObject("score_increase_from_hole_capture");
        if (increaseScores != null) {
            Set<?> keys = increaseScores.keys(); // Use Set<?> to parameterize the raw type
            for (Object keyObj : keys) {
                String colour = keyObj.toString();
                int score = increaseScores.getInt(colour); // Default to 10 if not found
                scoreWon.put(colour.toLowerCase(), score);
            }
        }
        scoreLost = new HashMap<>();
        JSONObject decreaseScores = config.getJSONObject("score_decrease_from_wrong_hole");
        if (decreaseScores != null) {
            Set<?> keys = decreaseScores.keys(); // Use Set<?> here as well
            for (Object keyObj : keys) {
                String color = keyObj.toString();
                int penalty = decreaseScores.getInt(color, 5); // Default to 5 if not found
                scoreLost.put(color.toLowerCase(), penalty);
            }
        }
        println("Config load finishes!");
    }

    /**
     * Loads a specific level based on the provided index.
     * Initializes level-specific parameters such as spawn intervals and timers.
     *
     * @param i the index of the level to load
     */
    public void loadLevel(int i) {
        if (i >= levels.size()) {
            println("All Levels loaded. Game finishes!");
            noLoop(); // Stop the game loop
            return;
        }

        Lines.clear();

        currentLevel = levels.getJSONObject(i);
        levelLayout = currentLevel.getString("layout");
        loadLevelLayout();

        // Store the current score as the starting score for the level
        levelStartScore = score;

        // Initialize spawn interval
        int spwanTime = currentLevel.getInt("spawn_interval");
        spawnFrames = spwanTime * FPS;
        spawnCount = spawnFrames;

        // Load balls to spawn
        ballsToSpawn = new ArrayList<>();
        JSONArray ballsArray = currentLevel.getJSONArray("balls");
        for (int j = 0; j < ballsArray.size(); j++) {
            ballsToSpawn.add(ballsArray.getString(j));
        }

        // Initialize level timer
        timeSeconds = currentLevel.getInt("time"); // Default to 120 seconds if not specified
        timeLeft = timeSeconds * FPS;

        // Reset level flags
        levelFinish = false;
        isLevelCompleting = false;
    }

    /**
     * Loads the layout of the current level from a text file.
     * Initializes tiles, spawners, holes, and preloaded balls based on the layout.
     */
    public void loadLevelLayout() {
        holes.clear(); // Clear existing holes
        spawners.clear(); // Clear existing spawners
        ballsLoaded = new ArrayList<>(); // Initialize the list of balls from the file
        List<String> tokens = Arrays.asList(loadStrings(levelLayout));
        grid = new Tile[GRID_HEIGHT][GRID_WIDTH]; // 2D ArrayList

        for (int y = 0; y < GRID_HEIGHT; y++) {
            String line = y < tokens.size() ? tokens.get(y) : "";
            for (int x = 0; x < GRID_WIDTH; x++) {
                Tile tile = null;
                if (x < line.length()) {
                    char c = line.charAt(x);

                    if (c == 'X') {
                        tile = new GreyTile(x, y, this);
                        System.out.println("Created Wall at (" + x + ", " + y + ")");
                    } else if (c == '1' || c == '2' || c == '3' || c == '4') {
                        int colour = Character.getNumericValue(c);
                        tile = new ColourTile(x, y, colour, this);
                    } else if (c == 'S') {
                        tile = new EntryPoint(x, y, this);
                        spawners.add((EntryPoint) tile); // Collect spawners
                        System.out.println("Created EntryPoint/Spawner at (" + x + ", " + y + ")");
                    } else if (c == 'H' && x + 1 < line.length()) {
                        char colourChar = line.charAt(x + 1);
                        int holeColour = Character.getNumericValue(colourChar);
                        putHoles(x, y, holeColour);
                        System.out.println("Created Hole at (" + x + ", " + y + ") with color index: " + holeColour);
                        x++; // Skip the next character since it's part of the hole definition
                        continue;
                    } else if (c == 'B' && x + 1 < line.length()) {
                        char colourChar = line.charAt(x + 1);
                        int ballColour = Character.getNumericValue(colourChar);
                        Ball ball = new Ball(x * TILE_SIZE, y * TILE_SIZE, ballColour, this);
                        balls.add(ball); // Add to the list of active balls
                        ballsLoaded.add(ball); // Track that this ball was loaded from the file
                        System.out.println("Created Ball at (" + x + ", " + y + ") with color index: " + ballColour);
                        x++; // Skip the next character since it's part of the ball definition
                    } else {
                        tile = null;
                    }

                    grid[y][x] = tile;
                } else {
                    grid[y][x] = null;
                }
            }
        }
        System.out.println("Level layout loaded from: " + levelLayout);
        System.out.println("Number of holes loaded: " + holes.size());
    }

    /**
     * The main drawing loop that is called repeatedly to render the game.
     * It handles rendering of UI components, game elements, and updates game logic.
     */
    @Override
    public void draw() {
        drawTopBar();
        drawBackground();
        drawGrid();
        drawLines();
        spawnCountdown();
        timeDisplay();
        updateBalls();
        HitAnimation();
        scoreUpdates();
        levelCompleteAnimation();
    }

    /**
     * Draws the top bar UI component, including score, timer, upcoming balls,
     * and game status indicators such as pause and level finish.
     */
    public void drawTopBar() {
        fill(192);
        noStroke(); // No outline (stroke) around shapes.
        rect(0, 0, WIDTH, TOPBAR);

        // Define fixed width and height for the black rectangle
        int rectHeight = TOPBAR - 25; // Adjust the height if needed
        int rectWidth = 160;          // Increased width of the black rectangle
        int rectX = 10;
        int rectY = (TOPBAR - rectHeight) / 2; // Center vertically within the top bar

        // Draw black rectangle with the new wider size
        fill(0); // Black color
        rect(rectX, rectY, rectWidth, rectHeight); // Draw at fixed position with updated width

        // Shift balls when shiftRemaining > 0
        if (shiftRemaining > 0) {
            float shiftAmount = ballWidth / totalShiftFrames;
            ballOffset += shiftAmount;
            shiftRemaining--;
            if (shiftRemaining == 0) {
                ballOffset = 0;
                // Remove the first ball from ballsToSpawn after shift completes
                if (!ballsToSpawn.isEmpty()) {
                    ballsToSpawn.remove(0);
                }
            }
        }

        // Draw up to 5 upcoming balls in the black rectangle
        int max = 5;
        int ballsToShow = Math.min(ballsToSpawn.size(), max);

        for (int i = 0; i < ballsToShow; i++) {
            String colorString = ballsToSpawn.get(i);
            int colorInt = colourToInt(colorString);
            PImage ballImage = ballImages[colorInt]; // Use preloaded sprites
            int startY = (TOPBAR - 25) / 2; // Center the balls vertically

            float x = rectX + 5 + i * ballWidth - ballOffset;
            if (x >= rectX && x <= rectX + rectWidth - ballWidth) {
                image(ballImage, x, startY); // Draw the ball at the updated position
            }
        }

        // Draw spawn interval countdown only if there are balls to spawn
        if (!ballsToSpawn.isEmpty()) {
            fill(0); // Black color for text
            textSize(20);
            textAlign(LEFT, CENTER);
            int countdownX = rectX + rectWidth + 10; // Position to the right of the black rectangle
            String countdownText = String.format("%.1f", spawnCount / (float)FPS); // Show as a decimal
            text(countdownText, countdownX, TOPBAR / 2);
        }

        // Draw score and timer on the top bar
        fill(0); // Black color for text
        textSize(20);
        textAlign(LEFT, CENTER); // Anchor text from the left to prevent shifting

        // Define fixed positions for score and timer
        float scoreX = WIDTH - 150; // Fixed x position for score
        float scoreY = TOPBAR / 2 - 14; // y position for score

        float timerX = WIDTH - 138; // Fixed x position for timer
        float timerY = TOPBAR / 2 + 10; // y position for timer

        // Display score with fixed alignment and leading zeros
        String scoreText = String.format("Score: %03d", score); // Pads score with leading zeros to 3 digits
        text(scoreText, scoreX, scoreY); // Fixed position

        // Display timer with fixed alignment and leading zeros
        int timeRemaining = timeLeft / FPS;
        String timerText = String.format("Time: %03d", timeRemaining); // Pads time with leading zeros to 3 digits
        text(timerText, timerX, timerY); // Fixed position

        // Display end game messages
        if (levelFinish) {
            fill(255, 0, 0); // Red color
            textSize(16);
            textAlign(CENTER, CENTER);
            if (currentLevelIndex >= levels.size()) {
                // Game has ended after the last level
                text("===ENDED===", WIDTH / 2, TOPBAR / 2);
            } else {
                // Level ended due to time running out
                text("===TIME'S UP===", WIDTH / 2, TOPBAR / 2);
            }
        }

        // Display pause indicator if the game is paused
        if (isPaused) {
            fill(255, 0, 0); // Red color for emphasis
            textSize(16);
            textAlign(CENTER, CENTER);
            text("***PAUSED***", WIDTH / 2, TOPBAR / 2);
        }
    }

    /**
     * Draws yellow tiles that move along the edges of the game grid.
     * This method is used for visual effects during level completion.
     */
    public void drawYellowTiles() {
        // Draw yellow tile 1
        PVector p1 = edgePositions.get(yellowTile1);
        int x1 = (int) p1.x * TILE_SIZE;
        int y1 = (int) p1.y * TILE_SIZE + TOPBAR;
        image(yellowTileImage, x1, y1, TILE_SIZE, TILE_SIZE); // Draw the sprite at the current position

        // Draw yellow tile 2
        PVector p2 = edgePositions.get(yellowTile2);
        int x2 = (int) p2.x * TILE_SIZE;
        int y2 = (int) p2.y * TILE_SIZE + TOPBAR;
        image(yellowTileImage, x2, y2, TILE_SIZE, TILE_SIZE); // Draw the sprite at the current position
    }

    /**
     * Draws all player-drawn lines on the game area.
     * Lines are not drawn if the level has finished.
     */
    public void drawLines() {
        if (!levelFinish) {
            for (Line line : Lines) {
                line.draw(this);
            }
        }
    }

    /**
     * Renders the background tiles across the entire game grid.
     */
    private void drawBackground() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                image(background, x * TILE_SIZE, y * TILE_SIZE + TOPBAR);
            }
        }
    }

    /**
     * Draws all tiles present in the game grid.
     * Also updates cooldowns for GreyTiles and ColourTiles.
     */
    public void drawGrid() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                Tile tile = grid[y][x];
                if (tile != null) {
                    tile.draw(this);
                }
                if (tile instanceof GreyTile) {
                    ((GreyTile) tile).updateCooldown();
                } else if (tile instanceof ColourTile) {
                    ((ColourTile) tile).updateCooldown();
                }
            }
        }
    }

    /**
     * Handles all game logic and updates, including rendering,
     * spawning balls, updating timers, and managing game state transitions.
     */
    // (Note: This comment block is a placeholder as detailed JavaDoc is provided for individual methods.)

    /**
     * Adds holes to the game grid based on the specified position and color.
     * Holes occupy a 2x2 tile area and are initialized with their position and color.
     *
     * @param x      the x-coordinate of the hole's top-left tile
     * @param y      the y-coordinate of the hole's top-left tile
     * @param colour the color index of the hole
     */
    public void putHoles(int x, int y, int colour) {
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx < GRID_WIDTH && ny < GRID_HEIGHT) {
                    if (grid[ny][nx] == null) {
                        boolean isDefult = (dx == 0 && dy == 0); // Only true for top-left tile
                        grid[ny][nx] = new HoleTile(nx, ny, colour, this, isDefult);
                        if (isDefult) {
                            // Compute the rectangular area of the hole using PVector
                            PVector holePosition = new PVector(x * TILE_SIZE, y * TILE_SIZE + TOPBAR);
                            PVector holeDimensions = new PVector(TILE_SIZE * 2, TILE_SIZE * 2);

                            // Create the Hole object with position and dimensions
                            Hole hole = new Hole(holePosition, holeDimensions, colour);
                            holes.add(hole);
                            System.out.println("Put Hole at center: " + hole.getCenter() + " with colour " + colour);
                        }
                    } else {
                        System.err.println("Warning: Overlapping hole at (" + nx + ", " + ny + ")");
                    }
                }
            }
        }
    }

    /**
     * Spawns a new ball at a random spawner if there are balls left to spawn.
     * Initiates a shift animation for the upcoming balls in the top bar.
     */
    public void spawnBall() {
        if (!ballsToSpawn.isEmpty() && !spawners.isEmpty()) {
            // Get the first ball from the list of balls to spawn
            String nextBallColour = ballsToSpawn.get(0); // Do not remove yet

            // Convert the colour to the corresponding integer (0-4)
            int colourInt = colourToInt(nextBallColour);

            // Select a random spawner to spawn the ball from
            EntryPoint spawner = spawners.get(random.nextInt(spawners.size()));

            // Create and add the new ball
            Ball newBall = new Ball(spawner.getX() * TILE_SIZE, spawner.getY() * TILE_SIZE, colourInt, this);
            balls.add(newBall);

            // Trigger the gradual shift to the left for remaining balls
            shiftRemaining = totalShiftFrames; // Start shifting by 40 pixels (one ball width)
        }
    }

    /**
     * Manages the countdown for spawning balls.
     * Decrements the spawn counter and spawns a new ball when the counter reaches zero.
     */
    public void spawnCountdown() {
        if (!isPaused && !levelFinish && !isLevelCompleting) {
            if (spawnCount <= 0 && !ballsToSpawn.isEmpty()) {
                spawnBall();
                spawnCount = spawnFrames; // Reset counter after spawning
            } else if (!ballsToSpawn.isEmpty()) {
                spawnCount--;
            }
        }
    }

    /**
     * Displays and updates the remaining time for the current level.
     * Handles level termination when time runs out.
     */
    public void timeDisplay() {
        if (!isPaused && !levelFinish && !isLevelCompleting) {
            // Decrease level time remaining
            if (timeLeft > 0) {
                timeLeft--;
            } else if (!levelFinish) {
                // Level time is up, handle level end as a loss
                checkLoseOrNot();
            }
        }
}

    /**
     * Updates the state of all active balls, including their positions and rendering.
     * Also checks for level completion conditions.
     */
    public void updateBalls() {
        if (!levelFinish) {
            for (Ball ball : balls) {
                ball.update();
                ball.draw();
            }
            balls.removeIf(ball -> ball.isCaptured()); // Remove captured balls
            balls.removeAll(ballsToRemove);
            ballsToRemove.clear();

            // Check for level completion (win condition)
            if (!isLevelCompleting && ballsToSpawn.isEmpty() && balls.isEmpty()) {
                checkWinOrNot();
            }
        } else {
            // Draw balls without updating to keep them on screen
            for (Ball ball : balls) {
                ball.draw();
            }
        }
    }

    /**
     * Animates the movement of yellow tiles along the edges of the grid.
     * Updates their positions based on the total number of edge tiles.
     */
    public void moveYellowTiles() {
        // Move yellow tile 1 clockwise
        yellowTile1 = (yellowTile1 + 1) % totalEdgeTiles;

        // Move yellow tile 2 clockwise
        yellowTile2 = (yellowTile2 + 1) % totalEdgeTiles;

        // Draw yellow tiles at their new positions
        drawYellowTiles();
    }

    /**
     * Detects and processes score updates based on remaining time and game events.
     * Decreases the timer, increases the score, and moves yellow tiles.
     * Concludes the level if the timer reaches zero.
     */
    public void scoreUpdateDetection() {
        // Only perform updates if there is remaining time
        if (timeLeft > 0) {
            // 1. Decrease Timer by 1 second
            timeLeft -= FPS; // Since timeLeft is in frames
            println("Timer decreased by 1 second. Time remaining: " + (timeLeft / FPS) + "s");

            // 2. Increase Score by 1
            score += 1;
            System.out.println("\n\n");
            println("Score increased by 1. Current score: " + score);

            // 3. Move Yellow Tiles by 1 tile
            moveYellowTiles();
            println("Yellow tiles moved by 1 tile.");

            // Decrement the remaining time to add
            if (remainTime > 0) {
                remainTime--;
            }
        } else {
            // Timer has reached zero, conclude the level completion phase
            isLevelFinished();
        }
    }

    /**
     * Handles periodic score updates based on the game's frame rate.
     * This method is called within the main draw loop to ensure timely updates.
     */
    public void scoreUpdates() {
        if (isLevelCompleting && !isPaused) {
            if (updateScoreFrames <= 0) {
                scoreUpdateDetection();
                updateScoreFrames = NEW_FPS; // Reset the counter
            } else {
                updateScoreFrames--;
            }
        }
    }

    /**
     * Creates a list of positions along the edges of the grid.
     * This list is used for animating yellow tiles around the perimeter.
     *
     * @return a list of {@link PVector} representing edge positions
     */
    public List<PVector> createEdges() {
        List<PVector> positions = new ArrayList<>();
        // Top edge (left to right)
        for (int x = 0; x < GRID_WIDTH; x++) {
            positions.add(new PVector(x, 0));
        }
        // Right edge (top to bottom)
        for (int y = 1; y < GRID_HEIGHT; y++) {
            positions.add(new PVector(GRID_WIDTH - 1, y));
        }
        // Bottom edge (right to left)
        for (int x = GRID_WIDTH - 2; x >= 0; x--) {
            positions.add(new PVector(x, GRID_HEIGHT - 1));
        }
        // Left edge (bottom to top)
        for (int y = GRID_HEIGHT - 2; y > 0; y--) {
            positions.add(new PVector(0, y));
        }
        return positions;
    }

    /**
     * Checks if the player has met the win conditions for the current level.
     * If conditions are met, initiates the level completion phase.
     */
    public void checkWinOrNot() {
        isLevelCompleting = true;
        println("Level completed successfully!");

        // Add remaining time to score at the specified rate
        remainTime = timeLeft / FPS; // Convert remaining frames to seconds
        println("Remaining time to add: " + remainTime + " seconds");

        // Initialize edge positions for yellow tile animations
        edgePositions = createEdges();
        totalEdgeTiles = edgePositions.size();

        yellowTile1 = 0; // Starting position for the first yellow tile
        yellowTile2 = totalEdgeTiles / 2; // Starting position for the second yellow tile
        println("Yellow Tiles starting at indices: " + yellowTile1 + ", " + yellowTile2);

        // Reset frame counters for animation
        updateScoreFrames = 2; // 1 unit every 0.067 seconds at ~30 FPS

        // Stop spawning and updating balls
        ballsToSpawn.clear();
        balls.clear();
    }

    /**
     * Handles the scenario when the player loses the level due to time running out.
     * Sets the appropriate flags to indicate level completion.
     */
    public void checkLoseOrNot() {
        levelFinish = true;
        timeLeft = 0; // Ensure the timer is exactly zero
        println("No time left! Level ended!.");
    }

    /**
     * Checks if the level completion animations and score updates are done.
     * If so, proceeds to the next level or ends the game.
     */
    public void checkCompleteOrNot() {
        // Check if it's time to update
        if (updateScoreFrames <= 0) {
            // Perform score update
            scoreUpdateDetection();
            updateScoreFrames = NEW_FPS; // Reset the counter
        } else {
            updateScoreFrames--;
        }

        // Draw yellow tiles
        drawYellowTiles();

        // Check if all remaining time has been added and tiles have completed a full loop
        if (remainTime <= 0 && yellowTile1 == 0 && yellowTile2 == totalEdgeTiles / 2) {
            // Proceed to next level or end game
            if (currentLevelIndex + 1 < levels.size()) {
                currentLevelIndex++;
                loadLevel(currentLevelIndex);
                isLevelCompleting = false;
                println("Proceeding to the next level: " + currentLevelIndex);
            } else {
                // No more levels, end the game
                levelFinish = true;
                println("Game has ended.");
            }
        }
    }

    /**
     * Manages the level completion animations and transitions.
     * Called within the main draw loop to ensure smooth animations.
     */
    public void levelCompleteAnimation() {
        if (isLevelCompleting) {
            checkCompleteOrNot();
        }
    }

    /**
     * Finalizes the level by resetting completion flags and loading the next level
     * or ending the game if all levels are completed.
     */
    private void isLevelFinished() {
        isLevelCompleting = false; // Exit the completion phase
        println("All Level has finished!");
        println("\n\n");

        // Proceed to the next level or end the game
        if (currentLevelIndex + 1 < levels.size()) {
            currentLevelIndex++;
            loadLevel(currentLevelIndex);
            println("Proceed to the next level: " + currentLevelIndex);
            println("\n\n");
        } else {
            levelFinish = true;
            currentLevelIndex = levels.size();
            println("All level loaded. Game ended!");
        }
    }

    /**
     * Handles collision detection and response between balls and tiles.
     * Applies damage or captures balls based on tile types and interactions.
     */
    public void HitAnimation() {
        for (Ball ball : balls) {
            // Determine which tile the ball is over
            int tileX = (int)(ball.getPosition().x / Tile.TILE_SIZE);
            int tileY = (int)((ball.getPosition().y - TOPBAR) / Tile.TILE_SIZE);

            // Boundary checks
            if (tileX < 0 || tileX >= GRID_WIDTH || tileY < 0 || tileY >= GRID_HEIGHT) {
                continue;
            }

            Tile tile = grid[tileY][tileX];
            if (tile instanceof GreyTile) {
                GreyTile wall = (GreyTile) tile;
                wall.getsHit(ball); // Pass the Ball object to the hit method
                if (tile instanceof ColourTile) {
                    ColourTile Cwall = (ColourTile) tile;
                    Cwall.getsHit(ball); // Apply damage based on color matching
                } else {
                    wall.getsHit(ball); // Apply damage for grey walls
                }
            }
        }
    }

    /**
     * Handles key press events.
     * Currently listens for the 'R' key to restart the game.
     *
     * @param event the key event triggered by the user
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (key == 'r' || key == 'R') {
            if (!isRestarting) {
                isRestarting = true;
                restart();
                isRestarting = false;
            }
        }
    }

    /**
     * Handles key release events.
     * Currently listens for the spacebar to toggle the pause state.
     *
     * @param event the key event triggered by the user
     */
    @Override
    public void keyReleased(KeyEvent event){
        if (event.getKey() == ' ') {
            Pause();
        }
    }

    /**
     * Handles mouse press events.
     * Allows the player to start drawing lines or remove existing lines based on the mouse button.
     */
    @Override
    public void mousePressed() {

        if (levelFinish || isLevelCompleting) {
            return; // Prevent drawing lines when the game is paused, ended, or completing
        }

        if (mouseButton == LEFT) {
            // Check if the mouse is below the top bar
            if (mouseY > TOPBAR) {
                // Start a new line if Ctrl is not pressed
                if (!keyPressed || (key != CODED && keyCode != CONTROL)) {
                    drawingLine = new Line();
                    drawingLine.addPoint(new PVector(mouseX, mouseY));
                    Lines.add(drawingLine);
                }
            }
        } else if (mouseButton == RIGHT) {
            // Only allow removing lines if clicked below the top bar
            if (mouseY > TOPBAR) {
                // Remove the nearest line upon right-click
                removeLine(new PVector(mouseX, mouseY));
            }
        }
    }

    /**
     * Handles mouse drag events.
     * Continues drawing the current line as the mouse is dragged.
     */
    @Override
    public void mouseDragged() {

        if (levelFinish || isLevelCompleting) {
            return; // Prevent drawing lines when the game is paused, ended, or completing
        }
        if (drawingLine != null && mouseButton == LEFT) {
            // Only add points if dragging within the game area
            if (mouseY > TOPBAR) {
                drawingLine.addPoint(new PVector(mouseX, mouseY));
            } else {
                println();
                println("Cannot draw it here!!!");
            }
        }
    }

    /**
     * Handles mouse release events.
     * Finalizes the current line being drawn if applicable.
     *
     * @param event the mouse event triggered by the user
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        if (isDrawing && drawingLine != null) {
            finishDrawing();
        }
    }

    /**
     * Finalizes the drawing of a line by adding it to the list of lines
     * if it contains more than one point.
     */
    public void finishDrawing() {
        if (drawingLine != null && drawingLine.getPoints().size() > 1) {
            Lines.add(drawingLine);
        }
        drawingLine = null;
        isDrawing = false;
    }

    /**
     * Calculates the minimum distance between a point and a line.
     *
     * @param point the point to measure from
     * @param line  the line to measure to
     * @return the minimum distance between the point and the line
     */
    public float lineRemoveDistance(PVector point, Line line) {
        float lineRemoveDistance = Float.MAX_VALUE;
        for (int i = 0; i < line.getPoints().size() - 1; i++) {
            PVector p1 = line.getPoints().get(i);
            PVector p2 = line.getPoints().get(i + 1);
            float distance = line.nearLineDistance(point, p1, p2);
            if (distance < lineRemoveDistance) {
                lineRemoveDistance = distance;
            }
        }
        return lineRemoveDistance;
    }

    /**
     * Removes the closest line to a given point from the list of player-drawn lines.
     *
     * @param point the point near which to find and remove a line
     */
    public void removeLine(PVector point) {
        Line Line = null;
        float lineRemoveDistance = Float.MAX_VALUE;

        for (Line line : Lines) {
            // Calculate the distance from the point to the line
            float distance = lineRemoveDistance(point, line);
            if (distance < lineRemoveDistance) {
                lineRemoveDistance = distance;
                Line = line;
            }
        }

        if (Line != null) {
            Lines.remove(Line);
            System.out.println();
            println("A line has been removed!");
            System.out.println();
        }
    }

    /**
     * Restarts the game by clearing all game entities, resetting scores,
     * and reloading the current or initial level based on the game state.
     */
    public void restart() {
        System.out.println("\n\n\n");
        System.out.println("Wanna Restart? OK ..... Restarting ...");
        System.out.println();

        // Clear active balls
        balls.clear();
        ballsToRemove.clear();
        ballsLoaded.clear();

        // Clear spawners
        spawners.clear();

        // Clear player-drawn lines
        Lines.clear();

        // Reset
        if (levelFinish && currentLevelIndex >= levels.size()) {
            // Game has ended after the last level, reset score to initial value
            score = 0;
            // Reset to initial level (level 0)
            currentLevelIndex = 0;
            println("Restarting from the initial level.");
        } else {
            // Restart the current level, keep the score as levelStartScore
            score = levelStartScore;
            println("Restarting the current level: " + currentLevelIndex);
        }

        // Load the appropriate level
        loadLevel(currentLevelIndex);

        // Reset spawn counters
        JSONObject currentLevel = levels.getJSONObject(currentLevelIndex);
        if (currentLevel != null) {
            int spwanTime = currentLevel.getInt("spawn_interval"); // Default to 10 seconds
            spawnFrames = spwanTime * FPS;
            spawnCount = spawnFrames;

            // Reset level timer
            timeSeconds = currentLevel.getInt("time");
            timeLeft = timeSeconds * FPS;
        }

        // Reset level completion flags
        levelFinish = false;
        isLevelCompleting = false;
        System.out.println();
        System.out.println("Restart Finished!");
        System.out.println("\n\n\n");
        if (levelFinish && currentLevelIndex >= levels.size()) {
            System.out.println("Game restarted from the initial level!");
        } else {
            System.out.println("Current level restarted!");
        }
        System.out.println("\n\n\n");
    }

    /**
     * Converts a colour name string to its corresponding integer index.
     *
     * @param colourString the name of the colour (e.g., "grey", "orange")
     * @return the integer index representing the colour
     */
    public int colourToInt(String colourString) {
        colourString = colourString.toLowerCase();
        if (colourString.equals("grey")) {
            return 0;
        } else if (colourString.equals("orange")) {
            return 1;
        } else if (colourString.equals("blue")) {
            return 2;
        } else if (colourString.equals("green")) {
            return 3;
        } else if (colourString.equals("yellow")) {
            return 4;
        } else {
            return 0; // Default case
        }
    }

    /**
     * Converts a colour index to its corresponding colour name string.
     *
     * @param colourInt the integer index representing the colour
     * @return the name of the colour as a string
     */
    public String colourToString(int colourInt) {
        if (colourInt == 0) {
            return "grey";
        } else if (colourInt == 1) {
            return "orange";
        } else if (colourInt == 2) {
            return "blue";
        } else if (colourInt == 3) {
            return "green";
        } else if (colourInt == 4) {
            return "yellow";
        } else {
            return "grey"; // Default case
        }
    }


    /**
     * Retrieves the current game grid.
     *
     * @return a 2D array of {@link Tile} representing the game grid
     */
    public Tile[][] getGrid() {
        return grid;
    }

    /**
     * Retrieves the ball image corresponding to the given colour index.
     *
     * @param i the colour index of the ball
     * @return the {@link PImage} representing the ball's colour, or {@code null} if invalid
     */
    public PImage getBallColourIndex(int i) {
        if (i >= 0 && i < ballImages.length) {
            return ballImages[i];
        } else {
            System.err.println("Invalid sprite index: " + i);
            return null;
        }
    }

    /**
     * Retrieves the tile at the specified grid coordinates.
     *
     * @param x the x-coordinate in the grid
     * @param y the y-coordinate in the grid
     * @return the {@link Tile} at the specified position, or {@code null} if out of bounds
     */
    public Tile getTile(int x, int y) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            return grid[y][x];
        } else {
            return null;
        }
    }

    /**
     * Retrieves the list of holes present in the game.
     *
     * @return a list of {@link Hole} objects
     */
    public List<Hole> getHoles() {
        return holes;
    }

    /**
     * Retrieves the array of preloaded ball images.
     *
     * @return an array of {@link PImage} representing ball sprites
     */
    public PImage[] getBallImages() {
        return ballImages;
    }

    /**
     * Toggles the pause state of the game.
     * When paused, the game loop stops updating; when unpaused, it resumes.
     */
    protected void Pause() {
        isPaused = !isPaused;
        if (isPaused) {
            System.out.println();
            println("Game is freezed!");
        } else {
            System.out.println();
            println("Let's gooooooooooo!");
            System.out.println();
        }
    }

    /**
     * Checks if the game is currently paused.
     *
     * @return {@code true} if the game is paused, {@code false} otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Checks if the game has ended.
     *
     * @return {@code true} if the game has ended, {@code false} otherwise
     */
    public boolean hasEnded() {
        return levelFinish;
    }

    /**
     * Checks if the level is currently in the completion phase.
     *
     * @return {@code true} if the level is completing, {@code false} otherwise
     */
    public boolean isCompleting() {
        return isLevelCompleting;
    }

    /**
     * Retrieves the list of all player-drawn lines.
     *
     * @return a list of {@link Line} objects
     */
    public List<Line> getLines() {
        return Lines;
    }

    /**
     * Removes a specific line from the list of player-drawn lines.
     *
     * @param line the {@link Line} to be removed
     */
    public void removeLines(Line line) {
        Lines.remove(line);
    }

    /**
     * Marks a ball for removal from the active balls list.
     *
     * @param ball the {@link Ball} to be removed
     */
    public void ballCatched(Ball ball) {
        ballsToRemove.add(ball);
    }

    /**
     * Adds a new ball to the spawn queue based on its color.
     *
     * @param colorString the color of the ball to be spawned
     */
    public void ballRefilled(String colorString) {
        ballsToSpawn.add(colorString);
        println("Ball refilled: " + colorString);
    }

    /**
     * Increases the player's score by a specified amount.
     *
     * @param amount the amount to add to the score
     */
    public void addingScore(int amount) {
        score += amount;
        println("Score added by " + amount + ". Total score: " + score);
    }

    /**
     * Decreases the player's score by a specified amount.
     *
     * @param amount the amount to subtract from the score
     */
    public void reducingScore(int amount) {
        score -= amount;
        println("Score decreased by " + amount + ". Total score: " + score);
    }

    /**
     * Retrieves the score increment associated with capturing a hole of a specific color.
     *
     * @param colourString the color of the hole
     * @return the score increment value
     */
    public int getScoreWon(String colourString) {
        return scoreWon.getOrDefault(colourString, 10);
    }

    /**
     * Retrieves the score decrement associated with failing to capture a hole of a specific color.
     *
     * @param colourString the color of the hole
     * @return the score decrement value
     */
    public int getScoreLost(String colourString) {
        return scoreLost.getOrDefault(colourString, 5);
    }

    /**
     * Retrieves the score increase multiplier from the current level's configuration.
     *
     * @return the score increase multiplier
     */
    public float getIncreaseMultiplier() {
        return currentLevel.getFloat("score_increase_from_hole_capture_modifier", 1.0f);
    }

    /**
     * Retrieves the score decrease multiplier from the current level's configuration.
     *
     * @return the score decrease multiplier
     */
    public float getDecreaaseMultiplier() {
        return currentLevel.getFloat("score_decrease_from_wrong_hole_modifier", 1.0f);
    }

    /**
     * Retrieves the current score of the player.
     *
     * @return the player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Retrieves the current level index.
     *
     * @return the index of the current level
     */
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    /**
     * Retrieves the list of active balls in the game.
     *
     * @return a list of {@link Ball} objects
     */
    public List<Ball> getBalls() {
        return balls;
    }

    /**
     * Retrieves the list of balls that are queued to be spawned.
     *
     * @return a list of ball color strings
     */
    public List<String> getBallsToSpawn() {
        return ballsToSpawn;
    }

    /**
     * Sets the remaining time left in the current level.
     *
     * @param time the time left in frames
     */
    public void setTimeLeft(int time) {
        this.timeLeft = time;
    }

    /**
     * Retrieves the remaining time left in the current level.
     *
     * @return the time left in frames
     */
    public int getTimeLeft() {
        return timeLeft;
    }

    /**
     * Checks if the level has finished based on the remaining time.
     * If the time has run out, it sets the levelFinish flag to true.
     */
    public void checkIfLevelFinished() {
        if (timeLeft <= 0) {
            levelFinish = true;
        }
    }

    /**
     * Retrieves the index of the first yellow tile.
     *
     * @return the index of yellowTile1
     */
    public int getYellowTile1() {
        return yellowTile1;
    }

    /**
     * Retrieves the index of the second yellow tile.
     *
     * @return the index of yellowTile2
     */
    public int getYellowTile2() {
        return yellowTile2;
    }

    /**
     * Retrieves the list of balls that are marked for removal.
     *
     * @return a list of {@link Ball} objects to remove
     */
    public List<Ball> getBallsToRemove() {
        return ballsToRemove;
    }

    /**
     * Retrieves the total number of edge tiles used for animating yellow tiles.
     *
     * @return the total number of edge tiles
     */
    public int getTotalEdgeTiles() {
        return this.totalEdgeTiles;
    }

    /**
     * Sets the path to the configuration file.
     *
     * @param configPath the path to the configuration JSON file
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * The main entry point of the application.
     * Initializes and starts the Processing sketch.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}

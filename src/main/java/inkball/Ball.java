package inkball;
import processing.core.PImage;
import processing.core.PVector;
import java.util.*;

/**
 * Represents a Ball in the Inkball game.
 * Handles movement, collision detection with wall tiles and player-drawn lines,
 * velocity reflection based on collision normals, and line removal upon collision.
 */
public class Ball {
    private PVector position; // Position in pixels
    private PVector velocity; // Velocity vector
    private int colour;
    private PImage ballImage;
    private App app;

    private float initialSize; // Original size of the ball (diameter)
    private float currentSize; // Current size of the ball (diameter)
    public boolean collided = false;
    private Hole holeHasBall; // The hole the ball is being captured into

    private State state = State.MOVING;
    
    private enum State {
        MOVING,
        BEING_CATCH,
        CAPTURED
    }

    public Ball(float x, float y, int colour, App app) {
        this.app = app;
        this.colour = colour;
        this.ballImage = app.getBallColourIndex(colour); // Retrieve the ballImage based on color index
        this.initialSize = Math.max(ballImage.width, ballImage.height);
        this.currentSize = this.initialSize;
        this.position = new PVector(x, y + App.TOPBAR); // Adjust y for the top bar
        initializeVelocity(); // Initialize velocity with random direction
        System.out.println("Ball initialized at position: " + position + " with velocity: " + velocity);
    }

    /**
     * Initializes the ball's velocity with random components.
     * Each component (vx and vy) is randomly set to either -1 or 1 pixels per frame.
     * This adjustment is made for a game running at 60 FPS to maintain consistent movement vel.
     */
    private void initializeVelocity() {
        float[] vel = {-2.0f, 2.0f};
        float vx = vel[(int) app.random(2)];
        float vy = vel[(int) app.random(2)];
        this.velocity = new PVector(vx, vy);
    }

    /**
     * Updates the ball's position and handles all collisions.
     */
    public void update() {
        if (app != null && app.isPaused()) {
            return; // Do not update ball if the game is paused
        }

        if (app.isPaused() || app.hasEnded() || app.isCompleting()) {
            return; // Do not update ball if the game is paused, ended, or completing
        }
        
        switch (state) {
            case MOVING:
                // Move the ball based on its velocity
                position.add(velocity);
    
                // Handle collisions
                wallCollideDetections();
                checkWindowCollide();
                checkLineCollide();

                holeAttracts();
                break;
    
            case BEING_CATCH:
                // Move the ball towards the center of the hole
                movingIntoHole();
                break;
    
            case CAPTURED:
                // Ball has been captured; no further action needed
                break;
        }
    }

    /**
     * Handles collisions with the window boundaries by reversing the appropriate velocity components.
     */
    public void checkWindowCollide() {
        // Left Boundary
        if (position.x <= 0) {
            position.x = 0;
            velocity.x *= -1;
            collided = true;
            System.out.println();
            System.out.println("Ball collided with the Left boundary. Reversing vx to " + velocity.x);
        }

        // Right Boundary
        if (position.x + currentSize >= App.WIDTH) {
            position.x = App.WIDTH - currentSize;
            velocity.x *= -1;
            collided = true;
            System.out.println();
            System.out.println("Ball collided with the Right boundary. Reversing vx to " + velocity.x);
        }

        // Top Boundary (considering TOPBAR)
        if (position.y <= App.TOPBAR) {
            position.y = App.TOPBAR;
            velocity.y *= -1;
            collided = true;
            System.out.println();
            System.out.println("Ball collided with the Top boundary. Reversing vy to " + velocity.y);
        }

        // Bottom Boundary
        if (position.y + currentSize >= App.HEIGHT) {
            position.y = App.HEIGHT - currentSize;
            velocity.y *= -1;
            collided = true;
            System.out.println();
            System.out.println("Ball collided with the Bottom boundary. Reversing vy to " + velocity.y);
        }
    }

    /**
     * caps a value between a minimum and maximum.
     *
     */
    public float cap(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Determines the side of collision and reflects the ball's velocity accordingly.
     *
     * @param tile The tile with which the collision has occurred.
     */
    public void handleWallReflection(Tile tile) {
        // Ball properties
        PVector ballCenter = PVector.add(position, new PVector(currentSize / 2.0f, currentSize / 2.0f));
        float radius = currentSize / 2.0f;

        // Tile properties
        float rectX = tile.getX() * App.TILE_SIZE;
        float rectY = tile.getY() * App.TILE_SIZE + App.TOPBAR;
        float rectWidth = App.TILE_SIZE;
        float rectHeight = App.TILE_SIZE;

        // Find the closest point on the rectangle to the ball's center
        PVector closestPoint = new PVector(
                cap(ballCenter.x, rectX, rectX + rectWidth),
                cap(ballCenter.y, rectY, rectY + rectHeight)
        );

        // Calculate the distance vector between the ball's center and the closest point
        PVector distanceVector = PVector.sub(ballCenter, closestPoint);
        float distance = distanceVector.mag();

        // Prevent division by zero
        if (distance == 0) {
            // Arbitrary normal vector
            distanceVector = new PVector(1, 0);
            distance = 1;
        }

        // Normalize the distance vector to get the collision normal
        PVector normal = distanceVector.copy().normalize();

        // Reflect the velocity vector based on the collision normal
        float dotProduct = velocity.dot(normal);
        velocity.sub(PVector.mult(normal, 2 * dotProduct));
        System.out.println();
        System.out.println("Velocity after reflection: " + velocity);

        // Move the ball out of collision
        float overlap = radius - distance;
        PVector correction = PVector.mult(normal, overlap);
        position.add(correction);
        System.out.println("Ball position corrected by: " + correction);

        // Change the ball's color if colliding with a ColouredWallTile
        if (tile instanceof GreyTile) {
            GreyTile wall = (GreyTile) tile;
            System.out.println("Ball hit Wall at (" + tile.getX() + ", " + tile.getY() + ").");
            wall.getsHit(this); // Apply damage to the wall
        }
        if (tile instanceof ColourTile) {
            int newColour = ((ColourTile) tile).getColour();
            ColourTile cWall = (ColourTile) tile;
            System.out.println();
            System.out.println("Ball hit ColouredWall at (" + tile.getX() + ", " + tile.getY() +")");
            cWall.getsHit(this);
            changeColour(newColour);
            
        }
    }
    
    /**
     * Handles collisions between the ball and wall tiles.
     * Implements collision detection and response similar to the provided float-based logic,
     * but adapted to use PVectors.
     */
    public void wallCollideDetections() {
        // Define the ball's bounding box
        float left = position.x;
        float right = position.x + currentSize;
        float top = position.y;
        float bottom = position.y + currentSize;

        // Determine the range of tiles the ball overlaps
        int startTileX = (int) (left / App.TILE_SIZE);
        int endTileX = (int) (right / App.TILE_SIZE);
        int startTileY = (int) ((top - App.TOPBAR) / App.TILE_SIZE);
        int endTileY = (int) ((bottom - App.TOPBAR) / App.TILE_SIZE);

        // cap the tile indices to valid ranges
        startTileX = Math.max(0, startTileX);
        endTileX = Math.min(App.GRID_WIDTH - 1, endTileX);
        startTileY = Math.max(0, startTileY);
        endTileY = Math.min(App.GRID_HEIGHT - 1, endTileY);

        // Iterate through all overlapping tiles
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                Tile tile = app.getTile(tileX, tileY);
                if (tile != null && tile.canBeHit()) {
                    if (checkWallCollide(tile)) {
                        handleWallReflection(tile);
                    }
                }
            }
        }
    }

    /**
     * Checks for collision between the ball (circle) and a given tile (rectangle).
     *
     * @param tile The tile to check collision against.
     * @return True if collision occurs, else false.
     */
    public boolean checkWallCollide(Tile tile) {
        // Ball properties
        PVector ballCenter = PVector.add(position, new PVector(currentSize / 2.0f, currentSize / 2.0f));
        float radius = currentSize / 2.0f;

        // Tile properties
        float rectX = tile.getX() * App.TILE_SIZE;
        float rectY = tile.getY() * App.TILE_SIZE + App.TOPBAR;
        float rectWidth = App.TILE_SIZE;
        float rectHeight = App.TILE_SIZE;

        // Find the closest point on the rectangle to the ball's center
        float closestX = cap(ballCenter.x, rectX, rectX + rectWidth);
        float closestY = cap(ballCenter.y, rectY, rectY + rectHeight);

        // Calculate the distance between the ball's center and this closest point
        float distanceX = ballCenter.x - closestX;
        float distanceY = ballCenter.y - closestY;

        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        return distanceSquared <= (radius * radius);
    }

    

    public void holeAttracts() {
        List<Hole> holes = app.getHoles();

        for (Hole hole : holes) {
            PVector holePosition = hole.getPosition();
            PVector holeDimensions = hole.getDimensions();
            PVector ballCenter = PVector.add(position, new PVector(currentSize / 2.0f, currentSize / 2.0f));

            // Check for collision between ball and hole
            if (circleIntersectsRectangle(ballCenter, currentSize / 2.0f, holePosition, holeDimensions)) {
                // Ball is entering the hole
                // Transition to being captured state
                state = State.BEING_CATCH;
                holeHasBall = hole;
                break; // Only capture one hole at a time
            }
        }
    }

    public boolean circleIntersectsRectangle(PVector circleCenter, float radius, PVector rectPos, PVector rectSize) {
        float closestX = cap(circleCenter.x, rectPos.x, rectPos.x + rectSize.x);
        float closestY = cap(circleCenter.y, rectPos.y, rectPos.y + rectSize.y);
    
        float distanceX = circleCenter.x - closestX;
        float distanceY = circleCenter.y - closestY;
    
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
    
        return distanceSquared < (radius * radius);
    }

    public void checkHoleInside() {
        boolean isGreyBall = (this.colour == 0);
        boolean isGreyHole = (holeHasBall.getColour() == 0);

        boolean isSuccess;

        if (isGreyBall || isGreyHole) {
            isSuccess = true;
        } else if (this.colour == holeHasBall.getColour()) {
            isSuccess = true;
        } else {
            isSuccess = false;
        }

        String colorString = app.colourToString(this.colour);

        if (isSuccess) {
            int baseScore = app.getScoreWon(colorString);
            float multiplier = app.getIncreaseMultiplier();
            int pointsToAdd = Math.round(baseScore * multiplier);
            app.addingScore(pointsToAdd);
            System.out.println("Good Ball goes into hole successful! Score increased by " + pointsToAdd + ".");
        } else {
            int decrease = app.getScoreLost(colorString);
            float multiplier = app.getDecreaaseMultiplier();
            int decreaseScore = Math.round(decrease * multiplier);
            app.reducingScore(decreaseScore);

            // Refilled the ball
            app.ballRefilled(colorString);
            System.out.println("Wrong Ball gets into Wrong hole! Unsuccessful! Score decreased by " + decreaseScore + " and ball refilled.");
        }
    }

    public void finishCatching() {
        // Handle scoring and game logic
        checkHoleInside();

        // Mark the ball as captured
        state = State.CAPTURED;

        // Remove the ball from the game
        app.ballCatched(this);
    }


    public void movingIntoHole() {
        if (holeHasBall == null) {
            // Safety check
            state = State.MOVING;
            return;
        }

        PVector holeCenter = holeHasBall.getCenter();
        PVector ballCenter = PVector.add(position, new PVector(currentSize / 2.0f, currentSize / 2.0f));

        PVector toHoleCenter = PVector.sub(holeCenter, ballCenter);
        float distance = toHoleCenter.mag();

        // Move the ball towards the hole center
        float moveRate = 2.0f; // Adjust as needed
        PVector moveVector = toHoleCenter.copy().normalize().mult(moveRate);
        position.add(moveVector);

        // Reduce the ball size
        float reduceBallSize = 0.9f; // Reduce size by 10% each frame
        currentSize *= reduceBallSize;
        if (currentSize < 1.0f) {
            currentSize = 1.0f;
        }

        // Check if the ball has reached the hole center
        if (distance < 1.0f) {
            // Finalize capture
            finishCatching();
        }
    }

    /**
     * Calculates the projectedPoint point of a point onto a line point.
     *
     * @param p The point to project.
     * @param a The first endpoint of the line point.
     * @param b The second endpoint of the line point.
     * @return The projectedPoint point as a PVector.
     */
    public PVector getProjectPoint(PVector p, PVector a, PVector b) {
        PVector ab = PVector.sub(b, a);
        float abSquared = ab.magSq();
        if (abSquared == 0) {
            return a.copy();
        }
        float t = PVector.sub(p, a).dot(ab) / abSquared;
        t = cap(t, 0, 1);
        return PVector.add(a, PVector.mult(ab, t));
    }


    /**
     * Handles collisions between the ball and player-drawn lines.
     * Reflects the ball's velocity based on the line's normal vector and removes the line upon collision.
     */
    public void checkLineCollide() {
        // Access the list of player-drawn lines from the App class
        List<Line> lines = app.getLines();
        PVector ballCenter = PVector.add(position, new PVector(currentSize / 2.0f, currentSize / 2.0f));

        for (Line line : lines) {
            Map<Integer, PVector> points = line.getPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                PVector p1 = points.get(i);
                PVector p2 = points.get(i + 1);

                float distance = line.nearLineDistance(ballCenter, p1, p2);
                float collisionThreshold = (currentSize / 2.0f) + (line.getThickness() / 2.0f);

                if (distance <= collisionThreshold) {
                    // Collision detected
                    System.out.println();
                    System.out.println("Ball hit with line point: (" + p1 + ") to (" + p2 + ")");

                    // Calculate the direction vector of the line point
                    PVector point = PVector.sub(p2, p1).normalize();

                    // Calculate the normal vector perpendicular to the point
                    PVector normal = new PVector(-point.y, point.x);

                    // Determine the correct normal direction based on the ball's position
                    PVector projectedPoint = getProjectPoint(ballCenter, p1, p2);
                    PVector toBall = PVector.sub(ballCenter, projectedPoint);
                    if (toBall.dot(normal) < 0) {
                        normal.mult(-1);
                    }

                    // Reflect the velocity vector across the normal
                    float dotProduct = velocity.dot(normal);
                    velocity.sub(PVector.mult(normal, 2 * dotProduct));
                    System.out.println("Velocity after reflection: " + velocity);

                    // Correct the ball's position to prevent sticking
                    PVector correction = PVector.mult(normal, collisionThreshold - distance);
                    position.add(correction);
                    System.out.println("Ball position corrected by: " + correction);

                    // Remove the collided line from the game
                    app.removeLines(line);
                    System.out.println("\n\n\n");
                    System.out.println("Player-drawn line removed due to collision.");
                    System.out.println("\n\n\n");

                    // Exit after handling one collision to prevent multiple reflections
                    return;
                }
            }
        }
    }

    public int getColour() {
        return colour;
    }

    public void changeColour(int newColor) {
        if (newColor >= 0 && newColor < app.getBallImages().length) {
            this.colour = newColor;
            this.ballImage = app.getBallColourIndex(newColor); // Update ballImage based on new color index
            System.out.println("Ball colour changed to index " + newColor);
        }
    }

    public PVector getPosition() {
        return position;
    }

    public boolean isCaptured() {
        return state == State.CAPTURED;
    }

    
    /**
     * Draws the ball on the screen using its ballImage or a default circle if the ballImage is missing.
     */
    public void draw() {
        if (state == State.CAPTURED) {
            // Do not draw the ball if it's captured
            return;
        }
    
        if (ballImage != null) {
            app.image(ballImage, position.x, position.y, currentSize, currentSize);
        } else {
            app.noStroke();
            app.fill(255, 0, 0);
            app.ellipse(position.x + (currentSize / 2.0f), position.y + (currentSize / 2.0f), currentSize, currentSize);
        }
    }
}







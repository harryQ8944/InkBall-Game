package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.LinkedHashMap;
import java.util.Map;


public class Line {
    // private List<PVector> points; // Points defining the line segments
    private final float THICKNESS = 10.0f; // Thickness of the drawn lines
    private LinkedHashMap<Integer, PVector> points;
    private int counter = 0; // Unique key for each point

    public Line() {
        points = new LinkedHashMap<>();
    }

    public float cap(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     *
     * @param p The point to measure the distance from.
     * @param a The first endpoint of the line segment.
     * @param b The second endpoint of the line segment.
     * @return The shortest distance from point p to the line segment ab.
     */
    public float nearLineDistance(PVector p, PVector a, PVector b) {
        PVector ap = PVector.sub(p, a);
        PVector ab = PVector.sub(b, a);
        float abSquared = ab.magSq();
        
        // Handle degenerate case where a and b are the same point
        if (abSquared == 0) {
            return PVector.dist(p, a);
        }
        
        // Calculate the projection scalar of point p onto line ab
        float t = ap.dot(ab) / abSquared;
        
        // Clamp t to the range [0, 1] to restrict to segment ab
        t = cap(t, 0, 1);
        
        // Find the projection point on the segment
        PVector projection = PVector.add(a, PVector.mult(ab, t));
        
        // Return the distance from p to the projection
        return PVector.dist(p, projection);
    }


    /**
     * Checks if a point is near any segment of the line within a given proximity.
     *
     * @param point     The point to check.
     * @param proximity The proximity threshold.
     * @return True if the point is near any segment, else false.
     */
    public boolean checkNearLinePoint(PVector point, float proximity) {
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            float distance = nearLineDistance(point, p1, p2);
            if (distance <= proximity + THICKNESS / 2) { // Consider thickness
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a point to the line.
     * Ensures that the point lies within the game board area (y > TOPBAR).
     */
    public void addPoint(PVector point) {
        if (point.y > App.TOPBAR) { // Ensure point is below the top bar
            // Clamp the point to the game area boundaries
            float capedX = cap(point.x, 0, App.WIDTH);
            float capedY = cap(point.y, App.TOPBAR, App.HEIGHT);
            points.put(counter++, new PVector(capedX, capedY));
        }
    }

    public void removeLastPoint() {
        if (!points.isEmpty()) {
            points.remove(points.size() - 1);
        }
    }

    public void clearAllPoints() {
        points.clear();
    }

    public Map<Integer, PVector> getPoints() {
        return points;
    }

    public float getThickness() {
        return THICKNESS;
    }

    public void draw(PApplet app) {
        if (points.size() < 2) {
        return; // No need to draw if there are fewer than 2 points
    }

        app.stroke(0); // Black color
        app.strokeWeight(THICKNESS);
        app.noFill();
        
        app.beginShape(); // Start a new shape
        for (PVector point : points.values()) {
            app.vertex(point.x, point.y);
        }
        app.endShape();
    }
}

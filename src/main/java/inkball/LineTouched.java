package inkball;

import processing.core.PVector;

/**
 * Represents a single line segment between two points.
 */
public class LineTouched {
    private PVector p1;
    private PVector p2;

    /**
     * Constructor for LineSegment.
     *
     * @param p1 Starting point.
     * @param p2 Ending point.
     */
    public LineTouched(PVector p1, PVector p2) {
        this.p1 = p1.copy();
        this.p2 = p2.copy();
    }

    /**
     * Gets the starting point.
     */
    public PVector getP1() {
        return p1;
    }

    /**
     * Gets the ending point.
     */
    public PVector getP2() {
        return p2;
    }
}


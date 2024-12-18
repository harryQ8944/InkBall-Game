package inkball;

import processing.core.PVector;

public class Hole {
    private int colour;
    private PVector position; // The top-left corner position of the hole
    private PVector dimensions;// Width and height of the hole

    public Hole(PVector position, PVector dimensions, int colour) {
        this.colour = colour;
        this.position = position;
        this.dimensions = dimensions;
    }

    public PVector getCenter() {
        return new PVector(position.x + dimensions.x / 2, position.y + dimensions.y / 2);
    }

    public int getColour() {
        return colour;
    }

    public PVector getPosition() {
        return position;
    }

    public PVector getDimensions() {
        return dimensions;
    }
}


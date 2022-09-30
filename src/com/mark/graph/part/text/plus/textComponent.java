package com.mark.graph.part.text.plus;

import java.awt.*;

public abstract class textComponent {
    public gp parent;
    public textComponent(gp parent) {
        this.parent = parent;
    }
    /**If this returns anything but null, getW(), getT(), getB(), etc will be completely ignored. This is set for things like newlines etc.*/
    public abstract specialCase isSpecial();
    public enum specialCase {
        NewLine,
    }
    // Text size in ???
    /**1.0 = Height of "normal" character in line, assuming scale is 1.0. For any other scale, the actual pixel-sizes are scale * getW/T/B().*/
    public abstract double getW();
    /**The highest point of this textComponent. For normal text, this is roughly -0.5 (aka half a line above the line's center).*/
    public abstract double getT();
    /**The lowest point of this textComponent. For normal text, this is roughly 0.5 (aka half a line below the line's center);*/
    public abstract double getB();
    public double getH() { return getB() - getT(); }

    /**This method is called to ask the textComponent to draw itself to the Graphics2D object provided. It provides:
     * x (the leftmost, absolute pixel-coordinate of this tC),
     * y (the middle absolute pixel-coordinate of this tC),
     * scale (a value that should linearly scale the component. If this is 1, the pixel-sizes should be equal to scale * getW/T/B())
     */
    public abstract void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions);

    /**
     * Parses the string as if it was the contents of this component's string representations, for example:
     * "monospace:"some text"more" would cause "new monospaceText().SelfFromString(...)" to be called with the index of the " from "some.
     * Note that the implementation of this method must always return the index of the first character that is unrelated to itself, in the example above, that would be the m of more().
     */
    public abstract int SelfFromString(String str, int indexOfFirstChar);
}

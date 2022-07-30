package com.mark.graph.part.text.plus.components.container;

import com.mark.graph.part.text.plus.componentsFromString;
import com.mark.graph.part.text.plus.textComponent;

import java.awt.*;
import java.util.ArrayList;

public class line extends textComponent {
    private ArrayList<textComponent> textComponents = new ArrayList<>();
    private double width;
    private double highest;
    private double lowest;
    private double height;

    public line() {
        resetMetrics();
    }

    @Override
    public specialCase isSpecial() {
        return null;
    }

    @Override
    public void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions) {
        for (textComponent component : textComponents) {
            component.draw(Img, x, y, scale, blockThreadedActions);
            x += component.getW() * scale;
        }
    }

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        clearTextComponents();
        var o = componentsFromString.componentsFromString(str, indexOfFirstChar);
        if (o == null) {
            return -1;
        }
        for (var c : o.comps) {
            addTextComponent(c);
        }
        return o.nextFirstChar;
    }

    public void clearTextComponents() {
        textComponents.clear();
        resetMetrics();
    }
    public void addTextComponent(textComponent newTC) {
        textComponents.add(newTC);
        resetMetrics();
    }

    private void resetMetrics() {
        width = Double.NaN;
        highest = Double.NaN;
        lowest = Double.NaN;
        height = Double.NaN;
    }

    @Override
    public double getW() {
        if (Double.isNaN(width)) {
            width = 0;
            for (textComponent tc : textComponents) {
                width += tc.getW();
            }
        }
        return width;
    }
    @Override
    public double getT() {
        if (Double.isNaN(highest)) {
            highest = Double.MAX_VALUE;
            for (textComponent tc : textComponents) {
                double T = tc.getT();
                if (T < highest) {
                    highest = T;
                }
            }
        }
        return highest;
    }
    @Override
    public double getB() {
        if (Double.isNaN(lowest)) {
            lowest = -Double.MAX_VALUE;
            for (textComponent tc : textComponents) {
                double B = tc.getB();
                if (B > lowest) {
                    lowest = B;
                }
            }
        }
        return lowest;
    }
    public double getHeight() {
        if (Double.isNaN(height)) {
            height = 2 * Math.max(getB(), -getT()); // double how far it goes up/down (the max of these two, so that the line's center is actually in the center.)
        }
        return height;
    }
}

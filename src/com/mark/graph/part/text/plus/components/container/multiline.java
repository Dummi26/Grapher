package com.mark.graph.part.text.plus.components.container;

import com.mark.graph.part.text.plus.componentsFromString;
import com.mark.graph.part.text.plus.gp;
import com.mark.graph.part.text.plus.textComponent;

import java.awt.*;
import java.util.ArrayList;

public class multiline extends textComponent {
    public multiline(gp parent) { super(parent); resetMetrics(); }
    private ArrayList<textComponent> textComponents = new ArrayList<>();
    private double width;
    private double height;

    @Override
    public specialCase isSpecial() {
        return null;
    }

    @Override
    public void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions) {
        y += getT() * scale;
        for (textComponent component : textComponents) {
            y -= component.getT() * scale;
            component.draw(Img, x, y, scale, blockThreadedActions);
            y += component.getB() * scale;
        }
    }

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        clearTextComponents();
        var o = componentsFromString.componentsFromString(str, indexOfFirstChar, parent);
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
        height = Double.NaN;
    }

    @Override
    public double getW() {
        if (Double.isNaN(width)) {
            width = 0;
            for (textComponent tc : textComponents) {
                var W = tc.getW();
                if (W > width) {
                    width = W;
                }
            }
        }
        return width;
    }
    @Override
    public double getT() {
        return getHeight() / -2;
    }
    @Override
    public double getB() {
        return getHeight() / 2;
    }
    public double getHeight() {
        if (Double.isNaN(height)) {
            height = 0;
            for (var c : textComponents) {
                var H = c.getB() - c.getT();
                height += H;
            }
        }
        return height;
    }
}

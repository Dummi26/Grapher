package com.mark.graph.part.text.plus.components.math;

import com.mark.graph.part.text.plus.components.container.line;
import com.mark.graph.part.text.plus.componentsFromString;
import com.mark.graph.part.text.plus.gp;
import com.mark.graph.part.text.plus.textComponent;

import java.awt.*;

public class fraction extends textComponent {
    public fraction(gp parent) { super(parent); }
    @Override
    public specialCase isSpecial() {
        return null;
    }

    private textComponent numerator = null;
    private textComponent denominator = null;
    @Override
    public double getW() {
        return getMinimumNecessaryW() * 1.2;
    }
    private double getMinimumNecessaryW() {
        return Math.max(numerator.getW(), denominator.getW());
    }

    @Override
    public double getT() {
        return -numerator.getH();
    }

    @Override
    public double getB() {
        return denominator.getH();
    }

    @Override
    public void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions) {
        double actualWidth = scale * getW(); // the width of the center line that separates the numerator from the denominator
        double contentWidth = scale * getMinimumNecessaryW(); // the width of the content
        double additionalSpace = actualWidth - contentWidth; // in pixels, how much extra space there is on the left and right combined
        Img.setColor(Color.WHITE);
        Img.drawLine((int)Math.round(x), (int)Math.round(y), (int)Math.round(x + actualWidth), (int)Math.round(y));
        x += additionalSpace / 2;
        numerator.draw(Img, x + (contentWidth - numerator.getW() * scale) / 2, y + getT() * scale / 2, scale, blockThreadedActions);
        denominator.draw(Img, x + (contentWidth - denominator.getW() * scale) / 2, y + getB() * scale / 2, scale, blockThreadedActions);
    }

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        var o = componentsFromString.componentFromString(str, indexOfFirstChar, parent);
        if (o == null) return -1;
        numerator = o.comp;
        o = componentsFromString.componentFromString(str, o.nextFirstChar, parent);
        if (o == null) return -1;
        denominator = o.comp;
        return o.nextFirstChar;
    }
}

package com.mark.graph.part.text.plus.components.container;

import com.mark.graph.part.text.plus.componentsFromString;
import com.mark.graph.part.text.plus.textComponent;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.*;

public class size extends textComponent {
    public textComponent cmp;
    public double scale_factor = 1.0;
    @Override
    public specialCase isSpecial() { return null; }

    @Override
    public double getW() {
        return cmp.getW() * scale_factor;
    }

    @Override
    public double getT() {
        return cmp.getT() * scale_factor;
    }

    @Override
    public double getB() {
        return cmp.getB() * scale_factor;
    }

    @Override
    public void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions) {
        cmp.draw(Img, x, y, scale * scale_factor, blockThreadedActions);
    }

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        int indexOfStar = str.indexOf('*', indexOfFirstChar);
        if (indexOfStar > 0) {
            double factor = -1;
            try {
                factor = Double.parseDouble(str.substring(indexOfFirstChar, indexOfStar));
            } catch (NumberFormatException e) {}
            if (factor <= 0) {
                InformationWindowDisplayer.display(Information.GetDefault(
                        "Size (" + factor + ") was out of range (<=0)!",
                        Information.DefaultType.Information_Short
                ));
                return -1;
            }
            var o = componentsFromString.componentFromString(str, indexOfStar+1);
            if (o == null) return -1;
            scale_factor = factor;
            cmp = o.comp;
            return o.nextFirstChar;
        } else {
            return -1;
        }
    }
}

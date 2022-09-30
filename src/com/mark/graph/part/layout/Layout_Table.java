package com.mark.graph.part.layout;

import com.mark.graph.graphPart;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

public class Layout_Table extends Layout {
    public Layout_Table(graphPart panel) {
        super(panel);
    }

    private int width = 1;
    private int height = 0;
    @Override
    public void performLayout() {
        var gps = panel.contents;
        int wid = width;
        int hei = height;
        if (wid <= 0 && hei <= 0) {
            int v = (int)Math.ceil(Math.sqrt(gps.length));
            if (v * v < gps.length) { v += 1; } // in case the sqrt was something like 3.00000000000001 and rounded to 3.0, causing Math.ceil to not actually do what it should do.
            wid = v;
            hei = v;
        } else if (wid <= 0) {
            wid = gps.length / hei;
            if (hei * wid < gps.length) { wid += 1; } // because integer division rounds down
        } else if (hei <= 0) {
            hei = gps.length / wid;
            if (hei * wid < gps.length) { hei += 1; } // because integer division rounds down
        }
        int index = 0;
        double W = 100.0 / wid;
        double H = 100.0 / hei;
        for (int y = 0; y < hei; y++) {
            double Y = 100.0 * y / hei;
            for (int x = 0; x < wid; x++) {
                double X = 100.0 * x / wid;
                if (index < gps.length) {
                    var gp = gps[index];
                    gp.X(X);
                    gp.Y(Y);
                    gp.W(W);
                    gp.H(H);
                    index++;
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public String getAdjustString() {
        return width + "x" + height;
    }
    @Override
    public String readAdjustString(String input) {
        int iox = input.indexOf('x');
        if (iox > 0) {
            try {
                width = Integer.parseInt(input.substring(0, iox));
                height = Integer.parseInt(input.substring(iox + 1));
            } catch (NumberFormatException ex) {
                return "The input '" + input + "' could not be parsed as width or height was not a number.\nTo automatically adjust one of the values, set it to 0.";
            }
        } else {
            return "The input '" + input + "' could not be parsed as [width]x[height], as no 'x' was found.";
        }
        return null;
    }
}

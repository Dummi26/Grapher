package com.mark.graph.part.shape.ellipse;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.input.CustomInputInfoContainer;

import java.awt.*;

public class gp extends graphPart {
    public boolean circular = false;
    public Color outlineColor = new Color(255, 255, 255);
    public Color fillColor = new Color(255, 255, 255, 0);

    public gp(Graph parent, graphPart container) {
        super(parent, container, gpIdentifiers.Ellipse);
    }

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "OutlineColor" -> {
                var nc = com.mark.useful.parsing.color.parse_string(value);
                if (nc != null) { outlineColor = nc; }
            }
            case "FillColor" -> {
                var nc = com.mark.useful.parsing.color.parse_string(value);
                if (nc != null) { fillColor = nc; }
            }
            case "Circular" -> {
                circular = value.length() > 0;
            }
        }
    }

    @Override
    public String[] customFileSave() {
        return new String[] {
                "OutlineColor:" + com.mark.useful.parsing.color.to_string(outlineColor),
                "FillColor:" + com.mark.useful.parsing.color.to_string(fillColor),
                "Circular:" + (circular ? "y" : ""),
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        if (circular) {
            if (w > h) {
                x = x + (w-h) / 2;
                w = h;
            } else if (h > w) {
                y = y + (h-w) / 2;
                h = w;
            } else {
                Img.drawOval(x, y, w, h);
            }
        }
        Img.setColor(fillColor);
        Img.fillOval(x, y, w, h);
        Img.setColor(outlineColor);
        Img.drawOval(x, y, w, h);
    }

    @Override
    protected String customToString() {
        return "";
    }

    @Override
    protected void wasRemoved() {

    }

    @Override
    public CustomInputInfoContainer customUserInput() {
        return null;
    }
}

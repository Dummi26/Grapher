package com.mark.graph.part.shape.ellipse;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.input.CustomInputInfoContainer;

import java.awt.*;

public class gp extends graphPart {
    public boolean circular = false;
    public Color color = new Color(255, 255, 255);

    public gp(Graph parent, graphPart container) {
        super(parent, container, gpIdentifiers.Ellipse);
    }

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "Color" -> {
                var nc = com.mark.useful.parsing.color.parse_string(value);
                if (nc != null) { color = nc; }
            }
            case "Circular" -> {
                circular = value.length() > 0;
            }
        }
    }

    @Override
    public String[] customFileSave() {
        return new String[] {
                "Color:" + com.mark.useful.parsing.color.to_string(color),
                "Circular:" + (circular ? "y" : ""),
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        if (circular) {
            if (w > h) {
                Img.drawOval(x + (w-h) / 2, y, h, h);
            } else if (h > w) {
                Img.drawOval(x, y + (h-w) / 2, w, w);
            } else {
                Img.drawOval(x, y, w, h);
            }
        } else{
            Img.drawOval(x, y, w, h);
        }
    }

    @Override
    protected String customToString() {
        return "Line";
    }

    @Override
    protected void wasRemoved() {

    }

    @Override
    public CustomInputInfoContainer customUserInput() {
        return null;
    }
}
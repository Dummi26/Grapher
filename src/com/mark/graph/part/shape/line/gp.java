package com.mark.graph.part.shape.line;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.input.CustomInputInfoContainer;

import java.awt.*;

public class gp extends graphPart {
    public boolean up = false;
    public Color color = new Color(255, 255, 255);

    public gp(Graph parent, graphPart container) {
        super(parent, container, gpIdentifiers.Line);
    }

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "Color" -> {
                var nc = com.mark.useful.parsing.color.parse_string(value);
                if (nc != null) { color = nc; }
            }
            case "Up" -> {
                up = value.length() > 0;
            }
        }
    }

    @Override
    public String[] customFileSave() {
        return new String[] {
                "Color:" + com.mark.useful.parsing.color.to_string(color),
                "Up:" + (up ? "y" : ""),
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        Img.setColor(color);
        if (up) {
            Img.drawLine(x, y+h, x+w, y);
        } else {
            Img.drawLine(x, y, x+w, y+h);
        }
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

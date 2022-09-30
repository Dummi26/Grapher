package com.mark.graph.part.panel;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.graph.graphPartDrawInfo;
import com.mark.input.CustomInputInfoContainer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.Panel); }

    Color Color = new Color(255, 255, 255, 0);
    Color OutlineColor = new Color(255, 255, 255, 255);

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "Color" -> {
                String[] split = value.split(",");
                switch (split.length) {
                    case 1 -> {try {
                        Color = new Color(255, 255, 255, Integer.parseInt(split[0]));} catch(Exception e) {}}
                    case 2 -> {try {
                        int v = Integer.parseInt(split[0]); OutlineColor = new Color(v, v, v, Integer.parseInt(split[1]));} catch(Exception e) {}}
                    case 3 -> {try {
                        Color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 255);} catch(Exception e) {}}
                    case 4 -> {try {
                        Color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));} catch(Exception e) {}}
                }
            }
            case "OutlineColor" -> {
                String[] split = value.split(",");
                switch (split.length) {
                    case 1 -> {try {
                        OutlineColor = new Color(255, 255, 255, Integer.parseInt(split[0]));} catch(Exception e) {}}
                    case 2 -> {try {
                        int v = Integer.parseInt(split[0]); OutlineColor = new Color(v, v, v, Integer.parseInt(split[1]));} catch(Exception e) {}}
                    case 3 -> {try {
                        OutlineColor = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 255);} catch(Exception e) {}}
                    case 4 -> {try {
                        OutlineColor = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));} catch(Exception e) {}}
                }
            }
        }
    }
    @Override
    public String[] customFileSave() {
        return new String[] {
                "Color:" + Color.getRed() + "," + Color.getGreen() + "," + Color.getBlue() + "," + Color.getAlpha(),
                "OutlineColor:" + OutlineColor.getRed() + "," + OutlineColor.getGreen() + "," + OutlineColor.getBlue() + "," + OutlineColor.getAlpha()
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions, graphPartDrawInfo info) {
        if (Color.getAlpha() > 0) {
            Img.setColor(Color);
            Img.fillRect(x, y, w, h);
        }
    }

    @Override
    protected void customDrawAfter(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        if (OutlineColor.getAlpha() > 0) {
            Img.setColor(OutlineColor);
            Img.drawRect(x-1, y-1, w+1, h+1);
        }
    }

    @Override protected String customToString() {
        Rectangle2D a = getArea();
        String what = null;
        if (Color.getAlpha() > 0) {
            if (OutlineColor.getAlpha() > 0) { what = "OutFill";
            } else { what = "Fill"; }
        } else {
            if (OutlineColor.getAlpha() > 0) { what = "Outline";
            } else { what = "Transparent"; }
        }
        return (what == null ? "" : what + " | ") + a.getWidth() + "x" + a.getHeight();
    }

    @Override
    protected void wasRemoved() {
    }
    @Override public CustomInputInfoContainer customUserInput() { return null; }
}

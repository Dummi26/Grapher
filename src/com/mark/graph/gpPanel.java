package com.mark.graph;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class gpPanel extends graphPart {
    gpPanel(graph parent, graphPart container) { super(parent, container, gpIdentifiers.Panel); }

    Color Color = new Color(255, 255, 255, 0);
    Color OutlineColor = new Color(255, 255, 255, 255);

    @Override
    public void customFileLoad(String identifier, String value) {
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
    void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
        if (Color.getAlpha() > 0) {
            Img.setColor(Color);
            Img.fillRect(x, y, w, h);
        }
        if (OutlineColor.getAlpha() > 0) {
            Img.setColor(OutlineColor);
            Img.drawRect(x, y, w-1, h-1);
        }
    }

    @Override public String toString() {
        Rectangle2D a = getArea();
        String what = null;
        if (Color.getAlpha() > 0) {
            if (OutlineColor.getAlpha() > 0) { what = "OutFill";
            } else { what = "Fill"; }
        } else {
            if (OutlineColor.getAlpha() > 0) { what = "Outline";
            } else { what = "Transparent"; }
        }
        return "Panel" + (what == null ? " " : ", " + what) + " (" + a.getWidth() + "x" + a.getHeight() + ")";
    }
}
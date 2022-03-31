package com.mark;

import java.awt.*;

public class gpPanel extends graphPart {
    gpPanel(graph parent, graphPart container) { super(parent, container, gpIdentifiers.Panel); }

    Color color = new Color(255, 255, 255, 50);

    @Override
    protected void customFileLoad(String identifier, String value) {
        switch (identifier) {
            case "Color" -> {
                String[] split = value.split(",");
                switch (split.length) {
                    case 1 -> {try {color = new Color(255, 255, 255, Integer.parseInt(split[0]));} catch(Exception e) {}}
                    case 3 -> {try {color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 255);} catch(Exception e) {}}
                    case 4 -> {try {color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));} catch(Exception e) {}}
                }
            }
        }
    }
    @Override
    protected String[] customFileSave() {
        return new String[] {
                "Color:" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha()
        };
    }

    @Override
    void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
        Img.setColor(color);
        Img.fillRect(x, y, w, h);
    }

    @Override public String toString() {
        return "Panel, Color: " + "R"+color.getRed() + "G"+color.getGreen() + "B"+color.getBlue() + "A"+color.getAlpha();
    }
}
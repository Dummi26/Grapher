package com.mark.useful.parsing;

import java.awt.*;

public final class color {
    public static Color parse_string(String s) {
        String[] split = s.split(",");
        switch (split.length) {
            case 1 -> {try {return new Color(255, 255, 255, Integer.parseInt(split[0]));} catch(Exception e) {}}
            case 3 -> {try {return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 255);} catch(Exception e) {}}
            case 4 -> {try {return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));} catch(Exception e) {}}
        }
        return null;
    }

    public static String to_string(Color color) { return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha(); }
}

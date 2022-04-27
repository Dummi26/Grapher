package com.mark;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public final class InformationWindowDisplayer {
    public static void display(Information info) {
        CurrentInfos.add(info);
    }

    public static boolean HasToDraw() {
        return CurrentInfos.size() > 0;
    }

    public static void draw(Graphics2D g2d, int w, int h) {
        LocalDateTime now = LocalDateTime.now();
        int x = w;
        int y = h;
        int PixelBufferDistance = 20;
        x -= PixelBufferDistance;
        y -= PixelBufferDistance;
        for (int i = 0; i < CurrentInfos.size(); i++) {
            Information info = CurrentInfos.get(i);
            Rectangle2D pos = info.draw(g2d, 0, 0, x, y, w, h, Information.Alignment.BottomRight, now);
            if (pos == null) {
                CurrentInfos.remove(i--);
            }
            else {
                y = (int) (y - pos.getHeight() - PixelBufferDistance);
            }
        }
    }
    public static ArrayList<Information> CurrentInfos = new ArrayList<>();
}


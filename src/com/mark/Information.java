package com.mark;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Information {

    public static Information GetDefault(String information, DefaultType type) {
        switch (type) {
            case Saved -> {
                return new Information(
                        information,
                        Duration.ofSeconds(2),
                        new Color(255, 255, 255, 200),
                        new Color(0, 0, 0, 200),
                        new Color(255, 255, 255, 200),
                        new Animation(0.25,null, Animation.Fly.Down),
                        Duration.ofMillis(250),
                        new Animation(1,Animation.Fade.Yes,null),
                        Duration.ofMillis(250)
                );
            }
        }
        return GetDefault(information, DefaultType.Saved); // default type
    }
    enum DefaultType {
        Saved,
    }

    public String Information;
    public LocalDateTime ExpiresTime;
    public Color ColorOutline;
    public Color ColorBG;
    public Color ColorText;
    public Animation AnimIn;
    public Duration AnimInDur;
    public Animation AnimOut;
    public Duration AnimOutDur;

    private LocalDateTime AnimInCompletedTime;
    private LocalDateTime AnimOutStartTime;

    public Information(String information, Duration duration, Color colorOutline, Color colorBG, Color colorText, Animation AnimIn, Duration AnimInDur, Animation AnimOut, Duration AnimOutDur) {
        LocalDateTime now = LocalDateTime.now();
        Information = information;
        ExpiresTime = now.plus(duration);
        ColorOutline = colorOutline;
        ColorBG = colorBG;
        ColorText = colorText;
        this.AnimIn = AnimIn;
        this.AnimInDur = AnimInDur;
        this.AnimOut = AnimOut;
        this.AnimOutDur = AnimOutDur;
        AnimInCompletedTime = now.plus(AnimInDur);
        AnimOutStartTime = ExpiresTime.minus(AnimOutDur);
    }

    enum Alignment {
        TopLeft,
        TopCenter,
        TopRight,
        MiddleLeft,
        MiddleCenter,
        MiddleRight,
        BottomLeft,
        BottomCenter,
        BottomRight,
    }
    public static class Animation {
        public Animation(double CurveExponent, Fade fade, Fly fly) { this.CurveExponent = CurveExponent; this.fade = fade; this.fly = fly; }

        public Fade fade = null;
        public Fly fly = null;

        public double CurveExponent;

        public double ApplyCurve(double Linear) {
            return Math.pow(Linear, CurveExponent);
        }

        enum Fly {
            Right,
            Left,
            Up,
            Down
        }
        enum Fade {
            Yes,
        }
    }

    public Rectangle2D draw(Graphics2D g, int x1, int y1, int x2, int y2, int Width, int Height, Alignment alignment, LocalDateTime now) {
        if (ExpiresTime.isBefore(now)) {
            return null;
        }
        String[] lines = Information.split("\n");
        int FontWidth = 0;
        for (String line : lines) {
            int LineWidth = (int) Math.ceil(g.getFontMetrics().getStringBounds(line, g).getWidth());
            if (LineWidth > FontWidth) {
                FontWidth = LineWidth;
            }
        }
        int FontLineHeight = g.getFontMetrics().getMaxAscent();
        int Margin = 5;
        int w = FontWidth + 2 * Margin;
        int h = lines.length * FontLineHeight + 2 * Margin;
        int x, y;
        {
            Point Position = GetPos(x1, y1, x2, y2, alignment, w, h);
            x = Position.x;
            y = Position.y;
        }
        Color colorOutline = ColorOutline;
        Color colorBG = ColorBG;
        Color colorText = ColorText;
        // Modify things (animation)
        {
            Animation anim = null;
            double FactorOut = 0; // increase this = out
            double FactorIn = 1; // increase this = in
            if (AnimOutStartTime.isBefore(now)) {
                // Should fade out
                anim = AnimOut;
                FactorOut = (double) AnimOutStartTime.until(now, ChronoUnit.MILLIS) / AnimOutDur.toMillis();
                FactorOut = anim.ApplyCurve(FactorOut); // FactorOut increases
                FactorIn = 1 - FactorOut;
            } else if (AnimInCompletedTime.isAfter(now)) {
                // Should fade in
                anim = AnimIn;
                FactorOut = (double) now.until(AnimInCompletedTime, ChronoUnit.MILLIS) / AnimInDur.toMillis();
                FactorIn = anim.ApplyCurve(1 - FactorOut); // 1 - FactorOut increases
                FactorOut = 1 - FactorIn;
            }
            if (anim != null) {
                if (anim.fly != null) {
                    switch (anim.fly) {
                        case Left -> x = (int) Math.round(x * FactorIn + (-w) * FactorOut); // -w -> x
                        case Right -> x = (int) Math.round(x * FactorIn + Width * FactorOut); // Width -> x
                        case Up -> y = (int) Math.round(y * FactorIn + (-h) * FactorOut); // -h -> h
                        case Down -> y = (int) Math.round(y * FactorIn + Height * FactorOut); // Height -> y
                    }
                }
                if (anim.fade != null) {
                    switch (anim.fade) {
                        case Yes -> {
                            colorOutline = new Color(colorOutline.getRed(), colorOutline.getGreen(), colorOutline.getBlue(), (int) Math.round(colorOutline.getAlpha() * FactorIn));
                            colorBG = new Color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), (int) Math.round(colorBG.getAlpha() * FactorIn));
                            colorText = new Color(colorText.getRed(), colorText.getGreen(), colorText.getBlue(), (int) Math.round(colorText.getAlpha() * FactorIn));
                        }
                    }
                }
            }
        }
        // Draw
        g.setPaint(colorOutline);
        g.drawRect(x, y, w, h);
        g.setPaint(colorBG);
        g.fillRect(x, y, w, h);
        g.setPaint(colorText);
        for (int i = 0; i < lines.length; ) {
            g.drawString(lines[i], x + Margin, y + Margin + (++i * FontLineHeight));
        }
        return new Rectangle(x, y, w, h);
    }

    private Point GetPos(int x1, int y1, int x2, int y2, Alignment alignment, int w, int h) {
        int x = -1;
        int y = -1;
        switch (alignment) {
            case TopLeft, TopCenter, TopRight -> y = y1;
            case MiddleLeft, MiddleCenter, MiddleRight -> y = (y1 + y2 - h) / 2;
            case BottomLeft, BottomCenter, BottomRight -> y = y2 - h;
        }
        switch (alignment) {
            case TopLeft, MiddleLeft, BottomLeft -> x = x1;
            case TopCenter, MiddleCenter, BottomCenter -> x = (x1 + x2 - w) / 2;
            case TopRight, MiddleRight, BottomRight -> x = x2 - w;
        }
        return new Point(x, y);
    }
}
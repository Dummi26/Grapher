package com.mark.notification;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

public class Information {

    public static Information GetDefault(String information, DefaultType type) {
        return switch (type) {
            case Information_Short -> new Information(
                        information,
                        Duration.ofSeconds(2),
                        new Color(255, 255, 255, 127),
                        new Color(0, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
            case Information_Long -> new Information(
                        information,
                        Duration.ofSeconds(4),
                        new Color(255, 255, 255, 127),
                        new Color(0, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
            case Error_Minor -> new Information(
                        information,
                        Duration.ofSeconds(3),
                        new Color(255, 255, 255, 127),
                        new Color(100, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
            case Error_Medium -> new Information(
                        information,
                        Duration.ofSeconds(5),
                        new Color(255, 255, 255, 127),
                        new Color(150, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
            case Error_Major -> new Information(
                        information,
                        Duration.ofSeconds(7),
                        new Color(255, 255, 255, 127),
                        new Color(200, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
            case Error_Fatal -> new Information(
                        information,
                        Duration.ofSeconds(10),
                        new Color(255, 255, 255, 127),
                        new Color(255, 0, 0, 127),
                        new Color(255, 255, 255),
                        GetDefaultAnimationIn(type),
                        Duration.ofMillis(250),
                        GetDefaultAnimationOut(type),
                        Duration.ofMillis(250)
            );
        };
    }
    private static final Animation GetDefaultAnimationIn(DefaultType type) {
        switch (type) {
            case Information_Short, Information_Long, Error_Minor, Error_Medium, Error_Major, Error_Fatal -> {
                return new Animation(new Curve(
                        new MathOperation(MathOperation.Operation.Pow,
                                new MathVariable(),
                                new MathNumber(0.25))
                ),null, Animation.Fly.Down);
            }
        }
        return null;
    }
    private static final Animation GetDefaultAnimationOut(DefaultType type) {
        switch (type) {
            case Information_Short, Information_Long, Error_Minor, Error_Medium, Error_Major, Error_Fatal -> {
                return new Animation(new Curve(
                        new MathVariable()
                ), Animation.Fade.Yes,null);
            }
        }
        return null;
    }
    public enum DefaultType {
        Information_Short, // 2 seconds, i.e. saved
        Information_Long, // 4 seconds, i.e. loaded (because not directly due to user input)
        Error_Minor, // should fix itself quite quickly. can also require user interaction if the user caused the error.
        Error_Medium, // might require reload or user interaction, but does not yet cause issues (apart from the graph not showing what the user wants)
        Error_Major, // requires reload or the user must fix something
        Error_Fatal, // requires restart
    }

    public String Information;
    public LocalDateTime BeginTime;
    public LocalDateTime ExpiresTime;
    public Color ColorOutline;
    public Color ColorBG;
    public Color ColorText;
    public Duration duration;
    public Animation AnimIn;
    public Duration AnimInDur;
    public Animation AnimOut;
    public Duration AnimOutDur;

    public LocalDateTime AnimInCompletedTime;
    public LocalDateTime AnimOutStartTime;

    public int LastX = 0;
    public int LastY = 0;
    public int LastW = 0;
    public int LastH = 0;

    /**Creates a new Information object, which will be displayed on screen. It is recommended to use Information.GetDefault() for consistency (or, for custom information types, to use Information.GetDefaultAnimationIn() and Information.GetDefaultAnimationOut() for the animations).
     * @param information The text to be displayed.
     * @param duration How long the text should be displayed. This includes the time the animations take to show or hide the information. If this is null, it will be displayed indefinitely. Initialize the out animation using ResetTime_Out().
     * @param colorOutline The color of the information box's outline
     * @param colorBG The background color of the information box
     * @param colorText The text color
     * @param AnimIn The animation to be used for showing this info.
     * @param AnimInDur How long said animation should take.
     * @param AnimOut The animation to be used for hiding this info.
     * @param AnimOutDur How long said animation should take.
     */
    public Information(String information, Duration duration, Color colorOutline, Color colorBG, Color colorText, Animation AnimIn, Duration AnimInDur, Animation AnimOut, Duration AnimOutDur) {
        LocalDateTime now = LocalDateTime.now();
        Information = information;
        ColorOutline = colorOutline;
        ColorBG = colorBG;
        ColorText = colorText;
        this.duration = duration;
        this.AnimIn = AnimIn;
        this.AnimInDur = AnimInDur;
        this.AnimOut = AnimOut;
        this.AnimOutDur = AnimOutDur;
        ResetTime(now);
    }

    public void ResetTime() {
        ResetTime(LocalDateTime.now());
    }
    public void ResetTime(LocalDateTime t) {
        BeginTime = t;
        ResetTime_Dur();
    }
    public void ResetTimeAnim() {
        AnimInCompletedTime = BeginTime.plus(AnimInDur);
        AnimOutStartTime = ExpiresTime == null ? null : ExpiresTime.minus(AnimOutDur);
    }
    public void ResetTime_End() {
        ResetTime_End(LocalDateTime.now());
    }
    public void ResetTime_End(LocalDateTime t) {
        ExpiresTime = t;
        duration = Duration.between(BeginTime, ExpiresTime);
        ResetTimeAnim();
    }
    public void ResetTime_Out() {
        ResetTime_Out(LocalDateTime.now());
    }
    public void ResetTime_Out(LocalDateTime t) {
        ResetTime_End(t.plus(AnimOutDur));
    }
    /**Calculates ExpiresTime and the animation times. Call this after changing duration or BeginTime.*/
    public void ResetTime_Dur() {
        ExpiresTime = duration == null ? null : BeginTime.plus(duration);
        ResetTimeAnim();
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
        public Animation(Curve curve, Fade fade, Fly fly) { this.curve = curve; this.fade = fade; this.fly = fly; }

        public Fade fade = null;
        public Fly fly = null;

        public Curve curve;

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
    public static class Curve {
        public Curve(MathEquation Equation) {
            this.Equation = Equation;
            Min = ApplyCurveRaw(0);
            Max = ApplyCurveRaw(1);
            double Dif = Max - Min;
            if (Dif == 0) throw new NumberFormatException("Curve must not have a range of 0");
        }
        public MathEquation Equation;
        public double Min;
        public double Max;

        private double ApplyOffsetAndFactor(double value) { return (value - Min) / (Max - Min); }

        /**
         * Applies the animation's curve to the linearly increasing number (Linear)
         * @param Linear A linearly increasing number that should be converted to a smooth curve
         * @return A (most likely) Non-Linear curve's number.
         */
        public double ApplyCurveInc(double Linear) {
            return ApplyOffsetAndFactor(ApplyCurveRaw(Linear));
        }
        private double ApplyCurveRaw(double Linear) {
            if (Linear < 0 || Linear > 1) throw new NumberFormatException("Linear number for using ApplyCurve must be 0<=x<=1!");
            MathVars mv = new MathVars();
            mv.x = Linear;
            return Equation.calc(mv);
        }
        /**
         * Applies the animation's curve to the linearly decreasing number (Linear)
         * @param Linear A linearly decreasing number that should be converted to a smooth curve
         * @return A (most likely) Non-Linear curve's number.
         */
        public double ApplyCurveDec(double Linear) {
            return 1 - ApplyCurveInc(1 - Linear);
        }
    }
    public static abstract class MathEquation {
        public abstract double calc(MathVars v);
    }
    public static class MathVars {
        double x;
    }
    public static class MathOperation extends MathEquation {
        public MathOperation(Operation operation, MathEquation left, MathEquation right) {
            this.operation = operation;
            this.left = left;
            this.right = right;
        }
        public Operation operation;
        public MathEquation left;
        public MathEquation right;
        public enum Operation {
            Add,
            Subtract,
            Multiply,
            Divide,
            Pow,
        }
        public double calc(MathVars v) {
            switch (operation) {
                case Add -> { return left.calc(v) + right.calc(v); }
                case Subtract -> { return left.calc(v) - right.calc(v); }
                case Multiply -> { return left.calc(v) * right.calc(v); }
                case Divide -> { return left.calc(v) / right.calc(v); }
                case Pow -> { return Math.pow(left.calc(v), right.calc(v)); }
            }
            return Double.NaN;
        }
    }
    public static class MathNumber extends MathEquation {
        public MathNumber(double numberValue) {
            this.num = numberValue;
        }
        public double num;
        public double calc(MathVars v) {
            return num;
        }
    }
    public static class MathVariable extends MathEquation {
        public MathVariable() {
        }
        public double calc(MathVars v) {
            return v.x;
        }
    }

    /**
     *
     * @return The area of the notification. If x is NaN, one of the following 3 conditions were true and the notification was not drawn: y: The notification should only appear later; w: The notification has expired; h: ?
     */
    public Rectangle2D draw(Graphics2D g, int x1, int y1, int x2, int y2, int Width, int Height, Alignment alignment, LocalDateTime now) {
        if (BeginTime.isAfter(now)) {
            return new Rectangle2D.Double(Double.NaN, Double.NaN, 0, 0);
        }
        if (ExpiresTime != null && ExpiresTime.isBefore(now)) {
            return new Rectangle2D.Double(Double.NaN, 0,  Double.NaN, 0);
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
            if (AnimOutStartTime != null && AnimOutStartTime.isBefore(now)) {
                // Should fade out
                anim = AnimOut;
                FactorOut = (double) AnimOutStartTime.until(now, ChronoUnit.MILLIS) / AnimOutDur.toMillis();
                FactorOut = anim.curve.ApplyCurveInc(FactorOut); // FactorOut increases
                FactorIn = 1 - FactorOut;
            } else if (AnimInCompletedTime.isAfter(now)) {
                // Should fade in
                anim = AnimIn;
                FactorOut = (double) now.until(AnimInCompletedTime, ChronoUnit.MILLIS) / AnimInDur.toMillis();
                FactorIn = anim.curve.ApplyCurveInc(1 - FactorOut); // 1 - FactorOut increases
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
        g.setPaint(colorBG);
        g.fillRect(x, y, w, h);
        g.setPaint(colorOutline);
        g.drawRect(x, y, w, h);
        g.setPaint(colorText);
        for (int i = 0; i < lines.length; ) {
            g.drawString(lines[i], x + Margin, y + Margin + (++i * FontLineHeight));
        }
        LastX = x;
        LastY = y;
        LastW = w;
        LastH = h;
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

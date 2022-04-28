package com.mark.notification;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        { // notification is gone, move other notifications down smoothly.
            Duration animTime = Duration.ofMillis(500);
            //
            double TotalHeightOffsetRequired = 0;
            for (int i = 0; i < RemovedInfos.size(); i++) {
                Information info = RemovedInfos.get(i);
                double LinearFactor = (double)info.ExpiresTime.until(now, ChronoUnit.NANOS) / animTime.getNano();
                if (LinearFactor > 1) {
                    RemovedInfos.remove(i--);
                }
                else {
                    double CountdownFactor = 1 - new Information.Curve( // -2x^3 + 3x^2
                            new Information.MathOperation(Information.MathOperation.Operation.Add,
                                    new Information.MathOperation(Information.MathOperation.Operation.Multiply,
                                            new Information.MathNumber(-2),
                                            new Information.MathOperation(Information.MathOperation.Operation.Pow,
                                                    new Information.MathVariable(),
                                                    new Information.MathNumber(3)
                                            )
                                    ),
                                    new Information.MathOperation(Information.MathOperation.Operation.Multiply,
                                            new Information.MathNumber(3),
                                            new Information.MathOperation(Information.MathOperation.Operation.Pow,
                                                    new Information.MathVariable(),
                                                    new Information.MathNumber(2)
                                            )
                                    )
                            )
                    ).ApplyCurveInc(LinearFactor);
                    TotalHeightOffsetRequired += (info.LastH + PixelBufferDistance) * CountdownFactor;
                }
            }
            y -= (int)TotalHeightOffsetRequired;
        }
        for (int i = 0; i < CurrentInfos.size(); i++) {
            Information info = CurrentInfos.get(i);
            Rectangle2D pos = info.draw(g2d, 0, 0, x, y, w, h, Information.Alignment.BottomRight, now);
            double height;
            if (pos == null) {
                RemoveInfo(i--);
                height = info.LastH;
            }
            else {
                height = pos.getHeight();
            }
            y = (int) (y - height - PixelBufferDistance);
        }
    }

    public static void RemoveInfo(Information info) {
        if (CurrentInfos.remove(info)) {
            RemovedInfos.add(info);
        }
    }
    public static void RemoveInfo(int index) {
        Information info = CurrentInfos.get(index);
        CurrentInfos.remove(index);
        RemovedInfos.add(info);
    }
    public static ArrayList<Information> CurrentInfos = new ArrayList<>();
    public static ArrayList<Information> RemovedInfos = new ArrayList<>();
}


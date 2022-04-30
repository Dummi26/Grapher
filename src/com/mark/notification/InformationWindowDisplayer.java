package com.mark.notification;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public final class InformationWindowDisplayer {
    private static int MaxCurrentInfos = 4;
    public static void display(Information info) {
        if (CurrentInfos.size() < MaxCurrentInfos) {
            CurrentInfos.add(info);
        }
        else {
            if (QueuedInfos.size() == 0) { QueuedInfosLastAddedOrRemovedFirstElement = LocalDateTime.now(); }
            QueuedInfos.add(info);
        }
    }

    public static boolean HasToDraw() {
        return CurrentInfos.size() > 0;
    }

    public static void draw(Graphics2D g2d, int w, int h) {
        Information.Curve MoveUpDownCurve; {
            MoveUpDownCurve = new Information.Curve( // -2x^3 + 3x^2 :: f'(0) = f'(1) = 0, f(0) = 0, f(0.5) = 0.5, f(1) = 1
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
            );
        }
        LocalDateTime now = LocalDateTime.now();
        int x = w;
        int y = h;
        int PixelBufferDistance = 20;
        x -= PixelBufferDistance;
        y -= PixelBufferDistance;
        for (int i = 0; i <= CurrentInfos.size(); i++) {
            boolean IsQueuePlusN = i >= CurrentInfos.size();
            Information info;
            if (!IsQueuePlusN) {
                info = CurrentInfos.get(i);
            }
            else { // Queue +n notification icon thing
                info = new Information("+" + QueuedInfos.size(),
                        Duration.ofHours(1),
                        new Color(255, 255, 255, 75),
                        new Color(0, 0, 0, 100),
                        new Color(255, 255, 255, 150),
                        new Information.Animation(new Information.Curve(new Information.MathOperation(Information.MathOperation.Operation.Pow, new Information.MathVariable(), new Information.MathNumber(0.25))), Information.Animation.Fade.Yes, Information.Animation.Fly.Right),
                        Duration.ofMillis(500),
                        new Information.Animation(new Information.Curve(new Information.MathOperation(Information.MathOperation.Operation.Pow, new Information.MathVariable(), new Information.MathNumber(4))), Information.Animation.Fade.Yes, Information.Animation.Fly.Right),
                        Duration.ofMillis(1000)
                );
                if (QueuedInfos.size() > 0) {
                    info.ResetTime(QueuedInfosLastAddedOrRemovedFirstElement); // last added first element, go to start of fade in animation
                }
                else if (QueuedInfosLastAddedOrRemovedFirstElement != null) {
                    info.ResetTime(QueuedInfosLastAddedOrRemovedFirstElement.minus(info.duration).plus(info.AnimOutDur)); // last removed last element, go to start of fade out animation
                    if (info.ExpiresTime.isBefore(now)) {
                        QueuedInfosLastAddedOrRemovedFirstElement = null;
                    }
                }
            }
            Rectangle2D pos = info.draw(g2d, 0, 0, x, y, w, h, Information.Alignment.BottomRight, now);
            // calculations for smoothly moving notifications down if below notifications have expired (not necessary for the Queue +n notification if it is on top, if it is below then it is necessary)
            if (!IsQueuePlusN) {
                double height;
                if (Double.isNaN(pos.getWidth())) { // Expired
                    double LinearFactor = (double) info.ExpiresTime.until(now, ChronoUnit.NANOS) / Duration.ofMillis(500).getNano();
                    if (LinearFactor > 1) {
                        RemoveInfo(i--);
                    } else {
                        double CountdownFactor;
                        {
                            CountdownFactor = 1 - MoveUpDownCurve.ApplyCurveInc(LinearFactor);
                        }
                        y -= (info.LastH + PixelBufferDistance) * CountdownFactor;
                    }
                    height = info.LastH;
                } else {
                    height = pos.getHeight();
                }
                if (!Double.isNaN(pos.getX())) { // X=NaN indicates that the notification was not drawn.
                    double Progress = (double)info.BeginTime.until(now, ChronoUnit.NANOS) / info.AnimInDur.getNano();
                    if (Progress > 1) {
                        Progress = 1;
                    }
                    else {
                        Progress = MoveUpDownCurve.ApplyCurveInc(Progress);
                        System.out.println(Progress);
                    }
                    y = (int) (y - (height + PixelBufferDistance) * Progress);
                }
            }
        }
    }

    public static void RemoveInfo(Information info) {
        if (CurrentInfos.remove(info)) {
            AfterRemovingFromCurrentInfos();
        }
    }
    public static void RemoveInfo(int index) {
        CurrentInfos.remove(index);
        AfterRemovingFromCurrentInfos();
    }
    private static LocalDateTime QueuedInfosLastAddedOrRemovedFirstElement; // the last time where QueuedInfos.size() went from 0 to 1 or from 1 to 0.
    private static void AfterRemovingFromCurrentInfos() {
        LocalDateTime now = null;
        while (CurrentInfos.size() < MaxCurrentInfos && QueuedInfos.size() > 0) {
            if (now == null) now = LocalDateTime.now();
            Information next = QueuedInfos.remove(0);
            if (QueuedInfos.size() == 0) {
                QueuedInfosLastAddedOrRemovedFirstElement = now; // emptied the queue
            }
            next.ResetTime(now);
            CurrentInfos.add(next);
        }
    }
    public static ArrayList<Information> QueuedInfos = new ArrayList<>();
    public static ArrayList<Information> CurrentInfos = new ArrayList<>();
}


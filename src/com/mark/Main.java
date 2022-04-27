package com.mark;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/*

The goal of this project is to create a file format to store graphs.

When trying to visually map out all operating systems i have access to, i noticed that the only options i really have
are MS-Office (or terrible), so i want to try to make my own Graph project.

TODO:
    Put things into panels etc (how???)
        Moving within panels is [probably] bad cuz speed wrong

 */

public class Main {
    public static int DrawCount = 0;
    public static class Render {
        private static double Zoom = 1; // Zoom level
        private static double PosX = 0; // Position from -100 to 100
        private static double PosY = 0;

        public static double getZoom() { return Zoom; }
        public static double getPosX() { return PosX; }
        public static double getPosY() { return PosY; }
        public static void setZoom(double Zoom) { Render.Zoom = Zoom; updateScreen = true; }
        public static void setPosX(double PosX) { Render.PosX = PosX; updateScreen = true; }
        public static void setPosY(double PosY) { Render.PosY = PosY; updateScreen = true; }
        public static double calcRenderWidth(double w) { return w * Zoom; }
        public static double calcRenderHeight(double h) { return h * Zoom; }
        public static void focusOnRectangle(Rectangle2D rect, boolean fill) { focusOnRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), fill); }
        public static void focusOnRectangle(double x, double y, double w, double h, boolean fill) {
            if (w != 0 && h != 0) {
                PosX = 2 * x + w - 100;
                PosY = 2 * y + h - 100;
                Zoom = fill ? Math.max(100.0 / w, 100.0 / h) : Math.min(100.0 / w, 100.0 / h);
                updateScreen = true;
            }
        }
        // AbsoluteRenderPos : Position on the Graphics2D object that will be drawn on [ 0..Wg | 0..Hg ]
        // RelativeRenderPos : Position on the Graph object that is used as the source [ 0..100 | 0..100 ]
        // AbsoluteScreenPos : Position on the screen [ 0..Ws | 0..Hs ]
        // RelativeScreenPos : Position on the screen [ 0..100 | 0..100 ]
        public static double calcRelativeScreenPosFromAbsoluteScreenPosX(double x, double w) { return 100.0 * x / w; }
        public static double calcAbsoluteRenderPosOfScreenCenterX(int w) { return -calcAbsoluteRenderPosFromRelativeScreenPosX(0, w); }
        public static double calcRelativeRenderPosFromAbsoluteScreenPosX(double x, double w) { return 100 * calcAbsoluteRenderPosFromAbsoluteScreenPosX(x, w) / calcRenderWidth(w); }
        public static double calcAbsoluteRenderPosFromAbsoluteScreenPosX(double x, double w) { return calcAbsoluteRenderPosFromRelativeScreenPosX(calcRelativeScreenPosFromAbsoluteScreenPosX(x, w), w); }
        public static double calcAbsoluteRenderPosFromRelativeScreenPosX(double x, double w) { // x: 0..100
            double W = calcRenderHeight(w);
            double PosInPixelCoordinates = (PosX + 100) / 2 * W / 100; // 0..W
            return PosInPixelCoordinates - w / 2.0 + x * w / 100;
        }
        public static double calcRelativeScreenPosFromAbsoluteScreenPosY(double y, double h) { return 100.0 * y / h; }
        public static double calcAbsoluteRenderPosOfScreenCenterY(int h) { return -calcAbsoluteRenderPosFromRelativeScreenPosY(0, h); }
        public static double calcRelativeRenderPosFromAbsoluteScreenPosY(double y, double h) { return 100 * calcAbsoluteRenderPosFromAbsoluteScreenPosY(y, h) / calcRenderHeight(h); }
        public static double calcAbsoluteRenderPosFromAbsoluteScreenPosY(double y, double h) { return calcAbsoluteRenderPosFromRelativeScreenPosY(calcRelativeScreenPosFromAbsoluteScreenPosY(y, h), h); }
        public static double calcAbsoluteRenderPosFromRelativeScreenPosY(double y, double h) { // y: 0..100
            double H = calcRenderHeight(h);
            double PosInPixelCoordinates = (PosY + 100) / 2 * H / 100; // 0..H
            return PosInPixelCoordinates - h / 2.0 + y * h / 100;
        }
    }
    public static JFrame frame;
    private static JLabel label;
    public static graph graph = null;
    public static graphPart graphPartMovingOrResizing = null;
    public static boolean graphPartMovingIsResizing = false;
    //
    public static int MousePosX = 0;
    public static int MousePosY = 0;
    public static int TempMouseX = 0; // TEMP, DON'T USE THESE!
    public static int TempMouseY = 0;
    //
    public static boolean IgnoreMouseDrag = false;
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showDialog(null, "Load graph");
        File f = fileChooser.getSelectedFile();
        if (f == null) return;
        if (!f.exists()) {
            try {
                Files.write(f.toPath(), new byte[0]);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not create file " + f.getAbsolutePath());
                return;
            }
        }
        try {
            graph = graphLoader.fromFile(f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not load graph from file " + f.getAbsolutePath());
            return;
        }
        frame = new JFrame("Grapher");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        label = new JLabel(new ImageIcon(new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB)));
        frame.add(label);
        /* MOUSE AND KEYBOARD */ {
            frame.getContentPane().addMouseWheelListener(e -> {
                Render.setZoom(Math.max(Double.MIN_VALUE, Render.getZoom() * (1 - e.getPreciseWheelRotation() * 0.1))); // * zoom speed factor
            });
            frame.getContentPane().addMouseListener(new MouseListener_Custom_Main());
            frame.getContentPane().addMouseMotionListener(new MouseMotionAdapter_Custom_Main());
            frame.addKeyListener(new KeyboardHandler());
        }
        frame.pack();
        frame.setVisible(true);
        int TargetFPS = 30;
        int FramesNotRendered = 0;
        int FramesNotRenderedMax = TargetFPS; // 0 = force render all frames
        {
            BufferedImage Image_ = null;
            updateScreen = true;
            while (frame.isDisplayable()) {
                FramesNotRendered++;
                long nanosecondsEnd = System.nanoTime() + 1000000000 / TargetFPS;
                // RENDER FRAME
                {
                    int w = frame.getContentPane().getWidth();
                    int h = frame.getContentPane().getHeight();
                    if (updateScreen || pWidth != w || pHeight != h || graphPartMovingOrResizing != null || FramesNotRendered > FramesNotRenderedMax) {
                        //System.out.println("Rendering");
                        FramesNotRendered = 0;
                        updateScreen = false;
                        pWidth = w;
                        pHeight = h;
                        w = w <= 0 ? 1 : w;
                        h = h <= 0 ? 1 : h;
                        Image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                        Graphics2D Image = Image_.createGraphics();
                        // Rendering
                        DrawCount = 0;
                        int Rx = (int) Render.calcAbsoluteRenderPosOfScreenCenterX(w);
                        int Ry = (int) Render.calcAbsoluteRenderPosOfScreenCenterY(h);
                        int Rw = (int) Render.calcRenderWidth(w);
                        int Rh = (int) Render.calcRenderHeight(h);
                        long StartTime = System.nanoTime();
                        graph.draw(Image, Rx, Ry, Rw, Rh, w, h);
                        System.out.println(DrawCount + " graphParts drawn in " + (System.nanoTime() - StartTime) / 1000 + "µs");
                        //graph.draw(Image, Rx+Rw/4, Ry+Rh/4, Rw/2, Rh/2, w/2, h/2);
                    }
                    // Finalizing
                    if (Image_ != null) {
                        if (InformationWindowDisplayer.HasToDraw()) {
                            BufferedImage ImageCopy = new BufferedImage(Image_.getWidth(), Image_.getHeight(), Image_.getType());
                            Graphics2D ImageCopyG = ImageCopy.createGraphics();
                            ImageCopyG.drawImage(Image_, 0, 0, null);
                            InformationWindowDisplayer.draw(ImageCopyG, ImageCopy.getWidth(), ImageCopy.getHeight());
                            label.setIcon(new ImageIcon(ImageCopy));
                        }
                        else {
                            label.setIcon(new ImageIcon(Image_));
                        }
                    }
                    // END OF RENDER
                    //while (System.nanoTime() < nanosecondsEnd);
                    try {
                        TimeUnit.NANOSECONDS.sleep(nanosecondsEnd - System.nanoTime());
                    } catch (Exception e) {
                    }
                    //System.out.println("FPS: " + 1000 / ((System.nanoTime() - nanoseconds) / 1000000.0));
                }
            }
        }
    }
    public static boolean updateScreen = false;
    private static int pWidth = -1;
    private static int pHeight = -1;
}
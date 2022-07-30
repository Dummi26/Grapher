package com.mark;

import com.mark.graph.Graph;
import com.mark.graph.graphLoader;
import com.mark.graph.graphPart;
import com.mark.input.KeyboardHandler;
import com.mark.input.MouseListener_Custom_Main;
import com.mark.input.MouseMotionAdapter_Custom_Main;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public static RenderC Render = new RenderC();
    public static class RenderC {
        private double Zoom = 1; // Zoom level
        private double PosX = 0; // Position from -100 to 100
        private double PosY = 0;

        public double getZoom() { return Zoom; }
        public double getPosX() { return PosX; }
        public double getPosY() { return PosY; }
        public void setZoom(double Zoom) { Render.Zoom = Zoom; updateScreen = true; }
        public void setPosX(double PosX) { Render.PosX = PosX; updateScreen = true; }
        public void setPosY(double PosY) { Render.PosY = PosY; updateScreen = true; }
        public double calcRenderWidth(double w) { return w * Zoom; }
        public double calcRenderHeight(double h) { return h * Zoom; }
        public void focusOnRectangle(Rectangle2D rect, boolean fill) { focusOnRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), fill); }
        public void focusOnRectangle(double x, double y, double w, double h, boolean fill) {
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
        public double calcRelativeScreenPosFromAbsoluteScreenPosX(double x, double w) { return 100.0 * x / w; }
        public double calcAbsoluteRenderPosOfScreenCenterX(int w) { return -calcAbsoluteRenderPosFromRelativeScreenPosX(0, w); }
        public double calcRelativeRenderPosFromAbsoluteScreenPosX(double x, double w) { return 100 * calcAbsoluteRenderPosFromAbsoluteScreenPosX(x, w) / calcRenderWidth(w); }
        public double calcAbsoluteRenderPosFromAbsoluteScreenPosX(double x, double w) { return calcAbsoluteRenderPosFromRelativeScreenPosX(calcRelativeScreenPosFromAbsoluteScreenPosX(x, w), w); }
        public double calcAbsoluteRenderPosFromRelativeScreenPosX(double x, double w) { // x: 0..100
            double W = calcRenderHeight(w);
            double PosInPixelCoordinates = (PosX + 100) / 2 * W / 100; // 0..W
            return PosInPixelCoordinates - w / 2.0 + x * w / 100;
        }
        public double calcRelativeScreenPosFromAbsoluteScreenPosY(double y, double h) { return 100.0 * y / h; }
        public double calcAbsoluteRenderPosOfScreenCenterY(int h) { return -calcAbsoluteRenderPosFromRelativeScreenPosY(0, h); }
        public double calcRelativeRenderPosFromAbsoluteScreenPosY(double y, double h) { return 100 * calcAbsoluteRenderPosFromAbsoluteScreenPosY(y, h) / calcRenderHeight(h); }
        public double calcAbsoluteRenderPosFromAbsoluteScreenPosY(double y, double h) { return calcAbsoluteRenderPosFromRelativeScreenPosY(calcRelativeScreenPosFromAbsoluteScreenPosY(y, h), h); }
        public double calcAbsoluteRenderPosFromRelativeScreenPosY(double y, double h) { // y: 0..100
            double H = calcRenderHeight(h);
            double PosInPixelCoordinates = (PosY + 100) / 2 * H / 100; // 0..H
            return PosInPixelCoordinates - h / 2.0 + y * h / 100;
        }
    }
    public static JFrame frame;
    private static JLabel label;
    public static Graph graph = null;
    public static Graphics2D Image;
    public static graphPart graphPartMovingOrResizing = null;
    public static boolean graphPartMovingIsResizing = false;
    public static boolean graphPartMovingIsSnapMode = false;
    //
    public static int TempMouseX = 0; // TEMP, DON'T USE THESE!
    public static int TempMouseY = 0;
    //
    public static boolean IgnoreMouseDrag = false;

    public static void SetTitle(Titles title) {
        frame.setTitle(title.TitleText);
    }

    public static enum Titles {
        Loading("Loading..."),
        Default("Grapher"),
        ;
        private final String TitleText;
        Titles(String TitleText) {
            this.TitleText = TitleText;
        }
    }

    public static void main(String[] args) {
        String LoadGraphFromPath = null;
        {
            String Args = String.join(" ", args);
            if (new File(Args).exists()) {
                // Load file
                LoadGraphFromPath = Args;
            }
        }
        frame = new JFrame("Grapher");
        frame.setLocationRelativeTo(null);
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
        SetTitle(Titles.Default);
        frame.setVisible(true);
        int TargetFPS = 60;
        int FramesNotRendered = 0;
        int FramesNotRenderedMax = TargetFPS; // 0 = force render all frames

        /* Load file */
        if (LoadGraphFromPath == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showDialog(null, "Load graph");
            File f = fileChooser.getSelectedFile();
            if (f != null) {
                if (!f.exists()) {
                    try {
                        Files.write(f.toPath(), new byte[0]);
                        LoadGraphFromPath = f.getAbsolutePath();
                    } catch (IOException e) {
                        InformationWindowDisplayer.display(Information.GetDefault("Graph file could not be created at\n" + f.getAbsolutePath() + "\n" + e.getMessage(), Information.DefaultType.Error_Major));
                    }
                }
                else {
                    LoadGraphFromPath = f.getAbsolutePath();
                }
            }
        }
        if (LoadGraphFromPath != null) {
            graph = graphLoader.fromFile(LoadGraphFromPath); // also handles displaying Information
        } else {
            InformationWindowDisplayer.display(Information.GetDefault("Graph was not loaded\nbecause no path was selected.", Information.DefaultType.Error_Medium));
            graph = new Graph(null);
        }
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

                    boolean resize_frame = pWidth != w || pHeight != h;
                    boolean render_frame = updateScreen || resize_frame || graphPartMovingOrResizing != null || FramesNotRendered > FramesNotRenderedMax;

                    if (resize_frame || render_frame) {
                        Image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                        Image = Image_.createGraphics();
                        //updateScreen = true;
                    }
                    if (render_frame) {
                        //System.out.println("Rendering");
                        FramesNotRendered = 0;
                        updateScreen = false;
                        pWidth = w;
                        pHeight = h;
                        w = w <= 0 ? 1 : w;
                        h = h <= 0 ? 1 : h;
                        // Rendering
                        DrawCount = 0;
                        int Rx = (int) Render.calcAbsoluteRenderPosOfScreenCenterX(w);
                        int Ry = (int) Render.calcAbsoluteRenderPosOfScreenCenterY(h);
                        int Rw = (int) Render.calcRenderWidth(w);
                        int Rh = (int) Render.calcRenderHeight(h);
                        long StartTime = System.nanoTime();
                        if (graph != null) {
                            graph.draw(Image, Rx, Ry, Rw, Rh, w, h, false);
                        }
                        //System.out.println(DrawCount + " graphParts drawn in " + (System.nanoTime() - StartTime) / 1000 + "µs");
                        //graph.draw(Image, Rx+Rw/4, Ry+Rh/4, Rw/2, Rh/2, w/2, h/2);
                    }
                    // Finalizing
                    if (Image_ != null) {
                        if (InformationWindowDisplayer.HasToDraw()) { // NOTIFICATIONS
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
    public static double getFontSizeForPixelHeight(double pixelHeight) {
        return 72.0 * pixelHeight / Toolkit.getDefaultToolkit().getScreenResolution();
    }
    public static double fontSizeForOnePixelHighText = getFontSizeForPixelHeight(1.0);
    public static double fontSizeFor100PixelHighText = getFontSizeForPixelHeight(100.0);
    public static boolean updateScreen = false;
    private static int pWidth = -1;
    private static int pHeight = -1;
}

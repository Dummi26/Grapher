package com.mark;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private static class Render {
        private static double Zoom = 1; // Zoom level
        private static double PosX = 0; // Position from -100 to 100
        private static double PosY = 0;

        public static double getZoom() { return Zoom; }
        public static double getPosX() { return PosX; }
        public static double getPosY() { return PosY; }
        public static void setZoom(double Zoom) { Render.Zoom = Zoom; updateScreen =true; }
        public static void setPosX(double PosX) { Render.PosX = PosX; updateScreen =true; }
        public static void setPosY(double PosY) { Render.PosY = PosY; updateScreen =true; }
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
    private static JFrame frame;
    private static JLabel label;
    public static graph graph = null;
    public static graphPart graphPartMovingOrResizing = null;
    public static boolean graphPartMovingIsResizing = false;
    //
    private static int MousePosX = 0;
    private static int MousePosY = 0;
    private static int TempMouseX = 0; // TEMP, DON'T USE THESE!
    private static int TempMouseY = 0;
    //
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showDialog(null, "Load graph");
        File f = fileChooser.getSelectedFile();
        if (f == null) return;
        try {graph = graphLoader.fromFile(f.getAbsolutePath());}catch(Exception e){e.printStackTrace();return;}
        frame = new JFrame("Grapher");
        frame.getContentPane().addMouseWheelListener(e -> Render.setZoom(Math.max(Double.MIN_VALUE, Render.getZoom() * (1 - e.getPreciseWheelRotation() * 0.1)))); // * zoom speed factor
        frame.getContentPane().addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {
                TempMouseX = e.getX();
                TempMouseY = e.getY();
                graphPartMovingOrResizing = null;
                switch (e.getButton()) {
                    case 1 -> { // LEFT
                    }
                    case 2 -> { // MIDDLE
                    }
                    case 3 -> { // RIGHT
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem item;
                        item = new JMenuItem("New");
                        {
                            item.addMouseListener(new MouseListener() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                }

                                @Override
                                public void mousePressed(MouseEvent e) {
                                    CreatePopupNewMenu(popupMenu, graph);
                                }

                                @Override
                                public void mouseReleased(MouseEvent e) {
                                }

                                @Override
                                public void mouseEntered(MouseEvent e) {
                                }

                                @Override
                                public void mouseExited(MouseEvent e) {
                                }
                            });
                            popupMenu.add(item);
                        }
                        item = new JMenuItem("Edit/Select");
                        {
                            item.addMouseListener(new MouseListener() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                }

                                @Override
                                public void mousePressed(MouseEvent e) {
                                    graphPart[] gpsAtMouse = graph.getGraphPartsAtLocation(Render.calcRelativeRenderPosFromAbsoluteScreenPosX(TempMouseX, frame.getContentPane().getWidth()), Render.calcRelativeRenderPosFromAbsoluteScreenPosY(TempMouseY, frame.getContentPane().getHeight()));
                                    popupMenu.removeAll();
                                    for (graphPart gp : gpsAtMouse) {
                                        JMenuItem item = new JMenuItem(gp.toString());
                                        item.addMouseListener(new MouseListener() {
                                            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                popupMenu.removeAll();
                                                JMenuItem item;
                                                item = new JMenuItem("New (internal)");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        CreatePopupNewMenu(popupMenu, gp);
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Edit");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        CreateEditWindow(gp);
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Focus [lmb=win|rmb=fill|mmb=cont]");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        switch (e.getButton()) {
                                                            case MouseEvent.BUTTON1 -> Render.focusOnRectangle(gp.getArea(), false);
                                                            case MouseEvent.BUTTON2 -> {
                                                                Render.focusOnRectangle(gp.container.getArea(), false);
                                                            }
                                                            case MouseEvent.BUTTON3 -> Render.focusOnRectangle(gp.getArea(), true);
                                                        }
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Move");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        graphPartMovingOrResizing = gp;
                                                        graphPartMovingIsResizing = false;
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Resize");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        graphPartMovingOrResizing = gp;
                                                        graphPartMovingIsResizing = true;
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Copy to clipboard");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        StringSelection sel = new StringSelection(gp.fileSave());
                                                        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                item = new JMenuItem("Remove");
                                                item.addMouseListener(new MouseListener() {
                                                    @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                                        graph.remove(gp);
                                                    } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                                });
                                                popupMenu.add(item);
                                                popupMenu.pack();
                                            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                                        });
                                        popupMenu.add(item);
                                    }
                                    popupMenu.pack();
                                }
                                @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                            });
                            popupMenu.add(item);
                        }
                        item = new JMenuItem("Reset zoom & pos (rmb=go here)");
                        {
                            item.addMouseListener(new MouseListener() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                }

                                @Override
                                public void mousePressed(MouseEvent e) {
                                    switch (e.getButton()) {
                                        case MouseEvent.BUTTON1 -> {
                                            Render.setZoom(1);
                                            Render.setPosX(0);
                                            Render.setPosY(0);
                                        }
                                        case MouseEvent.BUTTON2 -> {
                                        }
                                        case MouseEvent.BUTTON3 -> {
                                            System.out.println(Render.calcRelativeRenderPosFromAbsoluteScreenPosX(e.getX(), frame.getContentPane().getWidth()) + " | " + Render.calcRelativeRenderPosFromAbsoluteScreenPosY(e.getY(), frame.getContentPane().getHeight()));
                                            Render.setPosX(2 * Render.calcRelativeRenderPosFromAbsoluteScreenPosX(TempMouseX, frame.getContentPane().getWidth()) - 100);
                                            Render.setPosY(2 * Render.calcRelativeRenderPosFromAbsoluteScreenPosY(TempMouseY, frame.getContentPane().getHeight()) - 100);
                                        }
                                    }
                                }

                                @Override
                                public void mouseReleased(MouseEvent e) {
                                }

                                @Override
                                public void mouseEntered(MouseEvent e) {
                                }

                                @Override
                                public void mouseExited(MouseEvent e) {
                                }
                            });
                            popupMenu.add(item);
                        }
                        item = new JMenuItem("Manage embedded data"); {
                            // TODO
                            item.addMouseListener(new MouseListener() {
                                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                    CreateEmbedManagementWindow(graph);
                                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                            });
                            popupMenu.add(item);
                        }
                        item = new JMenuItem("Reload file (" + graph.SaveToPath + ")"); {
                            item.addMouseListener(new MouseListener() {
                                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                    try { graph = graphLoader.fromFile(graph.SaveToPath); } catch (IOException ex) {ex.printStackTrace();}
                                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                            });
                            popupMenu.add(item);
                        }
                        item = new JMenuItem("Save to file (" + graph.SaveToPath + ")"); {
                            item.addMouseListener(new MouseListener() {
                                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                                    try { graphLoader.toFile(graph, graph.SaveToPath); } catch (IOException ex) {ex.printStackTrace();}
                                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                            });
                            popupMenu.add(item);
                        }
                        popupMenu.show(frame, e.getX(), e.getY());
                    }
                }
            }
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        frame.getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int DifX = e.getPoint().x - MousePosX;
                int DifY = e.getPoint().y - MousePosY;
                if (graphPartMovingOrResizing != null) {
                    Rectangle2D area = graphPartMovingOrResizing.getContainerArea();
                    double chngX = 100.0 * DifX / frame.getContentPane().getWidth() / Render.getZoom() / area.getWidth() * 100;
                    double chngY = 100.0 * DifY / frame.getContentPane().getHeight() / Render.getZoom() / area.getHeight() * 100;
                    if (graphPartMovingIsResizing) {
                        if (DifX != 0) graphPartMovingOrResizing.W = Math.min(Math.max(graphPartMovingOrResizing.W + chngX, 0), 100 - graphPartMovingOrResizing.X);
                        if (DifY != 0) graphPartMovingOrResizing.H = Math.min(Math.max(graphPartMovingOrResizing.H + chngY, 0), 100 - graphPartMovingOrResizing.Y);
                    } else {
                        if (DifX != 0) graphPartMovingOrResizing.X = Math.min(Math.max(graphPartMovingOrResizing.X + chngX, 0), 100 - graphPartMovingOrResizing.W);
                        if (DifY != 0) graphPartMovingOrResizing.Y = Math.min(Math.max(graphPartMovingOrResizing.Y + chngY, 0), 100 - graphPartMovingOrResizing.H);
                    }
                }
                //
                MousePosX = e.getPoint().x;
                MousePosY = e.getPoint().y;
            }
            @Override public void mouseDragged(MouseEvent e) {
                int DifX = e.getPoint().x - MousePosX;
                int DifY = e.getPoint().y - MousePosY;
                if (DifX != 0) Render.setPosX(Math.min(Math.max(Render.getPosX() - 200.0 * DifX / frame.getContentPane().getWidth() / Render.getZoom(), -100), 100));
                if (DifY != 0) Render.setPosY(Math.min(Math.max(Render.getPosY() - 200.0 * DifY / frame.getContentPane().getHeight() / Render.getZoom(), -100), 100));
                //System.out.println("\nPosX: " + PosX + "\nPosY: " + PosY);
                //
                MousePosX = e.getPoint().x;
                MousePosY = e.getPoint().y;
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        label = new JLabel(new ImageIcon(new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB)));
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
        int TargetFPS = 30;
        int FramesNotRendered = 0;
        int FramesNotRenderedMax = TargetFPS; // 0 = force render all frames
        while (true) {
            FramesNotRendered++;
            long nanosecondsEnd = System.nanoTime() + 1000000000/TargetFPS;
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
                    BufferedImage Image_ = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D Image = Image_.createGraphics();
                    // Rendering
                    int Rx = (int) Render.calcAbsoluteRenderPosOfScreenCenterX(w);
                    int Ry = (int) Render.calcAbsoluteRenderPosOfScreenCenterY(h);
                    int Rw = (int) Render.calcRenderWidth(w);
                    int Rh = (int) Render.calcRenderHeight(h);
                    graph.draw(Image, Rx, Ry, Rw, Rh, w, h);
                    //graph.draw(Image, Rx+Rw/4, Ry+Rh/4, Rw/2, Rh/2, w/2, h/2);
                    // Finalizing
                    label.setIcon(new ImageIcon(Image_));
                }
            }
            // END OF RENDER
            //while (System.nanoTime() < nanosecondsEnd);
            try {TimeUnit.NANOSECONDS.sleep(nanosecondsEnd - System.nanoTime());} catch (Exception e) {}
            //System.out.println("FPS: " + 1000 / ((System.nanoTime() - nanoseconds) / 1000000.0));
        }
    }
    public static boolean updateScreen = false;
    private static int pWidth = -1;
    private static int pHeight = -1;
    private static void CreatePopupNewMenu(JPopupMenu popupMenu, graphPart selectedGraphPart) {
        popupMenu.removeAll();
        JMenuItem item;
        if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                graphPartAndOutInfo info = graphLoader.fromString(((String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor)).split("\n"), 0, selectedGraphPart.parent, selectedGraphPart);
                if (info != null && info.graphPart != null) {
                    item = new JMenuItem(info.graphPart.toString());
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            selectedGraphPart.contents = graphLoader.add(selectedGraphPart.contents, info.graphPart);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
            } catch (Exception e) {}
        }
        for (gpIdentifiers gpi : gpIdentifiers.values()) {
            item = new JMenuItem(gpi.toString());
            item.addMouseListener(new MouseListener() {
                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                    graphPart newGp = graphLoader.getGraphPart(gpi, selectedGraphPart.parent, selectedGraphPart);
                    selectedGraphPart.contents = graphLoader.add(selectedGraphPart.contents, newGp);
                    CreateEditWindow(newGp);
                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
            });
            popupMenu.add(item);
        }
        popupMenu.pack();
    }
    private static JFrame CreateEditWindow(graphPart gp) {
        JFrame EditFrame = new JFrame("Grapher - Edit (" + gp + ")");
        EditFrame.setLayout(new BorderLayout());
        // Text init
        String[] CustomData = gp.customFileSave();
        String nl = "";
        String TextKeyText = "Position:";
        String TextValueText = gp.X + " " + gp.Y + " " + gp.W + " " + gp.H;
        for (String CustomDataLine : CustomData) {
            int IndexOfColon = CustomDataLine.indexOf(':');
            if (IndexOfColon >= 0) {
                TextKeyText += "\n" + CustomDataLine.substring(0, ++IndexOfColon);
                TextValueText += "\n" + CustomDataLine.substring(IndexOfColon);
            }
        }
        JTextArea TextKey = new JTextArea(TextKeyText);
        JTextArea TextValue = new JTextArea(TextValueText);
        TextValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {update();}
            @Override public void removeUpdate(DocumentEvent e) {update();}
            @Override public void changedUpdate(DocumentEvent e) {update();}
            private void update() {
                String[] LinesK = TextKey.getText().split("\n");
                String[] LinesV = (TextValue.getText() + "\n-").split("\n");
                if (LinesK.length < LinesV.length /*one additional line added here so empty last line will be read too*/) {
                    String[] Position = LinesV[0].split(" ");
                    if (Position.length == 4) {
                        try {gp.X = Double.parseDouble(Position[0]);}catch(NumberFormatException ex){}
                        try {gp.Y = Double.parseDouble(Position[1]);}catch(NumberFormatException ex){}
                        try {gp.W = Double.parseDouble(Position[2]);}catch(NumberFormatException ex){}
                        try {gp.H = Double.parseDouble(Position[3]);}catch(NumberFormatException ex){}
                    }
                    for (int i = 1; i < LinesK.length; i++) {
                        gp.customFileLoad(LinesK[i].substring(0, LinesK[i].length() - 1), LinesV[i]);
                    }
                }
            }
        });
        TextKey.setEditable(false);
        EditFrame.add(TextKey, BorderLayout.WEST);
        EditFrame.add(TextValue, BorderLayout.CENTER);
        EditFrame.setPreferredSize(new Dimension(400, 400));
        EditFrame.pack();
        EditFrame.setVisible(true);
        return EditFrame;
    }
    public static JFrame CreateEmbedManagementWindow(graph g) {return CreateEmbedManagementWindow(g, null, null);}
    private static JFrame CreateEmbedManagementWindow(graph g, Point LocationOnScreen, Dimension Size) {
        JFrame EmbedManagementFrame = new JFrame("Embed management");
        if (LocationOnScreen != null) EmbedManagementFrame.setLocation(LocationOnScreen);
        JButton AddNewButton = new JButton("Add new (file)");
        EmbedManagementFrame.setLayout(new BoxLayout(EmbedManagementFrame.getContentPane(), BoxLayout.Y_AXIS));
        // Data list
        for (int i = 0; i < g.BytesInFileData.size(); i++) { EmbedManagementFrame.add(CreateEmbedManagementWindow__CreateSingleEntry(EmbedManagementFrame, g, i)); }
        // Add new buttons
        JPanel AddNewPanel = new JPanel();
        AddNewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        AddNewButton.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                byte[] arr = CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(null, "Add this file's data", null);
                if (arr != null) {
                    g.BytesInFileData.add(arr);
                    CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                    EmbedManagementFrame.dispose();
                }
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        AddNewPanel.add(AddNewButton);
        EmbedManagementFrame.add(AddNewPanel);
        EmbedManagementFrame.pack();
        if (Size != null) EmbedManagementFrame.setSize(Size);
        EmbedManagementFrame.setVisible(true);
        return EmbedManagementFrame;
    }
    private static JPanel CreateEmbedManagementWindow__CreateSingleEntry(JFrame EmbedManagementFrame, graph g, int index) {
        JPanel Out = new JPanel();
        Out.setLayout(new FlowLayout());
        Out.add(new JLabel(index + ": " + g.BytesInFileData.get(index).length + "b"));
        JButton Button;
        Button = new JButton("Del");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                g.BytesInFileData.remove(index);
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Edit");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                byte[] arr = CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(null, "Overwrite data with this file", null);
                if (arr != null) { g.BytesInFileData.set(index, arr); }
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Save To File");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                String chosenFile = LetUserChooseFile(null, "Save to this file", null);
                if (chosenFile != null) {
                    try { Files.write(Path.of(chosenFile), g.BytesInFileData.get(index)); }
                    catch (IOException e1) {}
                }
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Move up");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                int ToIndex = index - 1;
                if (ToIndex < 0) { ToIndex += g.BytesInFileData.size(); /* wrap around to the end */ }
                byte[] ThisData = g.BytesInFileData.get(index);
                g.BytesInFileData.set(index, g.BytesInFileData.get(ToIndex));
                g.BytesInFileData.set(ToIndex, ThisData);
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        return Out;
    }
    private static byte[] CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(Component parent, String approveButtonText, String StartDirectory) {
        String filePath = LetUserChooseFile(parent, approveButtonText, StartDirectory);
        if (filePath != null) {
            try {
                return Files.readAllBytes(Path.of(filePath));
            } catch (IOException e) {}
        }
        return null;
    }
    private static String LetUserChooseFile(Component parent, String approveButtonText, String StartDirectory) {
        JFileChooser FileChooser = new JFileChooser();
        if (StartDirectory != null) { FileChooser.setCurrentDirectory(new File(StartDirectory)); }
        FileChooser.showDialog(null, approveButtonText);
        File file = FileChooser.getSelectedFile();
        if (file != null) { return file.getAbsolutePath(); }
        return null;
    }
}
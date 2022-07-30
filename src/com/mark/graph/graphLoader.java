package com.mark.graph;

import com.mark.Main;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public final class graphLoader {
    public static Graph fromFile(String sourcePath) {
        try {
            ArrayList<String> entireFile = new ArrayList<String>();
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(Path.of(sourcePath));
            } catch (IOException e) {
                InformationWindowDisplayer.display(Information.GetDefault("Graph could not be loaded from file at\n" + sourcePath, Information.DefaultType.Error_Major));
            }
            int BytePosOfEmbeddedData = -1; // the first byte following the '<' character in the file.
            {
                String line = "";
                for (int i = 0; i < bytes.length; i++) {
                    String s = new String(bytes, i, Math.min(4, bytes.length - i - 1), StandardCharsets.UTF_8);
                    if (s.length() == 0) continue;
                    char Char = s.charAt(0);
                    i += String.valueOf(Char).getBytes(StandardCharsets.UTF_8).length - 1;
                    String line2 = line + Char;
                    if (line2.equals("<")) {
                        BytePosOfEmbeddedData = i + 1;
                        break;
                    }
                    if (line2.charAt(line2.length() - 1) == '\n') {
                        entireFile.add(line);
                        line = "";
                    } else {
                        line = line2;
                    }
                }
                if (BytePosOfEmbeddedData == -1) {
                    entireFile.add(line);
                }
            }
            if (entireFile.size() > 0) {
                Graph g = new Graph(sourcePath);
                if (BytePosOfEmbeddedData >= 0) {
                    // Load embedded byte-data from file
                    int i = BytePosOfEmbeddedData;
                    while (i < bytes.length) {
                        DecodeReturn Decoded = FileLoader.Decode(bytes, i);
                        if (Decoded == null) {
                            System.out.println(":(");
                            break;
                        }
                        byte[] data = Decoded.data;
                        i = Decoded.NextNonReadByte;
                        g.BytesInFileData.add(data);
                    }
                }
                {
                    // Load graph from file
                    ArrayList<graphPart> parts = new ArrayList<graphPart>();
                    String[] entireFileAsArray = entireFile.toArray(new String[0]);
                    int ln = 0;
                    while (true) {
                        graphPartAndOutInfo info = fromString(entireFileAsArray, ln, g, g);
                        if (info == null || info.graphPart == null) break;
                        parts.add(info.graphPart);
                        ln = info.lineNum;
                    }
                    g.contents = parts.toArray(new graphPart[0]);
                }
                InformationWindowDisplayer.display(Information.GetDefault("Loaded graph from\n" + g.SaveToPath(), Information.DefaultType.Information_Long));
                return g;
            }
        } catch (Exception ex) {
            InformationWindowDisplayer.display(Information.GetDefault("Error while loading graph:\n" + ex.getMessage(), Information.DefaultType.Error_Major));
        }
        return null;
    }
    public static graphPartAndOutInfo fromString(String source, int StartLine, Graph parent, graphPart container) { return fromString(source.split("\n"), StartLine, parent, container); }
    public static graphPartAndOutInfo fromString(String[] source, int StartLine, Graph parent, graphPart container) {
        int ln = StartLine;
        if (ln == source.length) return null;
        String FirstLine = source[ln];
        if (!FirstLine.startsWith(">")) {return null;}
        try {
            graphPart part = getGraphPart(gpIdentifiers.valueOf(FirstLine.substring(1)), parent, container);
            return new graphPartAndOutInfo(part, part.fileLoad(source, StartLine + 1));
        }
        catch (IllegalArgumentException e) {}
        return null;
    }
    public static void toFile(Graph g) {
        try {
            toFile(g, g.SaveToPath());
            InformationWindowDisplayer.display(Information.GetDefault("Saved to path:\n" + Main.graph.SaveToPath(), Information.DefaultType.Information_Short));
        } catch (IOException ex) {
            InformationWindowDisplayer.display(Information.GetDefault("Could not save to path:\n" + Main.graph.SaveToPath(), Information.DefaultType.Error_Minor));
        }
    }
    public static void toFile(Graph g, String filePath) throws IOException {
        String out = "";
        for (graphPart gp : g.contents) {
            out += gp.fileSave();
        }
        Files.writeString(Path.of(filePath), out + (g.BytesInFileData.size() == 0 ? "" : "<"), StandardCharsets.UTF_8);
        FileOutputStream fos = new FileOutputStream(filePath, true);
        for (byte[] b : g.BytesInFileData) {
            fos.write(FileLoader.Encode(b));
        }
    }
    public static graphPart[] add(graphPart[] old, graphPart n) {
        graphPart[] out = new graphPart[old.length + 1];
        for (int i = 0; i < old.length; i++) out[i] = old[i];
        out[old.length] = n;
        return out;
    }
    public static graphPart getGraphPart(gpIdentifiers gpi, Graph parent, graphPart container) {
        graphPart gp = null;
        switch (gpi) {
            case Panel -> gp = new com.mark.graph.part.panel.gp(parent, container);
            case LayoutArea -> gp = new com.mark.graph.part.layout.gp(parent, container);
            case Text_Basic -> gp = new com.mark.graph.part.text.basic.gp(parent, container);
            case Text_Plus -> gp = new com.mark.graph.part.text.plus.gp(parent, container);
            case Image -> gp = new com.mark.graph.part.image.gp(parent, container);
            case Reference -> gp = new com.mark.graph.part.reference.gp(parent, container);
        }
        return gp;
    }

    public static void toImageFile(Graph graph, String path, int w, int h, boolean EntireGraph) {
        int W = (int) Main.Render.calcRenderWidth(w);
        int H = (int) Main.Render.calcRenderHeight(h);
        if (EntireGraph) {
            w = W;
            h = H;
        }
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        if (EntireGraph) {
            graph.draw(g, 0, 0, W, H, w, h, true);
        } else {
            int Rx = (int) Main.Render.calcAbsoluteRenderPosOfScreenCenterX(w);
            int Ry = (int) Main.Render.calcAbsoluteRenderPosOfScreenCenterY(h);
            graph.draw(g, Rx, Ry, W, H, w, h, true);
        }
        g.dispose();
        try {
            ImageIO.write(output, "png", new File(path));
            InformationWindowDisplayer.display(Information.GetDefault("Saved " + w + "x" + h + " image to '" + path + "'.", Information.DefaultType.Information_Short));
        } catch (IOException e) {
            InformationWindowDisplayer.display(Information.GetDefault("Failed to save image to '" + path + "'.", Information.DefaultType.Error_Minor));
        }
    }
}

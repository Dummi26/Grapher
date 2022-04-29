package com.mark.graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public final class graphLoader {
    public static graph fromFile(String sourcePath) throws IOException {
        ArrayList<String> entireFile = new ArrayList<String>();
        byte[] bytes = Files.readAllBytes(Path.of(sourcePath));
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
                if (line2.charAt(line2.length()-1) == '\n') {
                    entireFile.add(line);
                    line = "";
                }
                else {
                    line = line2;
                }
            }
            if (BytePosOfEmbeddedData == -1) {entireFile.add(line);}
        }
        if (entireFile.size() > 0) {
            graph g = new graph(sourcePath);
            if (BytePosOfEmbeddedData >= 0) {
                // Load embedded byte-data from file
                int i = BytePosOfEmbeddedData;
                while (i < bytes.length) {
                    DecodeReturn Decoded = FileLoader.Decode(bytes, i);
                    if (Decoded == null) {System.out.println(":("); break;}
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
            return g;
        }
        else {return null;}
    }
    public static graphPartAndOutInfo fromString(String source, int StartLine, graph parent, graphPart container) { return fromString(source.split("\n"), StartLine, parent, container); }
    public static graphPartAndOutInfo fromString(String[] source, int StartLine, graph parent, graphPart container) {
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
    public static void toFile(graph g) {
        try { toFile(g, g.SaveToPath()); } catch (IOException ex) {}
    }
    public static void toFile(graph g, String filePath) throws IOException {
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
    public static graphPart getGraphPart(gpIdentifiers gpi, graph parent, graphPart container) {
        graphPart gp = null;
        switch (gpi) {
            case Panel -> gp = new gpPanel(parent, container);
            case Text -> gp = new gpText(parent, container);
            case Image -> gp = new gpImage(parent, container);
        }
        return gp;
    }
}


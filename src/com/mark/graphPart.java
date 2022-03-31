package com.mark;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public abstract class graphPart {
    private gpIdentifiers gpIdentifier;
    public graphPart(graph parent, graphPart container, gpIdentifiers gpIdentifier) { this.gpIdentifier = gpIdentifier; this.parent = parent; this.container = container; }
    protected graphPart container;
    protected graph parent;
    public int fileLoad(String[] file, int firstLine) {
        ArrayList<graphPart> NewGraphParts = new ArrayList<graphPart>();
        while (firstLine < file.length) {
            String line = file[firstLine++];
            if (line.length() == 0) break;
            switch (line.charAt(0)) {
                case ' ' -> {
                    int indexOfColon = line.indexOf(':');
                    if (indexOfColon > 0 && line.length() > indexOfColon + 1) {
                        String identifier = line.substring(1, indexOfColon);
                        String txt = line.substring(indexOfColon+1);
                        switch (identifier) {
                            case "X" -> {try {X = Double.parseDouble(txt);} catch(Exception e) {}}
                            case "Y" -> {try {Y = Double.parseDouble(txt);} catch(Exception e) {}}
                            case "W" -> {try {W = Double.parseDouble(txt);} catch(Exception e) {}}
                            case "H" -> {try {H = Double.parseDouble(txt);} catch(Exception e) {}}
                            default -> customFileLoad(identifier, txt);
                        }
                    }
                }
                case '>' -> {
                    graphPartAndOutInfo info = graphLoader.fromString(file, firstLine-1, parent, this);
                    if (info != null) {
                        firstLine = info.lineNum;
                        if (info.graphPart != null) {
                            NewGraphParts.add(info.graphPart);
                        }
                    }
                }
            }
        }
        // append new graphParts to contents
        NewGraphParts.addAll(0, List.of(contents));
        contents = NewGraphParts.toArray(new graphPart[0]);
        return firstLine;
    }
    public String fileSave() {
        String out = ">" + gpIdentifier.toString() + "\n";
        String[] custom = customFileSave();
        out += " X:" + X + "\n Y:" + Y + "\n W:" + W + "\n H:" + H + "\n";
        for (String c : customFileSave()) {
            out += " " + c + "\n";
        }
        for (graphPart c : contents) {
            out += c.fileSave();
        }
        out += "\n"; // empty line to indicate termination of this graphPart
        return out;
    }

    // customFileLoad should use a switch statement on identifier to decide how to interpret value.
    protected abstract void customFileLoad(String identifier, String value);
    // customFileSave should save everything that customFileLoad can load.
    protected abstract String[] customFileSave();

    public Rectangle2D getContainerArea() {
        if (container == null) return new Rectangle2D.Double(0, 0, 100, 100);
        return container.getArea();
    }
    public Rectangle2D getArea() {
        Rectangle2D ContainerArea = getContainerArea();
        return new Rectangle2D.Double(
                ContainerArea.getX() + ContainerArea.getWidth() * X / 100,
                ContainerArea.getY() + ContainerArea.getHeight() * Y / 100,
                ContainerArea.getWidth() * W / 100,
                ContainerArea.getHeight() * H / 100);
    }
    // rx, ry, rw and rh are the location of the parent graphPart.
    public void draw(Graphics2D Img, double rx, double ry, double rw, double rh, int ImgW, int ImgH) {
        // Get location of this graphPart
        double x = rx + rw * X / 100;
        double y = ry + rh * Y / 100;
        double w = rw * W / 100;
        double h = rh * H / 100;
        if (x + w < 0 || y + h < 0 || x > ImgW || y > ImgH) {
            return;
        }
        // Get int location so it can be used for drawing directly
        int ix = (int) Math.ceil(x);
        int iy = (int) Math.ceil(y);
        int iw = (int) Math.floor(w);
        int ih = (int) Math.floor(h);
        // ^ location in pixels for this part to be drawn on (i=int) ^
        if (iw > 0 && ih > 0) {
            customDraw(Img, ix, iy, iw, ih, ImgW, ImgH); // pixel-coordinates
            for (graphPart content : contents) {
                content.draw(Img, x, y, w, h, ImgW, ImgH);
            }
        }
    }
    // x,y,w,h are the area in pixel-coordinates that can be drawn on.
    abstract void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH);
    // Location stored as floating point numbers, where 0 <= X|Y|W|H <= 100
    public double X = 0;
    public double Y = 0;
    public double W = 100;
    public double H = 100;
    // other
    public graphPart[] contents = new graphPart[0];

    //

    public graphPart[] getGraphPartsAtLocation(double lX, double lY) { // lX,lY 0..100
        ArrayList<graphPart> out = new ArrayList<graphPart>();
        for (graphPart gp : contents) {
            if (gp.X <= lX && lX <= gp.X + gp.W && gp.Y <= lY && lY <= gp.Y + gp.H) {
                out.add(gp);
                double llX = 100 * (lX - gp.X) / gp.W;
                double llY = 100 * (lY - gp.Y) / gp.H;
                out.addAll(List.of(gp.getGraphPartsAtLocation(llX, llY)));
            }
        }
        return out.toArray(new graphPart[0]);
    }
    public int remove(graphPart objectToRemove) {
        ArrayList<graphPart> contentsList = new ArrayList<graphPart>(List.of(contents));
        int removed = 0;
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == objectToRemove) {
                contentsList.remove(i - removed);
                removed++;
            }
        }
        contents = contentsList.toArray(new graphPart[0]);
        for (graphPart gp : contents) removed += gp.remove(objectToRemove); // remove recursively
        return removed;
    }
}
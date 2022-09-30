package com.mark.graph;

import com.mark.Main;
import com.mark.input.CustomInputInfo;
import com.mark.input.CustomInputInfoContainer;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class graphPart {
    private gpIdentifiers gpIdentifier;
    public gpIdentifiers gpIdentifier() { return gpIdentifier; }
    private String ID = null;
    private LocalDateTime LastUpdatedTime = LocalDateTime.now();
    public LocalDateTime LastUpdatedTime() { return LastUpdatedTime; }
    public String ID() { return ID; }
    public void ID(String ID) { this.ID = ID; }
    public void ChangeIdTo(String ID) {
        if (parent != null) {
            if (this.ID != null) {
                parent.RemoveGraphPartWithID(this.ID);
            }
            if (ID != null && ID.isEmpty() == false && ID.isBlank() == false && ID.length() > 0) {
                this.ID = ID;
                parent.AddGraphPartWithID(this);
            }
            else {
                this.ID = null;
            }
        }
        else {
            this.ID = null;
        }
    }
    public graphPart(Graph parent, graphPart container, gpIdentifiers gpIdentifier) { this.gpIdentifier = gpIdentifier; this.parent = parent; this.container = container; }
    public graphPart container;
    public Graph parent;
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
                            case "ID" -> { ChangeIdTo(txt); }
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
        updated();
        return firstLine;
    }
    public String fileSave() {
        String out = ">" + gpIdentifier.toString() + "\n";
        String[] custom = customFileSave();
        out += " X:" + X + "\n Y:" + Y + "\n W:" + W + "\n H:" + H + "\n";
        if (ID != null) {
            out += " ID:" + ID + "\n";
        }
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
    public void customFileLoad(String identifier, String value) {
        customFileLoadLine(identifier, value);
        updated();
    }
    public abstract void customFileLoadLine(String identifier, String value);
    // customFileSave should save everything that customFileLoad can load.
    public abstract String[] customFileSave();

    public Rectangle2D getContainerArea() {
        if (container == null) return new Rectangle2D.Double(0, 0, 100, 100);
        return container.getContentArea();
    }
    public Rectangle2D getArea() {
        Rectangle2D ContainerArea = getContainerArea();
        return new Rectangle2D.Double(
                ContainerArea.getX() + ContainerArea.getWidth() * X / 100,
                ContainerArea.getY() + ContainerArea.getHeight() * Y / 100,
                ContainerArea.getWidth() * W / 100,
                ContainerArea.getHeight() * H / 100);
    }
    public Rectangle2D getContentArea() {
        Rectangle2D ContainerArea = getContainerArea();
        return new Rectangle2D.Double(
                ContainerArea.getX() + ContainerArea.getWidth() * getActualX() / 100,
                ContainerArea.getY() + ContainerArea.getHeight() * getActualY() / 100,
                ContainerArea.getWidth() * getActualW() / 100,
                ContainerArea.getHeight() * getActualH() / 100);
    }
    // rx, ry, rw and rh are the location of the parent graphPart. blockThreadedActions will run image scaling etc. in a blocking way, ensuring that the first output will be correct (instead of having to wait a few frames), but freezing the thread.
    public void draw(Graphics2D Img, double rx, double ry, double rw, double rh, int ImgW, int ImgH, boolean blockThreadedActions) {
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
            Main.DrawCount++;
            customDraw(Img, ix, iy, iw, ih, ImgW, ImgH, blockThreadedActions); // pixel-coordinates
            // get actual content area **after** customDraw!
            double xCont = rx + rw * getActualX() / 100;
            double yCont = ry + rh * getActualY() / 100;
            double wCont = rw * getActualW() / 100;
            double hCont = rh * getActualH() / 100;
            for (graphPart content : contents) {
                content.draw(Img, xCont, yCont, wCont, hCont, ImgW, ImgH, blockThreadedActions);
            }
            customDrawAfter(Img, ix, iy, iw, ih, ImgW, ImgH, blockThreadedActions); // pixel-coordinates
        }
    }
    /**x,y,w,h are the area in pixel-coordinates that can be drawn on. there is also customDrawAfter to draw things AFTER the subgraphparts have been drawn, although this shouldn't usually be used as it may overwrite things other gps have drawn.*/
    protected abstract void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions);
    /**Same args as customDraw(...). See customDraw's docs for info.*/
    protected void customDrawAfter(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {}
    // Location stored as floating point numbers, where 0 <= X|Y|W|H <= 100
    private double X = 0;
    /**Ranging from 0 to 100, this is the location or size relative to the container. This is just the bounding box, some gps (such as images) may be smaller. Use getActual?() to get these values.*/
    public double X() { return X; } public void X(double X) { this.X = X; updated();}
    private double Y = 0;
    /**Ranging from 0 to 100, this is the location or size relative to the container. This is just the bounding box, some gps (such as images) may be smaller. Use getActual?() to get these values.*/
    public double Y() { return Y; } public void Y(double Y) { this.Y = Y; updated();}
    private double W = 100;
    /**Ranging from 0 to 100, this is the location or size relative to the container. This is just the bounding box, some gps (such as images) may be smaller. Use getActual?() to get these values.*/
    public double W() { return W; } public void W(double W) { this.W = W; updated();}
    private double H = 100;
    /**Ranging from 0 to 100, this is the location or size relative to the container. This is just the bounding box, some gps (such as images) may be smaller. Use getActual?() to get these values.*/
    public double H() { return H; } public void H(double H) { this.H = H; updated();}

    protected double EffectiveX = 0; // X + W * EffectiveW = Actual X-Position where the contents will be drawn. EffectiveX + EffectiveW shouldn't exceed 1.
    protected double EffectiveY = 0;
    protected double EffectiveW = 1; // W * EffectiveW = Width of the contents (used to ensure the aspect ratio of the contents is correct)
    protected double EffectiveH = 1;
    public double getActualX() { return X + W * EffectiveX; }
    public double getActualY() { return Y + H * EffectiveY; }
    public double getActualW() { return W * EffectiveW; }
    public double getActualH() { return H * EffectiveH; }
    public double getActualEmptySpaceX() { return W * (1 - EffectiveW) / 2; }
    public double getActualEmptySpaceY() { return H * (1 - EffectiveH) / 2; }
    // other
    public graphPart[] contents = new graphPart[0];

    //

    protected abstract String customToString();

    public graphPart[] getGraphPartsAtLocation(double lX, double lY) { // lX,lY 0..100
        ArrayList<graphPart> out = new ArrayList<graphPart>();
        for (graphPart gp : contents) {
            double gpX = gp.getActualX();
            double gpY = gp.getActualY();
            double gpW = gp.getActualW();
            double gpH = gp.getActualH();
            if (gpX <= lX && lX <= gpX + gpW && gpY <= lY && lY <= gpY + gpH) {
                out.add(gp);
                double llX = 100 * (lX - gpX) / gpW;
                double llY = 100 * (lY - gpY) / gpH;
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
                int removeIndex = i - removed;
                contentsList.remove(removeIndex).was_removed(); // remove object and clean up
                removed++;
            }
        }
        contents = contentsList.toArray(new graphPart[0]);
        for (graphPart gp : contents) removed += gp.remove(objectToRemove); // remove recursively
        updated();
        return removed;
    }
    public void removeAll() {
        for (var gp : contents) { gp.was_removed(); }
        contents = new graphPart[0];
    }

    private void was_removed() {
        wasRemoved();
        removeAll();
        ChangeIdTo(null);
    }
    /**Perform some cleanup operations. The default cleanup things are handled by graphPart, this method only handles custom stuff.*/
    protected abstract void wasRemoved();

    protected void updated() {
        LastUpdatedTime = LocalDateTime.now();
        if (container != null) {
            container.updated();
        }
        Main.updateScreen = true;
    }

    public ArrayList<graphPart> GetAllContentsRecursive() { return GetAllContentsRecursive(new ArrayList<graphPart>()); }
    public ArrayList<graphPart> GetAllContentsRecursive(ArrayList<graphPart> current) {
        for (var gp : contents) {
            current.add(gp);
            current = gp.GetAllContentsRecursive(current);
        }
        return current;
    }

    @Override public String toString() {
        return gpIdentifier.toString() + (ID == null ? "" : " \"" + ID + "\"") + " :: " + customToString();
    }

    // Information

    protected ArrayList<ArrayList<Information>> gpInformations = new ArrayList<>();
    protected void createInformationCategory() {
        gpInformations.add(new ArrayList<>());
    }
    protected void removeInformationCategory(int cat) {
        var infos = gpInformations.remove(cat);
        while (infos.size() > 0) {
            removeInformation(infos, 0);
        }
    }
    protected int getInformationCategoryCount() {
        return gpInformations.size();
    }
    protected void removeInformation(int cat, int index) {
        removeInformation(gpInformations.get(cat), index); // start out animation and make duration not infinite
    }
    private void removeInformation(ArrayList<Information> list, int index) {
        list.remove(index).ResetTime_Out();
    }
    protected void addStaticInformation(int cat, Information info) {
        info.duration = null;
        info.ResetTime_Dur();
        gpInformations.get(cat).add(info);
        InformationWindowDisplayer.display(info);
    }
    protected Information getInformation(int cat, int index) {
        return gpInformations.get(cat).get(index);
    }
    protected int getInformationsSize(int cat) {
        return gpInformations.get(cat).size();
    }

    /**If this returns non-null, the returned custom input infos will be available from the context menu.*/
    public abstract CustomInputInfoContainer customUserInput();
}

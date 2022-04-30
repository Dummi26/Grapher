package com.mark.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class graph extends graphPart {
    public graph(String SaveToPath) {super(null, null, null); this.parent = this; this.SaveToPath = SaveToPath; }
    public ArrayList<byte[]> BytesInFileData = new ArrayList<byte[]>();
    private String SaveToPath;
    private HashMap<String, graphPart> graphPartIDs = new HashMap<>();
    @Override
    public void customFileLoadLine(String identifier, String value) {}
    @Override
    public String[] customFileSave() { return new String[0]; }
    @Override void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {}

    public String SaveToPath() { return SaveToPath; }

    /**
     * Adds a GP to graphPartIDs. This is automatically called when a GP is created.
     * @param graphPart the GP to add
     * @return null if the ID was not changed, or the new ID if it was changed
     */
    public String AddGraphPartWithID(graphPart graphPart) {
        int Num = 0;
        if (graphPartIDs.containsKey(graphPart.ID())) {
            String ID = graphPart.ID();
            while (graphPartIDs.containsKey(ID + "_" + ++Num)) ;
            graphPart.ID(ID + "_" + Num);
        }
        graphPartIDs.putIfAbsent(graphPart.ID(), graphPart);
        return graphPart.ID();
    }
    public boolean RemoveGraphPartWithID(String ID) {
        if (graphPartIDs.containsKey(ID)) {
            graphPartIDs.remove(ID);
            return true;
        }
        return false;
    }
    public graphPart GetGraphPartWithID(String ID) {
        if (graphPartIDs.containsKey(ID)) return graphPartIDs.get(ID);
        return null;
    }

    @Override protected String customToString() { return null; }
}
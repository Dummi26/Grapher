package com.mark;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class graph extends graphPart {
    public graph(String SaveToPath) {super(null, null, null); this.parent = this; this.SaveToPath = SaveToPath; }
    public ArrayList<byte[]> BytesInFileData = new ArrayList<byte[]>();
    public String SaveToPath;
    @Override protected void customFileLoad(String identifier, String value) {}
    @Override protected String[] customFileSave() { return new String[0]; }
    @Override void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {}
}
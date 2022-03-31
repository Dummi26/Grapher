package com.mark;
import org.w3c.dom.Text;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class gpText extends graphPart {
    public gpText(graph parent, graphPart container) { super(parent, container, gpIdentifiers.Text); }

    String[] text = new String[] { "[default text]" };
    Color color = new Color(255, 255, 255, 255);
    AlignmentBounds alignmentBounds = AlignmentBounds.TopLeft;
    AlignmentText alignmentText = AlignmentText.Left;
    enum AlignmentBounds {
        TopLeft,
        TopCenter,
        TopRight,
        MiddleLeft,
        MiddleCenter,
        MiddleRight,
        BottomLeft,
        BottomCenter,
        BottomRight,
    }
    enum AlignmentText {
        Left,
        Center,
        Right,
    }

    @Override
    protected void customFileLoad(String identifier, String value) {
        switch (identifier) {
            case "AlignmentBounds" -> {
                try { alignmentBounds = AlignmentBounds.valueOf(value); } catch (IllegalArgumentException e) {}
            }
            case "AlignmentText" -> {
                try { alignmentText = AlignmentText.valueOf(value); } catch (IllegalArgumentException e) {}
            }
            case "Color" -> {
                String[] split = value.split(",");
                switch (split.length) {
                    case 1 -> {try {color = new Color(255, 255, 255, Integer.parseInt(split[0]));} catch(Exception e) {}}
                    case 3 -> {try {color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 255);} catch(Exception e) {}}
                    case 4 -> {try {color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));} catch(Exception e) {}}
                }
            }
            case "Text" -> { text = value.replace("\\n", "\n").split("\n"); }
        }
    }

    @Override
    protected String[] customFileSave() {
        String textAsString = "";
        boolean AddNewLine = false;
        for (String line : text) {
            if (AddNewLine) { textAsString += "\\n"; }
            else { AddNewLine = true; }
            textAsString += line;
        }
        return new String[] {
                "AlignmentBounds:" + alignmentBounds.toString(),
                "AlignmentText:" + alignmentText.toString(),
                "Color:" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha(),
                "Text:" + textAsString,
        };
    }

    @Override
    void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
        double X = x;
        double Y = y;
        double W = w;
        double H = h;
        Img.setPaint(color);
        //Img.setFont(new Font("Serif", Font.BOLD, 20));
        //Img.setFont(new Font());
        // Autosize
        /*float size = 0;
        float inc = 0.1f; // increment
        do {
            size += inc;
            Img.setFont(Img.getFont().deriveFont(size));
        } while (Img.getFont().getStringBounds(text, Img.getFontRenderContext()).getWidth() < W && Img.getFont().getStringBounds(text, Img.getFontRenderContext()).getHeight() < H);
        Img.setFont(Img.getFont().deriveFont(size - inc));
        */
        double TextWidthMax = 0;
        int TextHeightPerLine = Img.getFontMetrics().getHeight();
        int TextHeightTotal = TextHeightPerLine * text.length;
        for (String line : text) {
            double Width = Img.getFont().getStringBounds(line, Img.getFontRenderContext()).getWidth();
            if (Width > TextWidthMax) TextWidthMax = Width;
        }
        // adjust font size
        Img.setFont(Img.getFont().deriveFont((float)Math.min(
                Img.getFont().getSize2D() * W / TextWidthMax,
                Img.getFont().getSize2D() * H / TextHeightTotal
        )));
        // get new sizes
        TextWidthMax = 0;
        TextHeightPerLine = Img.getFontMetrics().getHeight();
        TextHeightTotal = TextHeightPerLine * text.length;
        for (String line : text) {
            double Width = Img.getFont().getStringBounds(line, Img.getFontRenderContext()).getWidth();
            if (Width > TextWidthMax) TextWidthMax = Width;
        }
        //
        Y -= Img.getFontMetrics().getDescent();
        if (alignmentBounds == AlignmentBounds.TopLeft || alignmentBounds == AlignmentBounds.TopCenter || alignmentBounds == AlignmentBounds.TopRight) Y += TextHeightPerLine;
        if (alignmentBounds == AlignmentBounds.MiddleLeft || alignmentBounds == AlignmentBounds.MiddleCenter || alignmentBounds == AlignmentBounds.MiddleRight) Y += (TextHeightPerLine + (H - TextHeightPerLine * (text.length - 1))) / 2;
        if (alignmentBounds == AlignmentBounds.BottomLeft || alignmentBounds == AlignmentBounds.BottomCenter || alignmentBounds == AlignmentBounds.BottomRight) Y += H - TextHeightPerLine * (text.length - 1);
        //if (alignment == Alignment.TopLeft || alignment == Alignment.MiddleLeft || alignment == Alignment.BottomLeft) x += 0;
        if (alignmentBounds == AlignmentBounds.TopCenter || alignmentBounds == AlignmentBounds.MiddleCenter || alignmentBounds == AlignmentBounds.BottomCenter) X += (W - TextWidthMax) / 2;
        if (alignmentBounds == AlignmentBounds.TopRight || alignmentBounds == AlignmentBounds.MiddleRight || alignmentBounds == AlignmentBounds.BottomRight) X += W - TextWidthMax;
        // rectangle which will contain the text: X,Y TextWidthMax,TextHeightTotal
        for (int i = 0; i < text.length; i++) {
            double OffsetMax = (TextWidthMax - Img.getFont().getStringBounds(text[i], Img.getFontRenderContext()).getWidth());
            double Offset = 0;
            if (alignmentText == AlignmentText.Center) { Offset = OffsetMax / 2; }
            if (alignmentText == AlignmentText.Right) { Offset = OffsetMax; }
            Img.drawString(text[i], (int)(X + Offset), (int)(Y + TextHeightPerLine * i));
        }
    }

    @Override public String toString() {
        return "Text, " + (text.length == 0 ? "" : (text[0] + (text.length > 1 ? " [" + text.length + " lines]" : "")));
    }
}

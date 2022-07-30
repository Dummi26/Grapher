package com.mark.graph.part.text.plus;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.graph.part.text.plus.components.text;

import java.awt.*;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.Text_Plus); loadTextDataFromText(); }

    public String text = "line:text:[def]\\:size:0.2*multiline:line:text:line\\:text:1\\:/line:text:line\\:fraction:text:4\\:text:2\\://///";
    public textComponent textData = null;

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "Text" -> {
                text = value;
                loadTextDataFromText();
            }
        }
    }

    private void loadTextDataFromText() {
        var o = componentsFromString.componentFromString(text, 0);
        if (o != null) {
            textData = o.comp;
        } else {
            textData = new text();
            textData.SelfFromString("failed to load!\\:", 0);
        }
    }

    @Override
    public String[] customFileSave() {
        return new String[] {
                "Text:" + text,
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        var width = textData.getW();
        var height = textData.getB() - textData.getT();
        var scale_width = w / width;
        var scale_height = h / height;
        var scale = Math.min(scale_width, scale_height);
        textData.draw(Img, x, y +h/2.0, scale, blockThreadedActions);
    }

    @Override public String customToString() {
        return text;
    }
}

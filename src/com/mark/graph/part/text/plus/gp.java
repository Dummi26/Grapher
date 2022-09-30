package com.mark.graph.part.text.plus;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.graph.part.text.plus.components.text;
import com.mark.graph.part.text.plus.components.textCharacter;
import com.mark.input.CustomInputInfoContainer;
import com.mark.notification.Information;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.Text_Plus); createInformationCategory(); loadTextDataFromText(); }

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

    public ArrayList<String> info_invalidIdentifiers_d = new ArrayList<>();
    private void loadTextDataFromText() {
        info_invalidIdentifiers_d.clear();
        var o = componentsFromString.componentFromString(text, 0, this);
        ManageInfoNotifications();
        if (o != null) {
            textData = o.comp;
        } else {
            var t = new text(this);
            t.characters.addAll(List.of(
                    new textCharacter(this, 'f', false),
                    new textCharacter(this, 'a', false),
                    new textCharacter(this, 'i', false),
                    new textCharacter(this, 'l', false),
                    new textCharacter(this, 'e', false),
                    new textCharacter(this, 'd', false),
                    new textCharacter(this, ' ', false),
                    new textCharacter(this, 't', false),
                    new textCharacter(this, 'o', false),
                    new textCharacter(this, ' ', false),
                    new textCharacter(this, 'l', false),
                    new textCharacter(this, 'o', false),
                    new textCharacter(this, 'a', false),
                    new textCharacter(this, 'd', false),
                    new textCharacter(this, '.', false)
            ));
            textData = t;
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

    @Override
    protected void wasRemoved() {
        // Clear notifications
        info_invalidIdentifiers_d.clear();
        ManageInfoNotifications();
    }
    @Override public CustomInputInfoContainer customUserInput() { return null; }

    private void ManageInfoNotifications() {
        while (getInformationsSize(0) > info_invalidIdentifiers_d.size()) {
            // there are too many info notifications
            removeInformation(0, 0);
        }
        // set the text for the info notifications
        for (int i = 0; i < info_invalidIdentifiers_d.size(); i++) {
            if (i >= getInformationsSize(0)) {
                // there are not enough info notifications
                Information info = Information.GetDefault(
                        "",
                        Information.DefaultType.Error_Minor
                );
                addStaticInformation(0, info);
            }
            Information info = getInformation(0, i);
            info.Information = "Identifier '" + info_invalidIdentifiers_d.get(i) + "' in " + gpIdentifiers.Text_Plus.name() + " was not recognized!";
        }
    }
}

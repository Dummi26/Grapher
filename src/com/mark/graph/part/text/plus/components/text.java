package com.mark.graph.part.text.plus.components;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.part.text.plus.gp;
import com.mark.graph.part.text.plus.textComponent;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.*;
import java.util.ArrayList;

public class text extends textComponent {
    public text(gp parent) { super(parent); }
    public double monospacedCharacterWidth = new textCharacter(null, ' ', true).getW();
    public ArrayList<textCharacter> characters = new ArrayList<>();

    public void clearString() {
        characters.clear();
    }

    @Override
    public textComponent.specialCase isSpecial() {
        return null;
    }

    @Override
    public double getW() {
        double w = 0;
        for (var c : characters) {
            w += c.getW();
        }
        return w;
    }

    @Override
    public double getT() {
        return -0.5;
    }

    @Override
    public double getB() {
        return 0.5;
    }

    @Override
    public void draw(Graphics2D Img, double x, double y, double scale, boolean blockThreadedActions) {
        for (textCharacter character : characters) {
            character.draw(Img, x, y, scale, blockThreadedActions);
            x += scale * character.getW();
        }
    }

    private static ArrayList<Information> info_invalidEscapeSequence_i = new ArrayList<>();
    private ArrayList<Character> info_invalidEscapeSequence_d = new ArrayList<>();
    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        clearString();
        info_invalidEscapeSequence_d.clear();
        boolean backslashActive = false;
        boolean monospaced = false;
        for (int i = indexOfFirstChar; i < str.length(); i++) {
            char c = str.charAt(i);
            if (backslashActive) {
                switch (c) {
                    case ':' -> {
                        SelfFromString_Finish();
                        return i+1;
                    }
                    case 'm' -> monospaced = !monospaced;
                    case '\\' -> characters.add(new textCharacter(parent, '\\', monospaced));
                    default -> info_invalidEscapeSequence_d.add(c);
                }
                backslashActive = false;
            } else {
                if (c == '\\') {
                    backslashActive = true;
                } else {
                    characters.add(new textCharacter(parent, c, monospaced));
                }
            }
        }
        SelfFromString_Finish();
        return str.length();
    }
    private void SelfFromString_Finish() {
        while (info_invalidEscapeSequence_i.size() > info_invalidEscapeSequence_d.size()) {
            // there are too many info notifications
            info_invalidEscapeSequence_i.get(0).ResetTime_Out(); // start out animation and make duration not infinite
            info_invalidEscapeSequence_i.remove(0);
        }
        // set the text for the info notifications
        for (int i = 0; i < info_invalidEscapeSequence_d.size(); i++) {
            if (i >= info_invalidEscapeSequence_i.size()) {
                // there are not enough info notifications
                Information info = Information.GetDefault(
                        "",
                        Information.DefaultType.Error_Minor
                );
                info.duration = null;
                info.ResetTime_Dur(); // because duration was changed
                info_invalidEscapeSequence_i.add(info);
                InformationWindowDisplayer.display(info);
            }
            Information info = info_invalidEscapeSequence_i.get(i);
            info.Information = "Escape sequence '\\" + info_invalidEscapeSequence_d.get(i) + "' in " + gpIdentifiers.Text_Plus.name() + "[text:] was not recognized and will be ignored.\nValid chars are: \\ (backslash), m (monospaced), : (end)";
        }
    }
}

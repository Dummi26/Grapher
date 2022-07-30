package com.mark.graph.part.text.plus.components;

import com.mark.graph.part.text.plus.textComponent;
import com.mark.notification.Information;

import java.awt.*;
import java.util.ArrayList;

public class text extends textComponent {
    public double monospacedCharacterWidth = new textCharacter(' ', true).getW();
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

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        clearString();
        boolean backslashActive = false;
        boolean monospaced = false;
        for (int i = indexOfFirstChar; i < str.length(); i++) {
            char c = str.charAt(i);
            if (backslashActive) {
                switch (c) {
                    case ':' -> {
                        return i+1;
                    }
                    case 'm' -> monospaced = !monospaced;
                    case '\\' -> characters.add(new textCharacter('\\', monospaced));
                    default -> com.mark.notification.InformationWindowDisplayer.display(Information.GetDefault(
                            "Escape sequence '\\" + c + "' was not recognized and will be ignored.\nValid chars are: \\ (backslash), m (monospaced), : (end)",
                            Information.DefaultType.Information_Short
                    ));
                }
                backslashActive = false;
            } else {
                if (c == '\\') {
                    backslashActive = true;
                } else {
                    characters.add(new textCharacter(c, monospaced));
                }
            }
        }
        return str.length();
    }
}

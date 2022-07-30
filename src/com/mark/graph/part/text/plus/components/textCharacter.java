package com.mark.graph.part.text.plus.components;

import com.mark.Main;
import com.mark.graph.part.text.plus.textComponent;

import java.awt.*;

public class textCharacter extends textComponent {
    char character;
    boolean monospaced;
    double scale_size_factor = 0.4; // how big the character is. This value is completely arbitrary for now.
    double scale_down_factor = 0.2; // how far down the bottom line for a character should be (0=center, 1=bottom). This value has been arbitrarily chosen by me, the god of text drawing, who definitely knows how to properly draw text (i have no idea what i'm doing, please help me)
    public textCharacter(char character, boolean monospaced) {
        this.character = character;
        this.monospaced = monospaced;
    }
    @Override
    public textComponent.specialCase isSpecial() {
        return null;
    }

    @Override
    public double getW() {
        return monospaced ? 0.75 : getCharacterW();
    }
    private double getCharacterW() {
        return scale_size_factor * Main.Image.getFont().deriveFont((float)Main.fontSizeFor100PixelHighText).getStringBounds(""+character, Main.Image.getFontRenderContext()).getWidth() / 100;
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
        Img.setColor(Color.WHITE);
        var scale_size = scale * scale_size_factor;
        var scale_down = scale * scale_down_factor;
        Img.setFont(Img.getFont().deriveFont((float)(scale_size * Main.fontSizeForOnePixelHighText)));
        var str = ""+character;
        if (monospaced) {
            x += (getW() - getCharacterW()) * scale / 2;
        }
        Img.drawString(str, (float)x, (float)(y + scale_down/*this works because getB()-getT() == 1.0, meaning that the character is exactly size pixels high.*/));
    }

    @Override
    public int SelfFromString(String str, int indexOfFirstChar) {
        if (indexOfFirstChar < str.length()) {
            character = str.charAt(indexOfFirstChar);
        }
        return indexOfFirstChar + 1;
    }
}

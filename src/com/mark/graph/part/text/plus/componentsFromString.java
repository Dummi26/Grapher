package com.mark.graph.part.text.plus;

import com.mark.graph.part.text.plus.components.container.line;
import com.mark.graph.part.text.plus.components.container.multiline;
import com.mark.graph.part.text.plus.components.container.size;
import com.mark.graph.part.text.plus.components.math.fraction;
import com.mark.graph.part.text.plus.components.text;
import com.mark.graph.part.text.plus.components.textCharacter;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.util.ArrayList;

public final class componentsFromString {
    public static output_textComponents componentsFromString(String str, int indexOfFirstChar, gp parent) {
        ArrayList<textComponent> components = new ArrayList<>();
        while (indexOfFirstChar >= 0 && indexOfFirstChar < str.length()) {
            if (",/:".indexOf(str.charAt(indexOfFirstChar)) >= 0) {
                var o = new output_textComponents();
                o.nextFirstChar = indexOfFirstChar + 1;
                o.comps = components;
                return o;
            }
            var o = componentFromString(str, indexOfFirstChar, parent);
            if (o == null) {
                return null;
            }
            components.add(o.comp);
            indexOfFirstChar = o.nextFirstChar;
        }
        var o = new output_textComponents();
        o.nextFirstChar = indexOfFirstChar;
        o.comps = components;
        return o;
    }
    public static output_textComponent componentFromString(String str, int indexOfFirstChar, gp parent) {
        int startIndex = indexOfFirstChar;
        int identifierTerminatorIndex = str.indexOf(":", startIndex);
        if (identifierTerminatorIndex >= 0) {
            String identifier = str.substring(startIndex, identifierTerminatorIndex);
            textComponent component;
            switch (identifier) {
                case "line" -> component = new line(parent);
                case "multiline" -> component = new multiline(parent);
                case "size" -> component = new size(parent);
                case "text" -> component = new text(parent);
                case "char" -> component = new textCharacter(parent, ' ', true);
                case "fraction" -> component = new fraction(parent);
                default -> {
                    parent.info_invalidIdentifiers_d.add(identifier);
                    return null;
                }
            }
            startIndex = component.SelfFromString(str, identifierTerminatorIndex + 1);
            if (startIndex >= 0) {
                var o = new output_textComponent();
                o.comp = component;
                o.nextFirstChar = startIndex;
                return o;
            }
        }
        return null;
    }
}

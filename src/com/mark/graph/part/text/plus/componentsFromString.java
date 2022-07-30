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
    public static output_textComponents componentsFromString(String str, int indexOfFirstChar) {
        ArrayList<textComponent> components = new ArrayList<>();
        while (indexOfFirstChar >= 0 && indexOfFirstChar < str.length()) {
            if (",/:".indexOf(str.charAt(indexOfFirstChar)) >= 0) {
                var o = new output_textComponents();
                o.nextFirstChar = indexOfFirstChar + 1;
                o.comps = components;
                return o;
            }
            var o = componentFromString(str, indexOfFirstChar);
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
    public static output_textComponent componentFromString(String str, int indexOfFirstChar) {
        int startIndex = indexOfFirstChar;
        int identifierTerminatorIndex = str.indexOf(":", startIndex);
        if (identifierTerminatorIndex >= 0) {
            String identifier = str.substring(startIndex, identifierTerminatorIndex);
            textComponent component;
            switch (identifier) {
                case "line" -> component = new line();
                case "multiline" -> component = new multiline();
                case "size" -> component = new size();
                case "text" -> component = new text();
                case "char" -> component = new textCharacter(' ', true);
                case "fraction" -> component = new fraction();
                default -> {
                    InformationWindowDisplayer.display(Information.GetDefault(
                            "Identifier '" + identifier + "' was not recognized!",
                            Information.DefaultType.Information_Short
                    ));
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

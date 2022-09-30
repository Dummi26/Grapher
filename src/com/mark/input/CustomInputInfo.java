package com.mark.input;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class CustomInputInfo {
    public String displayText;
    private CustomInputInfoContainer containedInfos = null;
    public CustomInputInfoContainer getContainedInfos() { return containedInfos; }
    private Consumer<CustomInputMetadata> action = null;
    public Consumer<CustomInputMetadata> getAction() { return action; }

    public CustomInputInfo(String displayText, Consumer<CustomInputMetadata> action) {
        this.displayText = displayText;
        this.action = action;
    }
    public CustomInputInfo(String displayText, CustomInputInfoContainer containedInfos) {
        this.displayText = displayText;
        this.containedInfos = containedInfos;
    }

    public void add_to_popupmenu(JPopupMenu popupMenu) {
        JMenuItem item = new JMenuItem(displayText);
        item.addMouseListener(new MouseListener() {@Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
            if (action != null) {
                action.accept(new CustomInputMetadata(e));
            } else {
                if (containedInfos != null) {
                    containedInfos.replace_popupmenu(popupMenu);
                }
            }
        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}});
        popupMenu.add(item);
    }
}

package com.mark.input;

import javax.swing.*;
import java.util.ArrayList;

public class CustomInputInfoContainer {
    public ArrayList<CustomInputInfo> infos = new ArrayList<>();
    public CustomInputInfoContainer(ArrayList<CustomInputInfo> infos) {
        this.infos = infos;
    }

    public void replace_popupmenu(JPopupMenu popupMenu) {
        popupMenu.removeAll();
        for (var input : infos) {
            input.add_to_popupmenu(popupMenu);
        }
        popupMenu.pack();
    }
}

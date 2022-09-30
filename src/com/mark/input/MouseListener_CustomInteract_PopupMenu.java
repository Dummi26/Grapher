package com.mark.input;


import com.mark.Main;
import com.mark.graph.graphLoader;
import com.mark.graph.graphPart;
import com.mark.graph.graphPartAndOutInfo;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class MouseListener_CustomInteract_PopupMenu implements MouseListener {
    JPopupMenu popupMenu;
    public MouseListener_CustomInteract_PopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        graphPart[] gpsAtMouse = Main.graph.getGraphPartsAtLocation(Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosX(Main.TempMouseX, Main.frame.getContentPane().getWidth()), Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosY(Main.TempMouseY, Main.frame.getContentPane().getHeight()));
        popupMenu.removeAll();
        for (graphPart gp : gpsAtMouse) {
            var customUserInput = gp.customUserInput();
            if (customUserInput != null) {
                new CustomInputInfo(gp.toString(), customUserInput).add_to_popupmenu(popupMenu);
            }
        }
        popupMenu.pack();
    }
    @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
}

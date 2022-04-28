package com.mark.input;

import com.mark.Main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;

public class MouseMotionAdapter_Custom_Main extends MouseMotionAdapter {
    @Override public void mouseMoved(MouseEvent e) {
        int DifX = e.getPoint().x - Main.MousePosX;
        int DifY = e.getPoint().y - Main.MousePosY;
        if (Main.graphPartMovingOrResizing != null) {
            Rectangle2D area = Main.graphPartMovingOrResizing.getContainerArea();
            double chngX = 100.0 * DifX / Main.frame.getContentPane().getWidth() / Main.Render.getZoom() / area.getWidth() * 100;
            double chngY = 100.0 * DifY / Main.frame.getContentPane().getHeight() / Main.Render.getZoom() / area.getHeight() * 100;
            if (Main.graphPartMovingIsResizing) {
                if (DifX != 0) Main.graphPartMovingOrResizing.W = Math.min(Math.max(Main.graphPartMovingOrResizing.W + chngX, 0), 100 - Main.graphPartMovingOrResizing.X);
                if (DifY != 0) Main.graphPartMovingOrResizing.H = Math.min(Math.max(Main.graphPartMovingOrResizing.H + chngY, 0), 100 - Main.graphPartMovingOrResizing.Y);
            } else {
                if (DifX != 0) Main.graphPartMovingOrResizing.X = Math.min(Math.max(Main.graphPartMovingOrResizing.X + chngX, 0), 100 - Main.graphPartMovingOrResizing.W);
                if (DifY != 0) Main.graphPartMovingOrResizing.Y = Math.min(Math.max(Main.graphPartMovingOrResizing.Y + chngY, 0), 100 - Main.graphPartMovingOrResizing.H);
            }
        }
        //
        Main.MousePosX = e.getPoint().x;
        Main.MousePosY = e.getPoint().y;
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (Main.IgnoreMouseDrag) { Main.IgnoreMouseDrag = false; } else {
            int DifX = e.getPoint().x - Main.MousePosX;
            int DifY = e.getPoint().y - Main.MousePosY;
            if (DifX != 0) Main.Render.setPosX(Math.min(Math.max(Main.Render.getPosX() - 200.0 * DifX / Main.frame.getContentPane().getWidth() / Main.Render.getZoom(), -100), 100));
            if (DifY != 0) Main.Render.setPosY(Math.min(Math.max(Main.Render.getPosY() - 200.0 * DifY / Main.frame.getContentPane().getHeight() / Main.Render.getZoom(), -100), 100));
        }
        Main.MousePosX = e.getPoint().x;
        Main.MousePosY = e.getPoint().y;
    }
}
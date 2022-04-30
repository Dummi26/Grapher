package com.mark.input;

import com.mark.Main;
import com.mark.graph.graphPart;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MouseMotionAdapter_Custom_Main extends MouseMotionAdapter {
    private static boolean GraphPartMovingOrResizingPExisted = false;
    private static Rectangle2D GraphPartMovingOrResizingStartLocationAndSize;
    private static Point GraphPartMovingOrResizingStartMouseLocation;
    public static int MousePosX = 0;
    public static int MousePosY = 0;
    @Override public void mouseMoved(MouseEvent e) {
        if (Main.graphPartMovingOrResizing != null) {
            if (!GraphPartMovingOrResizingPExisted) {
                GraphPartMovingOrResizingPExisted = true;
                GraphPartMovingOrResizingStartMouseLocation = e.getPoint();
                GraphPartMovingOrResizingStartLocationAndSize = Main.graphPartMovingOrResizing.getArea();
            }
            double DifX = e.getPoint().x - GraphPartMovingOrResizingStartMouseLocation.x;
            double DifY = e.getPoint().y - GraphPartMovingOrResizingStartMouseLocation.y;
            Rectangle2D area = Main.graphPartMovingOrResizing.getContainerArea();
            double chngX = 100.0 * DifX / Main.frame.getContentPane().getWidth() / Main.Render.getZoom() / area.getWidth() * 100;
            double chngY = 100.0 * DifY / Main.frame.getContentPane().getHeight() / Main.Render.getZoom() / area.getHeight() * 100;
            if (Main.graphPartMovingIsResizing) {
                if (DifX != 0) Main.graphPartMovingOrResizing.W = Math.min(Math.max(GraphPartMovingOrResizingStartLocationAndSize.getWidth() + chngX, 0), 100 - Main.graphPartMovingOrResizing.X);
                if (DifY != 0) Main.graphPartMovingOrResizing.H = Math.min(Math.max(GraphPartMovingOrResizingStartLocationAndSize.getHeight() + chngY, 0), 100 - Main.graphPartMovingOrResizing.Y);
                if (Main.graphPartMovingIsSnapMode) {
                    // TODO: Snap to any adjacent graphParts
                }
            } else {
                if (DifX != 0) Main.graphPartMovingOrResizing.X = Math.min(Math.max(GraphPartMovingOrResizingStartLocationAndSize.getX() + chngX, 0), 100 - Main.graphPartMovingOrResizing.W);
                if (DifY != 0) Main.graphPartMovingOrResizing.Y = Math.min(Math.max(GraphPartMovingOrResizingStartLocationAndSize.getY() + chngY, 0), 100 - Main.graphPartMovingOrResizing.H);
            }
        }
        else {
            if (GraphPartMovingOrResizingPExisted) {
                GraphPartMovingOrResizingPExisted = false;
            }
        }
        //
        MousePosX = e.getPoint().x;
        MousePosY = e.getPoint().y;
    }
    private static boolean Between(double val, double min, double max) {
        return val > min && val < max;
    }
    private static Coords GetCoords(Rectangle2D rect) { return new Coords(rect.getX(), rect.getX() + rect.getWidth(), rect.getY(), rect.getY() + rect.getHeight()); }
    private static class Coords {
        public double L;
        public double R;
        public double T;
        public double B;
        public Coords(double L, double R, double T, double B) {
            this.L = L;
            this.R = R;
            this.T = T;
            this.B = B;
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (Main.IgnoreMouseDrag) { Main.IgnoreMouseDrag = false; } else {
            int DifX = e.getPoint().x - MousePosX;
            int DifY = e.getPoint().y - MousePosY;
            if (DifX != 0) Main.Render.setPosX(Math.min(Math.max(Main.Render.getPosX() - 200.0 * DifX / Main.frame.getContentPane().getWidth() / Main.Render.getZoom(), -100), 100));
            if (DifY != 0) Main.Render.setPosY(Math.min(Math.max(Main.Render.getPosY() - 200.0 * DifY / Main.frame.getContentPane().getHeight() / Main.Render.getZoom(), -100), 100));
        }
        MousePosX = e.getPoint().x;
        MousePosY = e.getPoint().y;
    }
}
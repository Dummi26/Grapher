package com.mark;


import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class MouseListener_Custom_PopupMenu implements MouseListener {
    JPopupMenu popupMenu;
    public MouseListener_Custom_PopupMenu(JPopupMenu popupMenu) {
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
            JMenuItem item = new JMenuItem(gp.toString());
            item.addMouseListener(new MouseListener() {
                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                    popupMenu.removeAll();
                    JMenuItem item;
                    item = new JMenuItem("New (internal)");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            PopupMenuHelper.CreatePopupNewMenu(popupMenu, gp);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Edit");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            PopupMenuHelper.CreateEditWindow(gp);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Focus [rmb=fill|mmb=cont]");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            switch (e.getButton()) {
                                case MouseEvent.BUTTON1 -> Main.Render.focusOnRectangle(gp.getArea(), false);
                                case MouseEvent.BUTTON2 -> {
                                    Main.Render.focusOnRectangle(gp.container.getArea(), false);
                                }
                                case MouseEvent.BUTTON3 -> Main.Render.focusOnRectangle(gp.getArea(), true);
                            }
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Move");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            Main.graphPartMovingOrResizing = gp;
                            Main.graphPartMovingIsResizing = false;
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Resize");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            Main.graphPartMovingOrResizing = gp;
                            Main.graphPartMovingIsResizing = true;
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Clone to closest edge (rmb=diagonal)");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            graphPartAndOutInfo info = graphLoader.fromString(gp.fileSave(), 0, gp.parent, gp.container);
                            if (info != null && info.graphPart != null) {
                                graphPart newGraphPart = info.graphPart;
                                double RelRenPosX = Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosX(Main.TempMouseX, Main.frame.getContentPane().getWidth());
                                double RelRenPosY = Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosY(Main.TempMouseY, Main.frame.getContentPane().getHeight());
                                Rectangle2D RelRenArea = gp.getArea();
                                double OffsetRight = 100 * (RelRenPosX - RelRenArea.getCenterX()) / RelRenArea.getWidth();
                                double OffsetDown = 100 * (RelRenPosY - RelRenArea.getCenterY()) / RelRenArea.getHeight();
                                System.out.println(OffsetRight + ", " + OffsetDown);
                                switch (e.getButton()) {
                                    case MouseEvent.BUTTON1 -> {
                                        if (Math.abs(OffsetDown) > Math.abs(OffsetRight)) {
                                            newGraphPart.Y += Math.copySign(1, OffsetDown) * newGraphPart.H;
                                        } else {
                                            newGraphPart.X += Math.copySign(1, OffsetRight) * newGraphPart.W;
                                        }
                                    }
                                    case MouseEvent.BUTTON3 -> {
                                        newGraphPart.Y += Math.copySign(newGraphPart.H, OffsetDown);
                                        newGraphPart.X += Math.copySign(newGraphPart.W, OffsetRight);
                                    }
                                }
                                gp.container.contents = graphLoader.add(gp.container.contents, newGraphPart);
                            }
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Copy to clipboard");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            StringSelection sel = new StringSelection(gp.fileSave());
                            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    item = new JMenuItem("Remove");
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            Main.graph.remove(gp);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                    popupMenu.pack();
                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
            });
            popupMenu.add(item);
        }
        popupMenu.pack();
    }
    @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
}
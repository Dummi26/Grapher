package com.mark.input;

import com.mark.Main;
import com.mark.graph.graphLoader;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class MouseListener_Custom_Main implements MouseListener {
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {
        Main.TempMouseX = e.getX();
        Main.TempMouseY = e.getY();
        Main.graphPartMovingOrResizing = null;
        switch (e.getButton()) {
            case 1 -> { // LEFT
            }
            case 2 -> { // MIDDLE
            }
            case 3 -> { // RIGHT
                JPopupMenu popupMenu = new JPopupMenu("Edit");
                JMenuItem item;
                item = new JMenuItem("New");
                {
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {}
                        @Override public void mousePressed(MouseEvent e) {
                            PopupMenuHelper.CreatePopupNewMenu(popupMenu, Main.graph);
                        }
                        @Override public void mouseReleased(MouseEvent e) {}
                        @Override public void mouseEntered(MouseEvent e) {}
                        @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
                item = new JMenuItem("Edit/Select");
                {
                    item.addMouseListener(new MouseListener_Custom_PopupMenu(popupMenu));
                    popupMenu.add(item);
                }
                item = new JMenuItem("Reset zoom & pos (rmb=go here)");
                {
                    item.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            switch (e.getButton()) {
                                case MouseEvent.BUTTON1 -> {
                                    Main.Render.setZoom(1);
                                    Main.Render.setPosX(0);
                                    Main.Render.setPosY(0);
                                }
                                case MouseEvent.BUTTON2 -> {
                                }
                                case MouseEvent.BUTTON3 -> {
                                    System.out.println(Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosX(e.getX(), Main.frame.getContentPane().getWidth()) + " | " + Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosY(e.getY(), Main.frame.getContentPane().getHeight()));
                                    Main.Render.setPosX(2 * Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosX(Main.TempMouseX, Main.frame.getContentPane().getWidth()) - 100);
                                    Main.Render.setPosY(2 * Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosY(Main.TempMouseY, Main.frame.getContentPane().getHeight()) - 100);
                                }
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                        }
                    });
                    popupMenu.add(item);
                }
                item = new JMenuItem("Mange IDs [NOT IMPLEMENTED]");
                {
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            PopupMenuHelper.CreateEmbedManagementWindow(Main.graph);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
                item = new JMenuItem("Manage embedded data");
                {
                    // TODO
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            PopupMenuHelper.CreateEmbedManagementWindow(Main.graph);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
                item = new JMenuItem("Reload file (" + Main.graph.SaveToPath() + ")");
                {
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            String SaveToPath = Main.graph.SaveToPath();
                            Main.graph = null;
                            Main.SetTitle(Main.Titles.Loading);
                            Main.graph = graphLoader.fromFile(SaveToPath);
                            Main.SetTitle(Main.Titles.Default);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
                item = new JMenuItem("Save to file (" + Main.graph.SaveToPath() + ")");
                {
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            graphLoader.toFile(Main.graph);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
                popupMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
                    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {Closing(e);}
                    @Override public void popupMenuCanceled(PopupMenuEvent e) {Closing(e);}
                    private void Closing(PopupMenuEvent e) {
                        Main.IgnoreMouseDrag = true;
                    }
                });
                popupMenu.show(Main.frame, e.getX(), e.getY());
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}

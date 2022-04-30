package com.mark.search;

import com.mark.Main;
import com.mark.graph.gpText;
import com.mark.graph.graph;
import com.mark.graph.graphLoader;
import com.mark.graph.graphPart;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SearchInGraphWindow {
    graph g;
    JFrame frame;
    JFrame helpFrame;
    JTextArea helpTextArea;
    JButton searchButton;
    JTextArea textArea;
    public SearchInGraphWindow(graph g) {
        this.g = g;
        //
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> { repackFrame(); });
        textArea = new JTextArea();
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                repackFrame();
            }
        });
        //
        helpFrame = new JFrame("Search help");
        helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        helpTextArea = new JTextArea("loading help...");
        helpTextArea.setEditable(false);
        helpFrame.add(helpTextArea);
        refreshHelpInHelpFrame();
        helpFrame.pack();
        helpFrame.setVisible(true);
        //
        frame = new JFrame("Loading...");
        frame.setSize(new Dimension(400, 200));
        //
        helpFrame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { frame.dispose(); super.windowClosed(e); } });
        //
        repackFrame();
    }
    private void refreshHelpInHelpFrame() {
        helpTextArea.setText(GetHelpText());
    }
    private void repackFrame() {
        // save old stuff
        Dimension frameOgSize = frame.getSize();
        Point frameOgLocation = frame.getLocation();
        var OldFrame = frame;
        for (var wl : frame.getWindowListeners()){
            frame.removeWindowListener(wl);
        }
        // create new stuff
        frame = new JFrame("Search in Graph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { helpFrame.dispose(); super.windowClosed(e); } });
        frame.setLayout(new BorderLayout());
        frame.add(textArea, BorderLayout.NORTH);
        while (frame.getComponents().length > 2) frame.remove(0);
        ArrayList<graphPart> validGraphParts = new ArrayList<>();
        String[] filters = textArea.getText().split("\n");
        for (graphPart gp : g.contents) {
            validGraphParts = getValidGraphParts(validGraphParts, gp, filters);
        }
        JPanel PreviewsPanel = new JPanel();
        PreviewsPanel.setLayout(new FlowLayout());
        for (graphPart gp : validGraphParts) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            // string representation
            JTextField textField = new JTextField(gp.toString());
            textField.setEditable(false);
            panel.add(textField, BorderLayout.NORTH);
            // Go To
            JButton GoTo = new JButton("Go to");
            GoTo.addActionListener(e -> {
                Main.Render.focusOnRectangle(gp.getArea(), false);
            });
            panel.add(GoTo, BorderLayout.SOUTH);
            // preview
            BufferedImage Image_ = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D Image = Image_.createGraphics();
            // create a new graphPart from the old one and use that to draw so not to mess up the old one's cache (like scaled images etc.)
            graphLoader.fromString(gp.fileSave(), 0, gp.parent, gp.container).graphPart.draw(Image, 0, 0, Image_.getWidth(), Image_.getHeight(), Image_.getWidth(), Image_.getHeight());
            panel.add(new JLabel(new ImageIcon(Image_)), BorderLayout.CENTER);
            PreviewsPanel.add(panel);
        }
        /*
         */
        frame.add(PreviewsPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocation(frameOgLocation);
        frame.setSize(frameOgSize);
        frame.setVisible(true);
        textArea.grabFocus();
        OldFrame.dispose();
    }

    private ArrayList<graphPart> getValidGraphParts(ArrayList<graphPart> currentList, graphPart g, String[] filters) {
        if (passesFilters(g, filters)) { currentList.add(g); }
        for (graphPart gp : g.contents) {
            currentList = getValidGraphParts(currentList, gp, filters);
        }
        return currentList;
    }

    private String GetHelpText() {
        return GetHelpTextHelper.GetHelpText();
    }
    private static class GetHelpTextHelper {
        public static String GetHelpText() {
            return "Search queries: (anything syntactically incorrect will return false, each condition has its own line, all conditions must be true)" +
                    I(00) + "Position:" +
                    I(01) + "L/R/T/B (left, right, top, bottom)" +
                    I(01) + ">/>=/</<= (greater than / less than)" +
                    I(01) + "[value] (0-100, can be decimal)" +
                    I(01) + "Example: R>50 -> right edge must be in the right half of the graph" +
                    I(00) + "ID" +
                    I(01) + "ID/equals=[id] :: Shows the graph part with the specified id \"[id]\"" +
                    I(01) + "ID/equalsIgnoreCase=[id] :: Shows the graph part with the specified id or any id that matches the given \"[id]\" when ignoring upper-/lowercase" +
                    I(01) + "ID/startsWith=[id] :: Shows all graph parts whose id starts with the specified string" +
                    I(01) + "ID/endsWith=[id] :: Shows all graph parts whose id ends with the specified string" +
                    I(01) + "ID/contains=[id] :: Shows all graph parts whose id contains the specified string" +
                    I(00) + "Type" +
                    I(01) + "Type=[type] :: Shows the graph part with the specified type \"[type]\". Types: " + GetAllTypes() +
                    I(00) + "Container::[query] :: Shows all graph parts that are inside the container that matches the query" +
                    I(00) + "Contains::[query] :: Shows all graph parts that contain a graph part that matches the query"
                    ;
        }
        // NewLine and Indentation generator
        private static String I(int i) {
            String Out = "\n";
            while (--i >= 0) {
                Out += "    ";
            }
            return Out;
        }
        // GetAllTypes
        private static String GetAllTypes() {
            String Out = "";
            for (var s : com.mark.graph.gpIdentifiers.values()) {
                Out += s + ", ";
            }
            return Out.substring(0, Out.length() - 2);
        }
    }
    private boolean passesFilters(graphPart g, String[] filters) {
        for (String filter : filters) {
            if (!passesFilter(g, filter)) return false;
        }
        return true;
    }
    private boolean passesFilter(graphPart g, String filter) {
        if (filter.isBlank() || filter.isEmpty()) return true;


        // Position or height
        if (filter.startsWith("L>=")) { try { return g.getArea().getX() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("T>=")) { try { return g.getArea().getY() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("R>=")) { try { Rectangle2D area = g.getArea(); return area.getX() + area.getWidth() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("B>=")) { try { Rectangle2D area = g.getArea(); return area.getY() + area.getHeight() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("W>=")) { try { return g.getArea().getWidth() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("H>=")) { try { return g.getArea().getHeight() >= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }

        if (filter.startsWith("L>")) { try { return g.getArea().getX() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("T>")) { try { return g.getArea().getY() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("R>")) { try { Rectangle2D area = g.getArea(); return area.getX() + area.getWidth() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("B>")) { try { Rectangle2D area = g.getArea(); return area.getY() + area.getHeight() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("W>")) { try { return g.getArea().getWidth() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("H>")) { try { return g.getArea().getHeight() > Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }

        if (filter.startsWith("L<=")) { try { return g.getArea().getX() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("T<=")) { try { return g.getArea().getY() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("R<=")) { try { Rectangle2D area = g.getArea(); return area.getX() + area.getWidth() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("B<=")) { try { Rectangle2D area = g.getArea(); return area.getY() + area.getHeight() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("W<=")) { try { return g.getArea().getWidth() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("H<=")) { try { return g.getArea().getHeight() <= Double.parseDouble(filter.substring(3)); } catch (NumberFormatException e) { return false; } }

        if (filter.startsWith("L<")) { try { return g.getArea().getX() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("T<")) { try { return g.getArea().getY() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("R<")) { try { Rectangle2D area = g.getArea(); return area.getX() + area.getWidth() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("B<")) { try { Rectangle2D area = g.getArea(); return area.getY() + area.getHeight() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("W<")) { try { return g.getArea().getWidth() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }
        if (filter.startsWith("H<")) { try { return g.getArea().getHeight() < Double.parseDouble(filter.substring(2)); } catch (NumberFormatException e) { return false; } }


        // ID
        if (filter.startsWith("ID/equals=")) { return g.ID() == null ? false : g.ID().equals(filter.substring(10)); }
        if (filter.startsWith("ID/equalsIgnoreCase=")) { return g.ID() == null ? false : g.ID().equals(filter.substring(20)); }
        if (filter.startsWith("ID/startsWith=")) { return g.ID() == null ? false : g.ID().startsWith(filter.substring(14)); }
        if (filter.startsWith("ID/endsWith=")) { return g.ID() == null ? false : g.ID().endsWith(filter.substring(12)); }
        if (filter.startsWith("ID/contains=")) { return g.ID() == null ? false : g.ID().contains(filter.substring(12)); }


        // Type
        if (filter.startsWith("Type=")) { return g.gpIdentifier().toString().equals(filter.substring(5)); }


        // Container/Contains
        if (filter.startsWith("Container::")) { return g.container == null ? false : passesFilter(g.container, filter.substring(11)); }
        if (filter.startsWith("Contains::")) {
            String iFilter = filter.substring(10);
            for (var igp : g.contents) {
                if (passesFilter(igp, iFilter)) return true;
            }
            return false;
        }


        if (g instanceof gpText) {
            String prefix = "Text:";
            String textIncludes = prefix + "TextIncludes:";
            if (filter.startsWith(textIncludes)) {
                String filterText = filter.substring(textIncludes.length());
                String[] lines = ((gpText) g).text;
                for (String line : lines) {
                    if (line.contains(filterText)) return true;
                }
                return false;
            }
        }
        return false;
    }
}

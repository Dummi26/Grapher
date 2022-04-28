package com.mark.search;

import com.mark.graph.gpText;
import com.mark.graph.graph;
import com.mark.graph.graphPart;

import javax.swing.*;
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
        frame = new JFrame("Search in Graph");
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
        frame.setLayout(new FlowLayout());
        frame.add(searchButton);
        frame.add(textArea);
        while (frame.getComponents().length > 2) frame.remove(0);
        ArrayList<graphPart> validGraphParts = new ArrayList<>();
        String[] filters = textArea.getText().split("\n");
        for (graphPart gp : g.contents) {
            validGraphParts = getValidGraphParts(validGraphParts, gp, filters);
        }
        for (graphPart gp : validGraphParts) {
            BufferedImage Image_ = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D Image = Image_.createGraphics();
            gp.draw(Image, 0, 0, Image_.getWidth(), Image_.getHeight(), Image_.getWidth(), Image_.getHeight());
            frame.add(new JLabel(new ImageIcon(Image_)));
        }
        System.out.println("Found " + validGraphParts.size() + " matching items.");
        /*
         */
        frame.pack();
        frame.setLocation(frameOgLocation);
        frame.setSize(frameOgSize);
        frame.setVisible(true);
        OldFrame.dispose();
    }
    private ArrayList<graphPart> getValidGraphParts(ArrayList<graphPart> currentList, graphPart g, String[] filters) {
        if (passesFilters(g, filters)) { currentList.add(g); System.out.println("+");}
        for (graphPart gp : g.contents) {
            System.out.println("GP");
            currentList = getValidGraphParts(currentList, gp, filters);
        }
        return currentList;
    }

    private boolean passesFilters(graphPart g, String[] filters) {
        for (String filter : filters) {
            if (!passesFilter(g, filter)) return false;
        }
        return true;
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
                    I(01) + "Example: R>50 -> right edge must be in the right half of the graph"
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

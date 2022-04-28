package com.mark.input;

import com.mark.Main;
import com.mark.graph.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PopupMenuHelper {
    public static void CreatePopupNewMenu(JPopupMenu popupMenu, graphPart selectedGraphPart) {
        popupMenu.removeAll();
        JMenuItem item;
        if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                graphPartAndOutInfo info = graphLoader.fromString(((String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor)).split("\n"), 0, selectedGraphPart.parent, selectedGraphPart);
                if (info != null && info.graphPart != null) {
                    item = new JMenuItem(info.graphPart.toString());
                    item.addMouseListener(new MouseListener() {
                        @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                            double RelRenPosX = Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosX(Main.TempMouseX, Main.frame.getContentPane().getWidth());
                            double RelRenPosY = Main.Render.calcRelativeRenderPosFromAbsoluteScreenPosY(Main.TempMouseY, Main.frame.getContentPane().getHeight());
                            Rectangle2D RelRenArea = selectedGraphPart.getArea();
                            double InnerPosX = Math.min(Math.max(100 * (RelRenPosX - RelRenArea.getX()) / RelRenArea.getWidth() - info.graphPart.W / 2, 0), 100 - info.graphPart.W);
                            double InnerPosY = Math.min(Math.max(100 * (RelRenPosY - RelRenArea.getY()) / RelRenArea.getHeight() - info.graphPart.H / 2, 0), 100 - info.graphPart.H);
                            info.graphPart.X = InnerPosX;
                            info.graphPart.Y = InnerPosY;
                            selectedGraphPart.contents = graphLoader.add(selectedGraphPart.contents, info.graphPart);
                        } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
                    });
                    popupMenu.add(item);
                }
            } catch (Exception e) {}
        }
        for (gpIdentifiers gpi : gpIdentifiers.values()) {
            item = new JMenuItem(gpi.toString());
            item.addMouseListener(new MouseListener() {
                @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                    graphPart newGp = graphLoader.getGraphPart(gpi, selectedGraphPart.parent, selectedGraphPart);
                    selectedGraphPart.contents = graphLoader.add(selectedGraphPart.contents, newGp);
                    CreateEditWindow(newGp);
                } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
            });
            popupMenu.add(item);
        }
        popupMenu.pack();
    }
    public static JFrame CreateEditWindow(graphPart gp) {
        JFrame EditFrame = new JFrame("Grapher - Edit (" + gp + ")");
        EditFrame.setLayout(new BorderLayout());
        // Text init
        String[] CustomData = gp.customFileSave();
        String nl = "";
        String TextKeyText = "Position:";
        String TextValueText = gp.X + " " + gp.Y + " " + gp.W + " " + gp.H;
        for (String CustomDataLine : CustomData) {
            int IndexOfColon = CustomDataLine.indexOf(':');
            if (IndexOfColon >= 0) {
                TextKeyText += "\n" + CustomDataLine.substring(0, ++IndexOfColon);
                TextValueText += "\n" + CustomDataLine.substring(IndexOfColon);
            }
        }
        JTextArea TextKey = new JTextArea(TextKeyText);
        JTextArea TextValue = new JTextArea(TextValueText);
        TextValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {update();}
            @Override public void removeUpdate(DocumentEvent e) {update();}
            @Override public void changedUpdate(DocumentEvent e) {update();}
            private void update() {
                String[] LinesK = TextKey.getText().split("\n");
                String[] LinesV = (TextValue.getText() + "\n-").split("\n");
                if (LinesK.length < LinesV.length /*one additional line added here so empty last line will be read too*/) {
                    String[] Position = LinesV[0].split(" ");
                    if (Position.length == 4) {
                        try {gp.X = Double.parseDouble(Position[0]);}catch(NumberFormatException ex){}
                        try {gp.Y = Double.parseDouble(Position[1]);}catch(NumberFormatException ex){}
                        try {gp.W = Double.parseDouble(Position[2]);}catch(NumberFormatException ex){}
                        try {gp.H = Double.parseDouble(Position[3]);}catch(NumberFormatException ex){}
                    }
                    for (int i = 1; i < LinesK.length; i++) {
                        gp.customFileLoad(LinesK[i].substring(0, LinesK[i].length() - 1), LinesV[i]);
                    }
                    Main.updateScreen = true;
                }
            }
        });
        TextKey.setEditable(false);
        EditFrame.add(TextKey, BorderLayout.WEST);
        EditFrame.add(TextValue, BorderLayout.CENTER);
        EditFrame.setPreferredSize(new Dimension(400, 400));
        EditFrame.pack();
        EditFrame.setVisible(true);
        return EditFrame;
    }
    public static JFrame CreateEmbedManagementWindow(graph g) {return CreateEmbedManagementWindow(g, null, null);}
    public static JFrame CreateEmbedManagementWindow(graph g, Point LocationOnScreen, Dimension Size) {
        JFrame EmbedManagementFrame = new JFrame("Embed management");
        if (LocationOnScreen != null) EmbedManagementFrame.setLocation(LocationOnScreen);
        JButton AddNewButton = new JButton("Add new (file)");
        EmbedManagementFrame.setLayout(new BoxLayout(EmbedManagementFrame.getContentPane(), BoxLayout.Y_AXIS));
        // Data list
        for (int i = 0; i < g.BytesInFileData.size(); i++) { EmbedManagementFrame.add(CreateEmbedManagementWindow__CreateSingleEntry(EmbedManagementFrame, g, i)); }
        // Add new buttons
        JPanel AddNewPanel = new JPanel();
        AddNewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        AddNewButton.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                byte[] arr = CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(null, "Add this file's data", null);
                if (arr != null) {
                    g.BytesInFileData.add(arr);
                    CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                    EmbedManagementFrame.dispose();
                }
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        AddNewPanel.add(AddNewButton);
        EmbedManagementFrame.add(AddNewPanel);
        EmbedManagementFrame.pack();
        if (Size != null) EmbedManagementFrame.setSize(Size);
        EmbedManagementFrame.setVisible(true);
        return EmbedManagementFrame;
    }
    public static JPanel CreateEmbedManagementWindow__CreateSingleEntry(JFrame EmbedManagementFrame, graph g, int index) {
        JPanel Out = new JPanel();
        Out.setLayout(new FlowLayout());
        Out.add(new JLabel(index + ": " + g.BytesInFileData.get(index).length + "b"));
        JButton Button;
        Button = new JButton("Del");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                g.BytesInFileData.remove(index);
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Edit");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                byte[] arr = CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(null, "Overwrite data with this file", null);
                if (arr != null) { g.BytesInFileData.set(index, arr); }
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Save To File");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                String chosenFile = LetUserChooseFile(null, "Save to this file", null);
                if (chosenFile != null) {
                    try { Files.write(Path.of(chosenFile), g.BytesInFileData.get(index)); }
                    catch (IOException e1) {}
                }
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        Button = new JButton("Move up");
        Button.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {} @Override public void mousePressed(MouseEvent e) {
                int ToIndex = index - 1;
                if (ToIndex < 0) { ToIndex += g.BytesInFileData.size(); /* wrap around to the end */ }
                byte[] ThisData = g.BytesInFileData.get(index);
                g.BytesInFileData.set(index, g.BytesInFileData.get(ToIndex));
                g.BytesInFileData.set(ToIndex, ThisData);
                CreateEmbedManagementWindow(g, EmbedManagementFrame.getLocationOnScreen(), EmbedManagementFrame.getSize());
                EmbedManagementFrame.dispose();
            } @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
        });
        Out.add(Button);
        return Out;
    }
    public static byte[] CreateEmbedManagementWindow__LetUserChooseFileAndLoadBytes(Component parent, String approveButtonText, String StartDirectory) {
        String filePath = LetUserChooseFile(parent, approveButtonText, StartDirectory);
        if (filePath != null) {
            try {
                return Files.readAllBytes(Path.of(filePath));
            } catch (IOException e) {}
        }
        return null;
    }
    public static String LetUserChooseFile(Component parent, String approveButtonText, String StartDirectory) {
        JFileChooser FileChooser = new JFileChooser();
        if (StartDirectory != null) { FileChooser.setCurrentDirectory(new File(StartDirectory)); }
        FileChooser.showDialog(null, approveButtonText);
        File file = FileChooser.getSelectedFile();
        if (file != null) { return file.getAbsolutePath(); }
        return null;
    }
}
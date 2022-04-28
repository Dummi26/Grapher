package com.mark.input;

import com.mark.Main;
import com.mark.search.SearchInGraphWindow;
import com.mark.graph.graphLoader;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public final class KeyboardHandler implements KeyListener {
    // Shift < Ctrl < Alt (1<2<4)
    enum Command {
        Save,
    }
    public KeyboardHandler() {
        //AddShortcuts(new String[] {"CS", "Cs"}, Command.Save);
    }
    private HashMap<String, Command> Shortcuts = new HashMap<>();

    public Command GetCommand(String Shortcut) {
        return Shortcuts.get(Shortcut);
    }
    public int AddShortcuts(String[] Shortcuts, Command Command) {
        int Count = 0;
        for (String Shortcut : Shortcuts) {
            Count += AddShortcuts(Shortcut, Command);
        }
        return Count;
    }
    /**
     * @param Shortcuts They key combinations to trigger the shortcut. The last char of the string is the key, it may be prefixed with C (ctrl), S (shift) or A (alt). Uppercase: Key must be depressed, Lowercase: Key may not be depressed, None: Key may be depressed, but does not have to be.
     * @param Command The command to be triggered
     * @return How many commands were added.
     */
    public int AddShortcuts(String Shortcuts, Command Command) {
        char Key = Shortcuts.charAt(Shortcuts.length() - 1);
        boolean Ctrl1 = Shortcuts.contains("C");
        boolean Ctrl0 = Shortcuts.contains("c");
        boolean Shift1 = Shortcuts.contains("S");
        boolean Shift0 = Shortcuts.contains("s");
        boolean Alt1 = Shortcuts.contains("A");
        boolean Alt0 = Shortcuts.contains("a");
        //
        int count = 0;
        for (int index = 0; index < 8; index++) {
            boolean ShiftDown = (index & 1) == 1;
            boolean CtrlDown = (index & 2) == 2;
            boolean AltDown = (index & 4) == 4;
            // Shift doesn't matter ||      Shift up          || Shift down
            if (((Shift0 == Shift1) || (Shift0 && !ShiftDown) || (Shift1 && ShiftDown))
            && ((Ctrl0 == Ctrl1) || (Ctrl0 && !CtrlDown) || (Ctrl1 && CtrlDown))
            && ((Alt0 == Alt1) || (Alt0 && !AltDown) || (Alt1 && AltDown))) {
                AddShortcut(GetValue(ShiftDown, CtrlDown, AltDown, Key), Command);
                count++;
            }
        }
        return count;
    }

    public static String GetValue(boolean Shift, boolean Ctrl, boolean Alt, char Key) {
        return ((Alt ? 4 : 0) + (Ctrl ? 2 : 0) + (Shift ? 1 : 0)) + "" + Key;
    }

    /**
     * @param Shortcut The key combination to trigger the shortcut
     * @param Command The command to be triggered
     * @return True if an old shortcut was replaced
     */
    public void AddShortcut(String Shortcut, Command Command) {
        Shortcuts.put(Shortcut, Command);
    }
    public boolean RemoveShortcut(String Shortcut, Command Command) {
        return Shortcuts.remove(Shortcut, Command);
    }
    @Override public void keyTyped(KeyEvent e) {
        boolean Ctrl = e.isControlDown();
        boolean Shift = e.isShiftDown();
        boolean Alt = e.isAltDown();
        System.out.println(e.getKeyChar());
        switch (e.getKeyChar()) {
            case '\u0013' /* CTRL S */ -> {
                InformationWindowDisplayer.display(Information.GetDefault("Saved to path:\n" + Main.graph.SaveToPath, Information.DefaultType.Saved));
                graphLoader.toFile(Main.graph);
            }
            case '\u0006' /* CTRL F */ -> { new SearchInGraphWindow(Main.graph); }
            case '\u0012' /* CTRL R */ -> {}
            case '\u0011' /* CTRL Q */ -> {}
            case '\u0003' /* CTRL C */ -> {}
        }
        /*
        String KeyEventString = GetValue(Shift, Ctrl, Alt, e.getKeyChar());
        System.out.println("Key typed: " + KeyEventString);
        Command command = GetCommand(KeyEventString);
        if (command != null) {
            switch (command) {
                case Save -> graphLoader.toFile(Main.graph);
            }
        }
         */
    }

    @Override public void keyPressed(KeyEvent e) {
    }

    @Override public void keyReleased(KeyEvent e) {
    }
}
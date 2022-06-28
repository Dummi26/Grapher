package com.mark.graph;

import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.*;

public class gpLayoutArea extends graphPart {
    gpLayoutArea(graph parent, graphPart container) { super(parent, container, gpIdentifiers.LayoutArea); PanelLayoutMode(PanelLayoutModes.Flow); }

    private PanelLayoutModes PanelLayoutMode = PanelLayoutModes.Flow;
    public PanelLayoutModes PanelLayoutMode() { return PanelLayoutMode; }
    public void PanelLayoutMode(PanelLayoutModes mode) { PanelLayoutMode = mode; Layout = GetLayout(); }
    private gpLayoutArea_Layout Layout;

    @Override public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "LayoutMode" -> {
                try {
                    PanelLayoutMode(PanelLayoutModes.valueOf(value));
                }
                catch (IllegalArgumentException e) {
                    InformationWindowDisplayer.display(Information.GetDefault("Layout mode '" + value + "' is not valid for " + gpIdentifier().toString() + ".", Information.DefaultType.Error_Minor));
                }
            }
        }
    }

    @Override public String[] customFileSave() {
        return new String[] {
                "LayoutMode:" + PanelLayoutMode.toString(),
        };
    }

    @Override void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
        // Change position and size of the contents according to PanelLayoutMode
        Layout.performLayout();
    }

    @Override protected String customToString() {
        return PanelLayoutMode.toString();
    }

    public static enum PanelLayoutModes {
        Flow,
        FlowAutoSize,
    }

    private static gpLayoutArea_Layout GetLayout(PanelLayoutModes mode, graphPart gp) {
        switch (mode) {
            case Flow -> { return new gpLayoutArea_Layout_FlowStaticSizeEffectiveSizes(gp); }
            case FlowAutoSize -> { return new gpLayoutArea_Layout_FlowAutoSize(gp); }
            default -> { return null; }
        }
    }
    private gpLayoutArea_Layout GetLayout() { return GetLayout(PanelLayoutMode); }
    private gpLayoutArea_Layout GetLayout(PanelLayoutModes mode) { return GetLayout(mode, this); }
}

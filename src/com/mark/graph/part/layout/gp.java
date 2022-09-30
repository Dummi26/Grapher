package com.mark.graph.part.layout;

import com.mark.graph.Graph;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.graphPart;
import com.mark.input.CustomInputInfoContainer;
import com.mark.notification.Information;
import com.mark.notification.InformationWindowDisplayer;

import java.awt.*;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.LayoutArea); PanelLayoutMode(PanelLayoutModes.Flow); createInformationCategory(); createInformationCategory(); }

    private PanelLayoutModes PanelLayoutMode = PanelLayoutModes.Flow;
    public PanelLayoutModes PanelLayoutMode() { return PanelLayoutMode; }
    public void PanelLayoutMode(PanelLayoutModes mode) { PanelLayoutMode = mode; Layout = GetLayout(); }
    private com.mark.graph.part.layout.Layout Layout = null;

    @Override public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "LayoutMode" -> {
                try {
                    PanelLayoutMode(PanelLayoutModes.valueOf(value));
                    while (getInformationsSize(0) > 0) { removeInformation(0, 0); }
                }
                catch (IllegalArgumentException e) {
                    if (getInformationsSize(0) == 0) { addStaticInformation(0, Information.GetDefault("Layout mode '" + value + "' is not valid for " + gpIdentifier().toString() + ".", Information.DefaultType.Error_Minor)); }
                }
            }
            case "LayoutAdjust" -> {
                if (Layout != null) {
                    String err = Layout.readAdjustString(value);
                    if (err == null) {
                        while (getInformationsSize(1) > 0) { removeInformation(1, 0); }
                    } else {
                        if (getInformationsSize(1) == 0) { addStaticInformation(1, Information.GetDefault(err, Information.DefaultType.Error_Medium)); }
                    }
                }
            }
        }
    }

    @Override public String[] customFileSave() {
        return new String[] {
                "LayoutMode:" + PanelLayoutMode.toString(),
                "LayoutAdjust:" + Layout.getAdjustString(),
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        // Change position and size of the contents according to PanelLayoutMode
        Layout.performLayout();
    }

    @Override protected String customToString() {
        return PanelLayoutMode.toString();
    }

    public static enum PanelLayoutModes {
        Flow,
        FlowAutoSize,
        Table,
    }

    private static com.mark.graph.part.layout.Layout GetLayout(PanelLayoutModes mode, graphPart gp) {
        return switch (mode) {
            case Flow -> new Layout_FlowStaticSizeEffectiveSizes(gp);
            case FlowAutoSize -> new Layout_FlowAutoSize(gp);
            case Table -> new Layout_Table(gp);
        };
    }
    private com.mark.graph.part.layout.Layout GetLayout() { return GetLayout(PanelLayoutMode); }
    private com.mark.graph.part.layout.Layout GetLayout(PanelLayoutModes mode) { return GetLayout(mode, this); }

    @Override
    protected void wasRemoved() {
    }
    @Override public CustomInputInfoContainer customUserInput() { return null; }
}

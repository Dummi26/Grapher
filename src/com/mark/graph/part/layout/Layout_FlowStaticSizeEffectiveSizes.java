package com.mark.graph.part.layout;

import com.mark.graph.graphPart;

public class Layout_FlowStaticSizeEffectiveSizes extends Layout {
    public Layout_FlowStaticSizeEffectiveSizes(graphPart panel) { super(panel); }
    @Override public void performLayout() {
        double MaxHeight = 0;
        double PosX = 0;
        double PosY = 0;
        for (graphPart p : panel.contents) {
            double h = p.getActualH();
            double w = p.getActualW();
            double totalWidth = PosX + w;
            if (totalWidth > 100) {
                // go to new line
                PosX = 0;
                PosY += MaxHeight;
                if (PosY > 100) PosY = 0;
                MaxHeight = 0;
            }
            if (h > MaxHeight) MaxHeight = h;

            p.X(PosX-p.getActualEmptySpaceX());
            p.Y(PosY-p.getActualEmptySpaceY());

            PosX += w;
        }
    }
}

package com.mark.graph.part.layout;

import com.mark.graph.graphPart;

public class gpLayoutArea_Layout_FlowAutoSize extends gpLayoutArea_Layout {
    public gpLayoutArea_Layout_FlowAutoSize(graphPart panel) { super(panel); }
    @Override public void performLayout() { // TODO: WHAT
        if (panel.contents.length == 0) return;
        double Dimensions = 100 / Math.sqrt(panel.contents.length); // 4 --> 100/2 = 50x50
        {
            double MaxHeight = 0;
            double PosX = 0;
            double PosY = 0;
            for (graphPart p : panel.contents) {
                double h = Dimensions;
                double w = Dimensions;
                p.H(Dimensions);
                p.W(Dimensions);
                double totalWidth = PosX + w;
                if (totalWidth > 100) {
                    // go to new line
                    PosX = 0;
                    PosY += MaxHeight;
                    if (PosY > 100) PosY = 0;
                }
                if (h > MaxHeight) MaxHeight = h;

                p.X(PosX);
                p.Y(PosY);

                PosX += w;
            }
        }
    }
}

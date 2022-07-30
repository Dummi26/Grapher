package com.mark.graph.part.layout;

import com.mark.graph.graphPart;

public abstract class gpLayoutArea_Layout {
    protected graphPart panel;
    public gpLayoutArea_Layout(graphPart panel) {
        this.panel = panel;
    }
    public abstract void performLayout();
}

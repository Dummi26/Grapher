package com.mark.graph.part.layout;

import com.mark.graph.graphPart;

public abstract class Layout {
    protected graphPart panel;
    public Layout(graphPart panel) {
        this.panel = panel;
    }
    public String getAdjustString() { return ""; }
    /**If this returns a non-null value, an error has occurred.*/
    public String readAdjustString(String input) { return null; }
    public abstract void performLayout();
}

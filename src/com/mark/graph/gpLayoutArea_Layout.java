package com.mark.graph;

public abstract class gpLayoutArea_Layout {
    protected graphPart panel;
    public gpLayoutArea_Layout(graphPart panel) {
        this.panel = panel;
    }
    public abstract void performLayout();
}
package com.mark.input;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CustomInputMetadata {
    public MouseEvent mouseEvent;

    public CustomInputMetadata(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
}

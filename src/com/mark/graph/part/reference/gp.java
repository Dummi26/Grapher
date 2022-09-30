package com.mark.graph.part.reference;

import com.mark.Main;
import com.mark.graph.*;
import com.mark.input.CustomInputInfo;
import com.mark.input.CustomInputInfoContainer;
import com.mark.input.CustomInputMetadata;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Consumer;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.Reference); }

    private String ReferenceID = "";
    private LocalDateTime ReferenceLastUpdated = null;
    private graphPart ReferencedGP = null;
    private graphPart CopyOfRefGP = null;

    @Override public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "ReferenceID" -> {
                ReferenceID = value;
                ReferencedGP = null;
                CopyOfRefGP = null;
                ReferenceLastUpdated = null;
            }
        }
    }

    @Override public String[] customFileSave() {
        return new String[] {
                "ReferenceID:" + ReferenceID
        };
    }

    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions) {
        if (ReferenceID != null) {
            if (ReferencedGP == null || !ReferenceID.equals(ReferencedGP.ID())) {
                ReferencedGP = parent.GetGraphPartWithID(ReferenceID);
            }
            if (ReferencedGP != null) {
                if (CopyOfRefGP == null || ReferenceLastUpdated == null || ReferencedGP.LastUpdatedTime().isAfter(ReferenceLastUpdated)) {
                    ReferenceLastUpdated = ReferencedGP.LastUpdatedTime();
                    graphPartAndOutInfo info = graphLoader.fromString(ReferencedGP.fileSave(), 0, parent, this);
                    if (info != null && info.graphPart != null) {
                        CopyOfRefGP = info.graphPart;
                        CopyOfRefGP.X(0);
                        CopyOfRefGP.Y(0);
                        CopyOfRefGP.W(100);
                        CopyOfRefGP.H(100);
                    }
                }
                if (CopyOfRefGP != null) { // DON'T MAKE THIS AN ELSE, IT WILL PREVENT THE DRAWING THE FIRST TIME
                    CopyOfRefGP.draw(Img, x, y, w, h, ImgW, ImgH, blockThreadedActions);
                }
            }
        }
    }

    @Override protected String customToString() {
        return ReferenceID;
    }

    @Override
    protected void wasRemoved() {
    }
    @Override public CustomInputInfoContainer customUserInput() {
        var infos = new ArrayList<CustomInputInfo>();
        if (ReferencedGP != null) {
            infos.add(new CustomInputInfo("Go to", meta -> {
                if (ReferencedGP != null) {
                    switch (meta.mouseEvent.getButton()) {
                        case MouseEvent.BUTTON1 -> Main.Render.focusOnRectangle(ReferencedGP.getArea(), false);
                        case MouseEvent.BUTTON2 -> {
                            Main.Render.focusOnRectangle(ReferencedGP.getArea(), false);
                        }
                        case MouseEvent.BUTTON3 -> Main.Render.focusOnRectangle(ReferencedGP.getArea(), true);
                    }
                }
            }));
        }
        if (infos.size() > 0) {
            return new CustomInputInfoContainer(infos);
        } else {
            return null;
        }
    }
}

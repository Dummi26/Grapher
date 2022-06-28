package com.mark.graph;

import java.awt.*;
import java.time.LocalDateTime;

public class gpReference extends graphPart {
    public gpReference(graph parent, graphPart container) { super(parent, container, gpIdentifiers.Reference); }

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

    @Override void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
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
                    CopyOfRefGP.draw(Img, x, y, w, h, ImgW, ImgH);
                }
            }
        }
    }

    @Override protected String customToString() {
        return ReferenceID;
    }
}

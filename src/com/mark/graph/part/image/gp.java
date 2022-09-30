package com.mark.graph.part.image;

import com.mark.Main;
import com.mark.graph.gpIdentifiers;
import com.mark.graph.Graph;
import com.mark.graph.graphPart;
import com.mark.graph.graphPartDrawInfo;
import com.mark.input.CustomInputInfoContainer;
import com.mark.notification.Information;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.time.Duration;
import java.time.LocalDateTime;

public class gp extends graphPart {
    public gp(Graph parent, graphPart container) { super(parent, container, gpIdentifiers.Image); createInformationCategory(); createInformationCategory(); } // try { ImageOriginal = ImageIO.read(new ByteArrayInputStream(Files.readAllBytes(Path.of("C:\\Users\\Markb\\Desktop\\img.jpg"))));} catch (Exception e) {}

    Alignment alignment = Alignment.MiddleCenter;
    Scaling scaling = Scaling.Fit;
    String ImageSource = "";
    enum Alignment {
        TopLeft,
        TopCenter,
        TopRight,
        MiddleLeft,
        MiddleCenter,
        MiddleRight,
        BottomLeft,
        BottomCenter,
        BottomRight,
    }
    enum Scaling {
        Fit,
        Fill,
        Stretch,
    }
    double CropL = 0; // how much of the image to crop away from the edges.
    double CropR = 0;
    double CropT = 0;
    double CropB = 0;

    private BufferedImage ImageOriginal = null;
    private BufferedImage ImageCropped = null;
    private BufferedImage ImageScaled = null;

    @Override
    public void customFileLoadLine(String identifier, String value) {
        switch (identifier) {
            case "Alignment" -> {
                try { alignment = Alignment.valueOf(value); } catch (IllegalArgumentException e) {}
            }
            case "Scaling" -> {
                try { scaling = Scaling.valueOf(value); } catch (IllegalArgumentException e) {}
            }
            case "Image" -> {
                if (!ImageSource.equals(value)) {
                    ImageSource = value;
                    LoaderTask = new gpImage__ImageLoader(value, parent);
                    LoaderTask.start();
                    ImageOriginal = null;
                    ImageScaled = null;
                }
            }
            case "ImageCrop" -> {
                String[] crop = value.split(" ");
                if (crop.length >= 4) {
                    try {
                        double d1 = Double.parseDouble(crop[0]);
                        double d2 = Double.parseDouble(crop[1]);
                        double d3 = Double.parseDouble(crop[2]);
                        double d4 = Double.parseDouble(crop[3]);
                        CropL = d1;
                        CropR = d2;
                        CropT = d3;
                        CropB = d4;
                        ImageCropped = null;
                    } catch (NumberFormatException ex) {}
                }
            }
        }
    }
    @Override
    public String[] customFileSave() {
        return new String[] {
                "Alignment:" + alignment.toString(),
                "Scaling:" + scaling.toString(),
                "Image:" + ImageSource,
                "ImageCrop:" + CropL + " " + CropR + " " + CropT + " " + CropB,
        };
    }

    private gpImage__ImageLoader LoaderTask = null;
    private gpImage__ImageScaler ScalerTask = null;
    private LocalDateTime ScalerTaskNeedsAccurateRerun = null;
    @Override
    protected void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH, boolean blockThreadedActions, graphPartDrawInfo info) {
        //if (info.reference_depth > 10) { return; }
        if (ImageOriginal == null && LoaderTask != null) {
            if (LoaderTask.isAlive()) {
                if (getInformationsSize(1) == 0) { addStaticInformation(1, Information.GetDefault("Loading image...", Information.DefaultType.Information_Short)); }
                while (getInformationsSize(0) > 0) { removeInformation(0, 0); }
                if (blockThreadedActions) { try { LoaderTask.join(); } catch (InterruptedException e) {} }
            } else {
                while (getInformationsSize(1) > 0) { removeInformation(1, 0); }
                if (LoaderTask.error == null) {
                    while (getInformationsSize(0) > 0) { removeInformation(0, 0); }
                    ImageOriginal = LoaderTask.Output;
                } else {
                    if (getInformationsSize(0) == 0) { addStaticInformation(0, Information.GetDefault("", Information.DefaultType.Error_Medium)); }
                    getInformation(0, 0).Information = LoaderTask.error;
                }
                LoaderTask = null;
            }
        }
        if (ImageOriginal != null && w > 0 && h > 0) {
            int W = w;
            int H = h;
            // crop the image
            if (ImageCropped == null) {
                int IOW = ImageOriginal.getWidth();
                int IOH = ImageOriginal.getHeight();
                double AbsCropW1 = CropL * IOW;
                double AbsCropH1 = CropT * IOH;
                double AbsCropW2 = CropR * IOW;
                double AbsCropH2 = CropB * IOH;
                int AfterCropX = (int)AbsCropW1;
                int AfterCropY = (int)AbsCropH1;
                int AfterCropW = (int)Math.ceil(IOW - AbsCropW1 - AbsCropW2);
                int AfterCropH = (int)Math.ceil(IOH - AbsCropH1 - AbsCropH2);
                AfterCropX = Math.min(Math.max(AfterCropX, 0), IOW - 1);
                AfterCropY = Math.min(Math.max(AfterCropY, 0), IOH - 1);
                AfterCropW = Math.min(Math.max(AfterCropW, 1), IOW);
                AfterCropH = Math.min(Math.max(AfterCropH, 1), IOH);
                if (AfterCropX + AfterCropW > IOW) {
                    double Factor = (double)(AfterCropX + AfterCropW) / IOW;
                    AfterCropX *= Factor;
                    AfterCropW *= Factor;
                }
                if (AfterCropY + AfterCropH > IOH) {
                    double Factor = (double)(AfterCropY + AfterCropH) / IOH;
                    AfterCropY *= Factor;
                    AfterCropH *= Factor;
                }
                ImageCropped = ImageOriginal.getSubimage(AfterCropX, AfterCropY, AfterCropW, AfterCropH);
            }
            // Figure out how to scale the image based on scaling mode
            {
                double ScalingW = (double) w / ImageCropped.getWidth();
                double ScalingH = (double) h / ImageCropped.getHeight();
                switch (scaling) {
                    case Fit -> ScalingW = ScalingH = Math.min(ScalingW, ScalingH);
                    case Fill -> ScalingW = ScalingH = Math.max(ScalingW, ScalingH);
                }
                W = (int)Math.ceil(ImageCropped.getWidth() * ScalingW);
                H = (int)Math.ceil(ImageCropped.getHeight() * ScalingH);
            }
            // scaler task init (and waiting, if blocking)
            if (ImageScaled == null || ImageScaled.getWidth() != W || ImageScaled.getHeight() != H) {
                if (ScalerTask == null) {
                    ScalerTask = new gpImage__ImageScaler(ImageCropped, blockThreadedActions ? null : null/*Duration.ofNanos(1000000000/100)*/, W, H); // TODO: Make time-limited image scaling so good that i can actually use it (when resizing, fast scaling, after a certain timeout (900ms in this case, call the slow resizer to make things look good - null = slow upscaler)
                    ScalerTask.start();
                    if (blockThreadedActions) {
                        try { ScalerTask.join(); } catch (InterruptedException e) {}
                    } else {
                    }
                }
            }
            // get scaled image from task
            if (ScalerTask != null && !ScalerTask.isAlive()) {
                ImageScaled = ScalerTask.Output;
                ScalerTaskNeedsAccurateRerun = ScalerTask.cutoffTime == null ? null : LocalDateTime.now().plus(Duration.ofMillis(900));
                ScalerTask = null;
            }
            if (ScalerTaskNeedsAccurateRerun != null && ScalerTaskNeedsAccurateRerun.isBefore(LocalDateTime.now())) {
                ScalerTask = new gpImage__ImageScaler(ImageCropped, null, W, H);
                ScalerTask.start();
            }
            //
            if (ImageScaled != null) {
                // EffectiveXYWH
                EffectiveW = (double)ImageScaled.getWidth() / w;
                EffectiveH = (double)ImageScaled.getHeight() / h;
                switch (alignment) {
                    case TopLeft, TopCenter, TopRight -> EffectiveY = 0;
                    case MiddleLeft, MiddleCenter, MiddleRight -> EffectiveY = (1 - EffectiveH) / 2;
                    case BottomLeft, BottomCenter, BottomRight -> EffectiveY = 1 - EffectiveH;
                }
                switch (alignment) {
                    case TopLeft, MiddleLeft, BottomLeft -> EffectiveX = 0;
                    case TopCenter, MiddleCenter, BottomCenter -> EffectiveX = (1 - EffectiveW) / 2;
                    case TopRight, MiddleRight, BottomRight -> EffectiveX = 1 - EffectiveW;
                }
                // Draw
                int ox = 0;
                int oy = 0;
                BufferedImage ImageDraw = ImageScaled;
                int CutoffX = Math.max(0, ImageDraw.getWidth() - w);
                int CutoffY = Math.max(0, ImageDraw.getHeight() - h);
                if (CutoffX > 0 || CutoffY > 0) {
                    ImageDraw = ImageDraw.getSubimage(CutoffX/2, CutoffY/2, ImageDraw.getWidth() - CutoffX, ImageDraw.getHeight() - CutoffY);
                }
                if (alignment == Alignment.MiddleLeft || alignment == Alignment.MiddleCenter || alignment == Alignment.MiddleRight) oy = (h - ImageDraw.getHeight()) / 2;
                if (alignment == Alignment.BottomLeft || alignment == Alignment.BottomCenter || alignment == Alignment.BottomRight) oy = h - ImageDraw.getHeight();
                if (alignment == Alignment.TopCenter || alignment == Alignment.MiddleCenter || alignment == Alignment.BottomCenter) ox = (w - ImageDraw.getWidth()) / 2;
                if (alignment == Alignment.TopRight || alignment == Alignment.MiddleRight || alignment == Alignment.BottomRight) ox = w - ImageDraw.getWidth();
                Img.drawImage(ImageDraw, x + ox, y + oy, null);
            }
        }
    }

    @Override protected String customToString() {
        return "Image: " + ImageSource;
    }

    @Override
    protected void wasRemoved() {
    }
    @Override public CustomInputInfoContainer customUserInput() { return null; }
}

class gpImage__ImageScaler extends Thread {
    private BufferedImage Source;
    public Duration cutoffTime;
    private int w;
    private int h;
    public BufferedImage Output;
    public gpImage__ImageScaler(BufferedImage Source, Duration cutoffTime, int w, int h) {
        this.Source = Source;
        this.cutoffTime = cutoffTime;
        this.w = w;
        this.h = h;
    }

    @Override
    public void run() {
        if (cutoffTime == null) {
            AffineTransform at = new AffineTransform();
            at.setToScale((double) w / Source.getWidth(), (double) h / Source.getHeight());
            AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
            Output = ato.filter(Source, new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
            Main.updateScreen = true;
        } else {
            LocalDateTime endTime = LocalDateTime.now().plus(cutoffTime);
            int W = Source.getWidth();
            int H = Source.getHeight();
            var InBuf = Source.getRaster().getDataBuffer();
            Output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            var OutBuf = Output.getRaster().getDataBuffer();
            int iterations = 16;
            for (int iteration_y = 0; iteration_y < iterations; iteration_y++) {
                for (int iteration_x = 0; iteration_x < iterations; iteration_x++) {
                    for (int y = iteration_y; y < h; y += iterations) {
                        var line_out = w * y;
                        var x_out = line_out;
                        for (int x = iteration_x; x < w; x += iterations) {
                            OutBuf.setElem(x_out + x, Source.getRGB(x * W / w, y * H / h));
                        }
                    }
                    if (endTime.isBefore(LocalDateTime.now())) break;
                }
            }
            Main.updateScreen = true;
        }
    }
}

class gpImage__ImageLoader extends Thread {
    private String Source;
    private graphPart Parent;
    public BufferedImage Output = null;
    public String error = null;
    public gpImage__ImageLoader(String Source, graphPart Parent) {
        this.Source = Source;
        this.Parent = Parent;
    }

    @Override
    public void run() {
        try {
            int id = Integer.parseInt(Source);
            if (id < 0) throw new NumberFormatException("id was less than 0");
            Output = ImageIO.read(new ByteArrayInputStream(Parent.parent.BytesInFileData.get(id)));
        }
        catch (Exception ex1) {
            try {
                File actualUsedFile = null;
                String SourceAssumingLocalPath = new File(Parent.parent.SaveToPath()).getParent();
                if (SourceAssumingLocalPath != null) {
                    actualUsedFile = new File(SourceAssumingLocalPath + File.separator + Source);
                    if (!actualUsedFile.exists()) { actualUsedFile = null; }
                }
                if (actualUsedFile == null) {
                    File fileAbs = new File(Source);
                    if (!fileAbs.exists()) throw new InvalidPathException(Source, "File does not exist on this machine.");
                    actualUsedFile = fileAbs;
                }
                Output = ImageIO.read(actualUsedFile);
            }
            catch (Exception ex2) {
                try {
                    Output = ImageIO.read(new URL(Source).openStream());
                }
                catch (Exception ex3) {
                    error = "Error loading image from source:\n" + Source;
                }
            }
        }
        Main.updateScreen = true;
    }
}

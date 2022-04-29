package com.mark.graph;

import com.mark.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;

public class gpImage extends graphPart {
    public gpImage(graph parent, graphPart container) { super(parent, container, gpIdentifiers.Image); } // try { ImageOriginal = ImageIO.read(new ByteArrayInputStream(Files.readAllBytes(Path.of("C:\\Users\\Markb\\Desktop\\img.jpg"))));} catch (Exception e) {}

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
    public void customFileLoad(String identifier, String value) {
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
    @Override
    void customDraw(Graphics2D Img, int x, int y, int w, int h, int ImgW, int ImgH) {
        if (ImageOriginal == null && LoaderTask != null && !LoaderTask.isAlive()) {
            ImageOriginal = LoaderTask.Output;
            LoaderTask = null;
        }
        if (ImageOriginal != null && w > 0 && h > 0) {
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
            if (ScalerTask != null && !ScalerTask.isAlive()) {
                ImageScaled = ScalerTask.Output;
                ScalerTask = null;
            }
            int W = w;
            int H = h;
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
            if (ImageScaled == null || ImageScaled.getWidth() != W || ImageScaled.getHeight() != H) {
                if (ScalerTask == null) {
                    ScalerTask = new gpImage__ImageScaler(ImageCropped, W, H);
                    ScalerTask.start();
                }
            }
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

    @Override public String toString() {
        return "Image: " + ImageSource;
    }
}

class gpImage__ImageScaler extends Thread {
    private BufferedImage Source;
    private int w;
    private int h;
    public BufferedImage Output;
    public gpImage__ImageScaler(BufferedImage Source, int w, int h) {
        this.Source = Source;
        this.w = w;
        this.h = h;
    }

    @Override
    public void run() {
        AffineTransform at = new AffineTransform();
        at.setToScale((double)w / Source.getWidth(), (double)h / Source.getHeight());
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        Output = ato.filter(Source, new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        Main.updateScreen = true;
    }
}

class gpImage__ImageLoader extends Thread {
    private String Source;
    private graph Parent;
    public BufferedImage Output;
    public gpImage__ImageLoader(String Source, graph Parent) {
        this.Source = Source;
        this.Parent = Parent;
    }

    @Override
    public void run() {
        try {
            int id = Integer.parseInt(Source);
            if (id < 0) throw new NumberFormatException("id was less than 0");
            Output = ImageIO.read(new ByteArrayInputStream(Parent.BytesInFileData.get(id)));
        }
        catch (Exception ex1) {
            try {
                File actualUsedFile = null;
                String SourceAssumingLocalPath = new File(Parent.SaveToPath()).getParent();
                if (SourceAssumingLocalPath != null) {
                    actualUsedFile = new File(SourceAssumingLocalPath + File.separator + Source);
                    if (!actualUsedFile.exists()) { actualUsedFile = null; }
                }
                if (actualUsedFile == null) {
                    File fileAbs = new File(Source);
                    if (!fileAbs.exists()) throw new InvalidPathException(Source, "File does not exist on this machine.");
                }
                Output = ImageIO.read(actualUsedFile);
            }
            catch (Exception ex2) {
                try {
                    Output = ImageIO.read(new URL(Source).openStream());
                }
                catch (Exception ex3) {
                    System.out.println("Loading image failed:");
                    ex1.printStackTrace();
                    ex2.printStackTrace();
                    ex3.printStackTrace();
                }
            }
        }
        Main.updateScreen = true;
    }
}
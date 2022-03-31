package com.mark;

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

    private BufferedImage ImageOriginal = null;
    private BufferedImage ImageScaled = null;

    @Override
    protected void customFileLoad(String identifier, String value) {
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
        }
    }
    @Override
    protected String[] customFileSave() {
        return new String[] {
                "Alignment:" + alignment.toString(),
                "Scaling:" + scaling.toString(),
                "Image:" + ImageSource,
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
            if (ScalerTask != null && !ScalerTask.isAlive()) {
                ImageScaled = ScalerTask.Output;
                ScalerTask = null;
            }
            int W = w;
            int H = h;
            {
                double ScalingW = (double) w / ImageOriginal.getWidth();
                double ScalingH = (double) h / ImageOriginal.getHeight();
                switch (scaling) {
                    case Fit -> {
                        ScalingW = ScalingH = Math.min(ScalingW, ScalingH);
                    }
                    case Fill -> {
                        ScalingW = ScalingH = Math.max(ScalingW, ScalingH);
                    }
                }
                W = (int)(1 + ImageOriginal.getWidth() * ScalingW);
                H = (int)(1 + ImageOriginal.getHeight() * ScalingH);
            }
            if (ImageScaled == null || ImageScaled.getWidth() != W || ImageScaled.getHeight() != H) {
                if (ScalerTask == null) {
                    ScalerTask = new gpImage__ImageScaler(ImageOriginal, W, H);
                    ScalerTask.start();
                }
            }
            if (ImageScaled != null) {
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
                File fileAbs = new File(Source);
                if (!fileAbs.exists()) throw new InvalidPathException(Source, "File does not exist on this machine.");
                Output = ImageIO.read(fileAbs);
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
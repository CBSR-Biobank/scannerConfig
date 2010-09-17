package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import edu.ualberta.med.scannerconfig.ScannerRegion;
import edu.ualberta.med.scannerconfig.ScannerRegion.Orientation;

public class GridRegion {

    // pixel coordinates
    private double left, top, width, height;

    private double gapOffsetX, gapOffsetY;

    private Orientation orientation;

    public String name;

    private Point oldCanvasSize;

    private Canvas canvas;

    public GridRegion(ScannerRegion r, Canvas canvas) {
        this.canvas = canvas;

        double canvasWidth = canvas.getBounds().width;
        double canvasHeight = canvas.getBounds().height;

        name = r.name;

        gapOffsetX = r.gapX / regionToPixelWidth(canvasWidth);
        gapOffsetY = r.gapY / regionToPixelHeight(canvasHeight);
        left = r.left / regionToPixelWidth(canvasWidth);
        top = r.top / regionToPixelHeight(canvasHeight);
        width = (r.right - r.left) / regionToPixelWidth(canvasWidth);
        height = (r.bottom - r.top) / regionToPixelHeight(canvasHeight);
        orientation = r.orientation;

        adjustBounds();

        oldCanvasSize = canvas.getSize();
    }

    private void adjustBounds() {
        if (width < 50) {
            width = 50;
        }
        if (height < 50) {
            height = 50;
        }
        if (left < 0) {
            left = 0;
        }
        if (top < 0) {
            top = 0;
        }
        if (left + width > canvas.getSize().x - 1) {
            left = canvas.getSize().x - width - 1;
        }
        if (top + height > canvas.getSize().y - 1) {
            top = canvas.getSize().y - height - 1;
        }
    }

    public double getGapOffsetX() {
        return gapOffsetX;
    }

    public double getGapOffsetY() {
        return gapOffsetY;
    }

    public double getHeight() {
        return height;
    }

    public double getLeft() {
        return left;
    }

    public Rectangle getRectangle() {
        return new Rectangle((int) left, (int) top, (int) width, (int) height);
    }

    public ScannerRegion getScannerRegion() {

        ScannerRegion r = new ScannerRegion();

        double canvasWidth = canvas.getBounds().width;
        double canvasHeight = canvas.getBounds().height;

        adjustBounds();

        r.name = name;

        r.left = left * regionToPixelWidth(canvasWidth);
        r.top = top * regionToPixelHeight(canvasHeight);
        r.right = (width + left) * regionToPixelWidth(canvasWidth);
        r.bottom = (height + top) * regionToPixelHeight(canvasHeight);
        r.gapX = gapOffsetX * regionToPixelWidth(canvasWidth);
        r.gapY = gapOffsetY * regionToPixelHeight(canvasHeight);
        r.orientation = orientation;

        return r;
    }

    public double getTop() {
        return top;
    }

    public double getWidth() {
        return width;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        double t = height;
        height = width;
        width = t;
        adjustBounds();
    }

    public double regionToPixelHeight(double canvasHeight) {
        PlateScannedImage plateImage = PlateScannedImage.instance();
        Assert.isTrue(plateImage.exists());
        return (plateImage.getScannedImage().getBounds().height / (canvasHeight * PlateScannedImage.PALLET_IMAGE_DPI));
    }

    public double regionToPixelWidth(double canvasWidth) {
        PlateScannedImage plateImage = PlateScannedImage.instance();
        Assert.isTrue(plateImage.exists());
        return (plateImage.getScannedImage().getBounds().width / (canvasWidth * PlateScannedImage.PALLET_IMAGE_DPI));
    }

    /*
     * left,top,right,bottom are relative to the canvas size, so any change to
     * the canvas must call this function.
     */
    public void scaleGrid(Point newCanvasSize) {

        double horiztonalRatio = regionToPixelWidth(oldCanvasSize.x)
            / regionToPixelWidth(newCanvasSize.x);

        double verticalRatio = regionToPixelHeight(oldCanvasSize.y)
            / regionToPixelHeight(newCanvasSize.y);

        left *= horiztonalRatio;
        top *= verticalRatio;
        width *= horiztonalRatio;
        height *= verticalRatio;
        gapOffsetX *= horiztonalRatio;
        gapOffsetY *= verticalRatio;
        oldCanvasSize = newCanvasSize;
    }

    public void setGapOffsetX(double gap) {
        gapOffsetX = gap;
        if (gapOffsetX < 0)
            gapOffsetX = 0;
        double w = (getRectangle().width) / 12.0;
        if (w - gapOffsetX < 0.1) {
            gapOffsetX = w - 0.1;
        }
    }

    public void setGapOffsetY(double gap) {

        gapOffsetY = gap;

        if (gapOffsetY < 0)
            gapOffsetY = 0;

        double h = (getRectangle().height) / 8.0;
        if (h - gapOffsetY < 0.1) {
            gapOffsetY = h - 0.1;
        }
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
package edu.ualberta.med.scannerconfig;

public class ScannerRegion {
    public enum Orientation {
        HORIZONTAL, VERTICAL
    };

    public String name;

    public double left;

    public double top;

    public double right;

    public double bottom;

    public double gapX;

    public double gapY;

    public Orientation orientation;

    public ScannerRegion() {
        left = top = right = bottom = gapX = gapY = 0;
        orientation = Orientation.HORIZONTAL;
    }

    public ScannerRegion(ScannerRegion region) {
        this.set(region);
    }

    public ScannerRegion(String name, double left, double top, double right,
        double bottom, double gapX, double gapY, Orientation orientation) {
        set(name, left, top, right, bottom, gapX, gapY, orientation);
    }

    public void set(ScannerRegion region) {
        set(region.name, region.left, region.top, region.right, region.bottom,
            region.gapX, region.gapY, region.orientation);
    }

    public void set(String name, double left, double top, double right,
        double bottom, double gapX, double gapY, Orientation orientation) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.gapX = gapX;
        this.gapY = gapY;
        this.orientation = orientation;
    }
}

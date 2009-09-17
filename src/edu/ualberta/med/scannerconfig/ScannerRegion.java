package edu.ualberta.med.scannerconfig;

public class ScannerRegion {

    public String name;

    public double left;

    public double top;

    public double right;

    public double bottom;

    public ScannerRegion() {
        left = top = right = bottom = 0;
    }

    public ScannerRegion(ScannerRegion region) {
        name = region.name;
        left = region.left;
        top = region.top;
        right = region.right;
        bottom = region.bottom;
    }

    public ScannerRegion(String name, double left, double top, double right,
        double bottom) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(ScannerRegion region) {
        left = region.left;
        top = region.top;
        right = region.right;
        bottom = region.bottom;

    }

    public void set(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean equal(ScannerRegion region) {
        return ((left == region.left) && (top == region.top)
            && (right == region.right) && (bottom == region.bottom));
    }
}

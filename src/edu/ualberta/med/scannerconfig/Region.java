package edu.ualberta.med.scannerconfig;

public class Region {

    public String name;

    public double left;

    public double top;

    public double right;

    public double bottom;

    public Region() {
        left = top = right = bottom = 0;
    }

    public Region(String name, double left, double top, double right,
        double bottom) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}

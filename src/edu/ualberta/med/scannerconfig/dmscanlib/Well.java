package edu.ualberta.med.scannerconfig.dmscanlib;

public class Well {

    final String label;

    final double left;

    final double top;

    final double right;

    final double bottom;

    public Well(String label, double left, double top, double right,
        double bottom) {
        this.label = label;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public String getLabel() {
        return label;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

}

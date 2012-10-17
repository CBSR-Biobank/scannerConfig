package edu.ualberta.med.scannerconfig.dmscanlib;

public class Point {
    final double x;
    final double y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(x).append(",").append(y).append(")");
        return sb.toString();
    }

}

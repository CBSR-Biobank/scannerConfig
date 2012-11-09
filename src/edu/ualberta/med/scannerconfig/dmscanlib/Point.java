package edu.ualberta.med.scannerconfig.dmscanlib;

public final class Point {
    final double x;
    final double y;

    public Point(double x, double y) {
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

    public Point translate(Point point) {
        return new Point(x + point.x, y + point.y);
    }

    public Point scale(double factor) {
        return new Point(x * factor, y * factor);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(x).append(",").append(y).append(")");
        return sb.toString();
    }

}

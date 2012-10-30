package edu.ualberta.med.scannerconfig.dmscanlib;

public class Point {
    final int x;
    final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point translate(Point point) {
        return new Point(x + point.x, y + point.y);
    }

    public Point scale(int factor) {
        return new Point(x * factor, y * factor);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(x).append(",").append(y).append(")");
        return sb.toString();
    }

}

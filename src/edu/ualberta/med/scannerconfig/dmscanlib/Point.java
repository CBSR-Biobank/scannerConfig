package edu.ualberta.med.scannerconfig.dmscanlib;

public class Point<T> {
    private final T x;
    private final T y;

    Point(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

}

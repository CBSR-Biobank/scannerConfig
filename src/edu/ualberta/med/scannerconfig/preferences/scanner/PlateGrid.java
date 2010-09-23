package edu.ualberta.med.scannerconfig.preferences.scanner;

/**
 * Tracks the grid's attributes in number of pixels for the currently scanned
 * plate image.
 * 
 */
public class PlateGrid<T extends Number> {
    public enum Orientation {
        LANDSCAPE, PORTRAIT
    };

    private String name;

    private T left;

    private T top;

    private T width;

    private T height;

    private T gapX;

    private T gapY;

    private Orientation orientation;

    public PlateGrid() {
        left = top = width = height = gapX = gapY = null;
        orientation = Orientation.LANDSCAPE;
    }

    public PlateGrid(String name, T left, T top, T right, T bottom, T gapX,
        T gapY, Orientation orientation) {
        set(name, left, top, right, bottom, gapX, gapY, orientation);
    }

    public PlateGrid(PlateGrid<T> region) {
        set(region);
    }

    public void set(String name, T left, T top, T right, T bottom, T gapX,
        T gapY, Orientation orientation) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.width = right;
        this.height = bottom;
        this.gapX = gapX;
        this.gapY = gapY;
        this.orientation = orientation;
    }

    public void set(PlateGrid<T> region) {
        set(region.name, region.left, region.top, region.width, region.height,
            region.gapX, region.gapY, region.orientation);
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public T getTop() {
        return top;
    }

    public void setTop(T top) {
        this.top = top;
    }

    public T getWidth() {
        return width;
    }

    public void setWidth(T width) {
        this.width = width;
    }

    public T getHeight() {
        return height;
    }

    public void setHeight(T height) {
        this.height = height;
    }

    public T getGapX() {
        return gapX;
    }

    public void setGapX(T gapX) {
        this.gapX = gapX;
    }

    public T getGapY() {
        return gapY;
    }

    public void setGapY(T gapY) {
        this.gapY = gapY;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return "left/" + left + " top/" + top + " width/" + width + " height/"
            + height + " gapX/" + gapX + " gapY/" + gapY;
    }
}

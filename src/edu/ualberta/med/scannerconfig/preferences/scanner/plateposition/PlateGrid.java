package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

/**
 * Tracks the grid's attributes in number of pixels for the currently scanned
 * plate image.
 * 
 */
public class PlateGrid<T extends Number> {
    public enum Orientation {
        LANDSCAPE,
        PORTRAIT
    };

    public static final int MAX_ROWS = 8;

    public static final int MAX_COLS = 12;

    protected String name;

    protected T left;

    protected T top;

    protected T width;

    protected T height;

    protected Orientation orientation;

    public PlateGrid() {
        left = top = width = height = null;
        orientation = Orientation.LANDSCAPE;
    }

    public PlateGrid(String name, T left, T top, T right, T bottom,
        Orientation orientation) {
        set(name, left, top, right, bottom, orientation);
    }

    public PlateGrid(PlateGrid<T> region) {
        set(region);
    }

    public void set(String name, T left, T top, T right, T bottom,
        Orientation orientation) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.width = right;
        this.height = bottom;
        this.orientation = orientation;
    }

    public void set(PlateGrid<T> region) {
        set(region.name, region.left, region.top, region.width, region.height,
            region.orientation);
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

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "left/" + left + " top/" + top + " width/" + width + " height/"
            + height;
    }

    public int getMaxRows() {
        if (orientation == Orientation.LANDSCAPE) {
            return PlateGrid.MAX_ROWS;
        }
        return PlateGrid.MAX_COLS;
    }

    public int getMaxCols() {
        if (orientation == Orientation.LANDSCAPE) {
            return PlateGrid.MAX_COLS;
        }
        return PlateGrid.MAX_ROWS;

    }
}

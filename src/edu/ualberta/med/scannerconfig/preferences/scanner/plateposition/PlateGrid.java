package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;

import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;

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

    public enum GridDimensions {
        ROWS8COLS12,
        ROWS10COLS10
    };

    protected String name;

    protected T left;

    protected T top;

    protected T width;

    protected T height;

    protected Orientation orientation;

    protected GridDimensions gridDimensions;

    public PlateGrid() {
        left = top = width = height = null;
        orientation = Orientation.LANDSCAPE;
        gridDimensions = GridDimensions.ROWS8COLS12;
    }

    public PlateGrid(String name, T left, T top, T right, T bottom,
        Orientation orientation, GridDimensions gridDimensions) {
        set(name, left, top, right, bottom, orientation, gridDimensions);
    }

    public PlateGrid(PlateGrid<T> region) {
        set(region);
    }

    public void set(String name, T left, T top, T right, T bottom,
        Orientation orientation, GridDimensions gridDimensions) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.width = right;
        this.height = bottom;
        this.orientation = orientation;
        this.gridDimensions = gridDimensions;
    }

    public void set(PlateGrid<T> region) {
        set(region.name, region.left, region.top, region.width, region.height,
            region.orientation, region.gridDimensions);
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

    public GridDimensions getGridDimensions() {
        return gridDimensions;
    }

    public void setGridDimensions(GridDimensions gridDimensions) {
        this.gridDimensions = gridDimensions;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "left/" + left + " top/" + top + " width/" + width + " height/"
            + height;
    }

    public static String orientationToString(Orientation orientation) {
        switch (orientation) {
        case LANDSCAPE:
            return PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE;
        case PORTRAIT:
            return PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT;
        default:
            return null;
        }
    }

    public static String gridDimensionsToString(GridDimensions gridDimensions) {
        switch (gridDimensions) {
        case ROWS8COLS12:
            return PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12;
        case ROWS10COLS10:
            return PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10;
        default:
            return null;
        }
    }

    public static Orientation orientationFromString(String orientation) {
        if (orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE))
            return Orientation.LANDSCAPE;
        else if (orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT))
            return Orientation.PORTRAIT;
        else return Orientation.LANDSCAPE;
    }

    public static GridDimensions gridDimensionsFromString(String gridDimensions) {
        if (gridDimensions.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12))
            return GridDimensions.ROWS8COLS12;
        else if (gridDimensions.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10))
            return GridDimensions.ROWS10COLS10;
        else return GridDimensions.ROWS8COLS12;
    }

    public int getMaxRows() {
        return PreferenceConstants.gridRows(gridDimensionsToString(gridDimensions), orientationToString(orientation));
    }

    public int getMaxCols() {
        return PreferenceConstants.gridCols(gridDimensionsToString(gridDimensions), orientationToString(orientation));
    }
}

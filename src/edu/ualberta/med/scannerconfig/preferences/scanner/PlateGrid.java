package edu.ualberta.med.scannerconfig.preferences.scanner;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Tracks the grid's attributes in number of pixels for the currently scanned
 * plate image.
 * 
 */
public class PlateGrid {
    public enum Orientation {
        LANDSCAPE, PORTRAIT
    };

    public String name;

    public int left;

    public int top;

    public int width;

    public int height;

    public int gapX;

    public int gapY;

    public Orientation orientation;

    public PlateGrid() {
        left = top = width = height = gapX = gapY = 0;
        orientation = Orientation.LANDSCAPE;
    }

    public PlateGrid(String name, int left, int top, int right, int bottom,
        int gapX, int gapY, Orientation orientation) {
        set(name, left, top, right, bottom, gapX, gapY, orientation);
    }

    public PlateGrid(PlateGrid region) {
        set(region);
    }

    public void set(String name, int left, int top, int right, int bottom,
        int gapX, int gapY, Orientation orientation) {
        this.name = name;
        this.left = left;
        this.top = top;
        this.width = right;
        this.height = bottom;
        this.gapX = gapX;
        this.gapY = gapY;
        this.orientation = orientation;
    }

    public void set(PlateGrid region) {
        set(region.name, region.left, region.top, region.width, region.height,
            region.gapX, region.gapY, region.orientation);
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getGapX() {
        return gapX;
    }

    public void setGapX(int gapX) {
        this.gapX = gapX;
    }

    public int getGapY() {
        return gapY;
    }

    public void setGapY(int gapY) {
        this.gapY = gapY;
    }

    public Rectangle getRectangle() {
        return new Rectangle(left, top, width, height);
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

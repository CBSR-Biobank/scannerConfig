package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.geom.Rectangle2D;

public class BoundingBox {

    private final Rectangle2D.Double rectangle;

    public BoundingBox(double x, double y, double width, double height) {
        this.rectangle = new Rectangle2D.Double(x, y, width, height);
        checkValid();
    }

    public BoundingBox(Rectangle2D.Double rectangle) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public BoundingBox(Point point1, Point point2) {
        this(point1.x, point1.y, point2.x - point1.x, point2.y - point1.y);
    }

    public BoundingBox(BoundingBox that) {
        this(that.rectangle);
    }

    @SuppressWarnings("nls")
    protected void checkValid() {
        if (rectangle.x < 0) {
            throw new IllegalArgumentException("invalid value for x: " + rectangle.x);
        }
        if (rectangle.y < 0) {
            throw new IllegalArgumentException("invalid value for y: " + rectangle.y);
        }
        if (rectangle.width < 0) {
            throw new IllegalArgumentException("invalid value for width: " + rectangle.width);
        }
        if (rectangle.y < 0) {
            throw new IllegalArgumentException("invalid value for height: " + rectangle.height);
        }
    }

    public Rectangle2D.Double getRectangle() {
        return rectangle;
    }

    public Point getWidthAndHeightAsPoint() {
        // get the bounding box width and height - note that the bounding box
        // may not originate at (0,0)
        return getCorner(1).translate(getCorner(0).scale(-1));
    }

    @SuppressWarnings("nls")
    public Point getCorner(int cornerId) {
        if (cornerId == 0) {
            return new Point(rectangle.x, rectangle.y);
        } else if (cornerId == 1) {
            return new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        } else {
            throw new IllegalArgumentException("invalid value for corner: " + cornerId);
        }
    }

    public double getCornerX(int cornerId) {
        Point pt = getCorner(cornerId);
        return pt.x;
    }

    public double getCornerY(int cornerId) {
        Point pt = getCorner(cornerId);
        return pt.y;
    }

    public double getWidth() {
        return rectangle.width;
    }

    public double getHeight() {
        return rectangle.height;
    }

    public BoundingBox translate(Point point) {
        return new BoundingBox(
            rectangle.x + point.x,
            rectangle.y + point.y,
            rectangle.width,
            rectangle.height);
    }

    @Override
    public String toString() {
        return rectangle.toString();
    }
}

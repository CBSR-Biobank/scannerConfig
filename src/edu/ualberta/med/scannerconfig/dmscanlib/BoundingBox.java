package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class BoundingBox {
    final List<Point> points = new ArrayList<Point>(2);

    public BoundingBox(Point point1, Point point2) {
        this.points.add(point1);
        this.points.add(point2);

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size");
        }
    }

    public BoundingBox(List<Point> corners) {
        if (corners.size() > 2) {
            throw new IllegalArgumentException(
                "number of corner id is invalid: " + corners.size());
        }
        points.addAll(corners);

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size");
        }
    }

    public BoundingBox(Rectangle rect) {
        double maxX = Integer.MAX_VALUE;
        double maxY = Integer.MAX_VALUE;
        double minX = 0;
        double minY = 0;

        for (int i = 0; i < 4; ++i) {
            maxX = Math.min(maxX, rect.points.get(i).x);
            maxY = Math.min(maxY, rect.points.get(i).y);

            minX = Math.max(minX, rect.points.get(i).x);
            minY = Math.max(minY, rect.points.get(i).y);
        }

        points.add(new Point(minX, minY));
        points.add(new Point(maxX, maxY));

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size");
        }
    }

    public BoundingBox(int x1, int y1, int x2, int y2) {
        points.add(new Point(x1, y1));
        points.add(new Point(x2, y2));

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size:" + points.get(0) + " " + points.get(1));
        }
    }

    public Point getWidthAndHeightAsPoint() {
        // get the bounding box width and height - note that the bounding box
        // may not originate at (0,0)
        return getCorner(1).translate(getCorner(0).scale(-1)); 
    }

    public Point getCorner(int cornerId) {
        if ((cornerId < 0) || (cornerId >= points.size())) {
            throw new IllegalArgumentException("corner id is invalid: "
                + cornerId);
        }
        return points.get(cornerId);
    }

    public double getCornerX(int cornerId) {
        Point pt = getCorner(cornerId);
        return pt.x;
    }

    public double getCornerY(int cornerId) {
        Point pt = getCorner(cornerId);
        return pt.y;
    }

    private boolean isValid() {
        return (points.get(0).getX() < points.get(1).getX())
            && (points.get(0).getY() < points.get(1).getY());
    }

    public double getWidth() {
        return points.get(1).x - points.get(0).x;
    }

    public double getHeight() {
        return points.get(1).y - points.get(0).y;
    }

    public BoundingBox translate(Point point) {
        return new BoundingBox(points.get(0).translate(point), points.get(1)
            .translate(point));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Point point : points) {
            sb.append(point);
        }
        return sb.toString();
    }

}

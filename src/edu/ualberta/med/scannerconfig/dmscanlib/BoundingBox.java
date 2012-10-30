package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class BoundingBox {
    final List<Point> points = new ArrayList<Point>(2);

    BoundingBox(Point point1, Point point2) {
        this.points.add(point1);
        this.points.add(point2);

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size");
        }
    }

    BoundingBox(List<Point> corners) {
        if (corners.size() > 2) {
            throw new IllegalArgumentException(
                "number of corner id is invalid: " + corners.size());
        }
        points.addAll(corners);

        if (!isValid()) {
            throw new IllegalArgumentException("invalid size");
        }
    }

    BoundingBox(Rectangle rect) {
        int maxX = Integer.MAX_VALUE;
        int maxY = Integer.MAX_VALUE;
        int minX = 0;
        int minY = 0;

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
            throw new IllegalArgumentException("invalid size");
        }
    }

    public Point getCorner(int cornerId) {
        if ((cornerId < 0) || (cornerId >= points.size())) {
            throw new IllegalArgumentException("corner id is invalid: "
                + cornerId);
        }
        return points.get(cornerId);
    }

    private boolean isValid() {
        return (points.get(0).getX() < points.get(1).getX())
            && (points.get(0).getY() < points.get(1).getY());
    }

    public double getArea() {
        return (points.get(1).getX() - points.get(0).getX())
            * (points.get(1).getY() - points.get(0).getY());
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

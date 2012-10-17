package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class BoundingBox {
    final List<Point> points = new ArrayList<Point>(2);

    BoundingBox(List<Point> corners) {
        if (corners.size() > 2) {
            throw new IllegalArgumentException(
                "number of corner id is invalid: " + corners.size());
        }
        this.points.addAll(corners);
    }

    BoundingBox(Rectangle rect) {
        double maxX = Double.MAX_VALUE;
        double maxY = Double.MAX_VALUE;
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
    }

    public BoundingBox(double x1, double y1, double x2, double y2) {
        points.add(new Point(x1, y1));
        points.add(new Point(x2, y2));
    }

    Point getCorner(int cornerId) {
        if ((cornerId < 0) || (cornerId >= points.size())) {
            throw new IllegalArgumentException("corner id is invalid: "
                + cornerId);
        }
        return points.get(cornerId);
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

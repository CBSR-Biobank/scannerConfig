package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Rectangle {
    final List<Point> points = new ArrayList<Point>(4);

    Rectangle(BoundingBox boundingBox) {
        points.addAll(Arrays.asList(
            new Point(boundingBox.points.get(0)), 
            new Point(boundingBox.points.get(0).x, boundingBox.points.get(1).y),
            new Point(boundingBox.points.get(1)), 
            new Point(boundingBox.points.get(1).x, boundingBox.points.get(0).y)));
    }

    @SuppressWarnings("nls")
    Rectangle(List<Point> corners) {
        if (corners.size() > 4) {
            throw new IllegalArgumentException("number of corner id is invalid: " + corners.size());
        }
        this.points.addAll(corners);
    }

    @SuppressWarnings("nls")
    Point getPoint(int cornerId) {
        if ((cornerId < 0) || (cornerId >= points.size())) {
            throw new IllegalArgumentException("corner id is invalid: " + cornerId);
        }
        return points.get(cornerId);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Point point : points) {
            sb.append(point).append(" ");
        }
        return sb.toString();
    }
}

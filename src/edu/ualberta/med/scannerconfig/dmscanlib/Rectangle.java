package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class Rectangle<T> {
    final List<Point<T>> corners = new ArrayList<Point<T>>(4);

    Rectangle(List<Point<T>> corners) {
        if (corners.size() > 4) {
            throw new IllegalArgumentException(
                "number of corner id is invalid: " + corners.size());
        }
        this.corners.addAll(corners);
    }

    Point<T> getCorner(int cornerId) {
        if ((cornerId < 0) || (cornerId >= corners.size())) {
            throw new IllegalArgumentException("corner id is invalid: "
                + cornerId);
        }
        return corners.get(cornerId);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Point<T> point : corners) {
            sb.append(point);
        }
        return sb.toString();
    }
}

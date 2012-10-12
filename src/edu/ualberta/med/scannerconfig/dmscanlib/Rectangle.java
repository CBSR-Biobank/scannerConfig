package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class Rectangle<T> {
    final List<Point<T>> corners = new ArrayList<Point<T>>(4);

    Rectangle(List<Point<T>> corners) {
        if (corners.size() > 4) {
            throw new IllegalArgumentException(
                "number of corner id is invalid: "
                    + corners.size());
        }
        for (int i = 0, n = this.corners.size(); i < n; ++i) {
            this.corners.set(i, corners.get(i));
        }
    }

    Point<T> getCorner(int cornerId) {
        if (cornerId >= corners.size()) {
            throw new IllegalArgumentException("corner id is invalid: "
                + cornerId);
        }
        return corners.get(cornerId);
    }
}

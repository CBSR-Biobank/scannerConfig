package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class ScanRegion {

    private final List<Point> points = new ArrayList<Point>(2);

    public ScanRegion(double left, double top, double right, double bottom) {
        this.points.set(0, new Point(left, top));
        this.points.set(1, new Point(right, bottom));
    }

    public Double getPointX(int pointId) {
        if (pointId >= points.size()) {
            throw new IllegalArgumentException("point id is invalid: "
                + pointId);
        }
        return points.get(pointId).getX();
    }

    public Double getPointY(int pointId) {
        if (pointId >= points.size()) {
            throw new IllegalArgumentException("point id is invalid: "
                + pointId);
        }
        return points.get(pointId).getY();
    }

}

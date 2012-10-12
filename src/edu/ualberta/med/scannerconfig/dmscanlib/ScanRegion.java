package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class ScanRegion {

    private final List<Point<Double>> points = new ArrayList<Point<Double>>(2);

    public ScanRegion(double left, double top, double right, double bottom) {
        this.points.set(0, new Point<Double>(left, top));
        this.points.set(1, new Point<Double>(right, bottom));
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

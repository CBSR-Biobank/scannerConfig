package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.List;

public class ScanRegion extends BoundingBox {

    public ScanRegion(List<Point> corners) {
        super(corners);
    }

    public ScanRegion(Point point1, Point point2) {
        super(point1, point2);
    }

    @Override
    protected boolean checkValid() {
        Point point1 = points.get(0);
        Point point2 = points.get(1);
        return (point1.getX() > 0) && (point1.getY() > 0) && (point2.getX() > 0)
            && (point2.getY() > 0);
    }
}

package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.List;

public class WellRectangle {

    final String label;

    final Rectangle rectangle;

    public WellRectangle(String label, BoundingBox boundingBox) {
        this.label = label;
        this.rectangle = new Rectangle(boundingBox);
    }

    public WellRectangle(String label, Rectangle rectangle) {
        this.label = label;
        this.rectangle = rectangle;
    }

    public WellRectangle(String label, List<Point> corners) {
        this.label = label;
        this.rectangle = new Rectangle(corners);
    }

    public String getLabel() {
        return label;
    }

    public int getCornerX(int cornerId) {
        return rectangle.getPoint(cornerId).getX();
    }

    public int getCornerY(int cornerId) {
        return rectangle.getPoint(cornerId).getY();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(label).append(" ").append(rectangle);
        return sb.toString();
    }

}

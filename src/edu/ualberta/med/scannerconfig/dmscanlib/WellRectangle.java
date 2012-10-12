package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.Arrays;
import java.util.List;

public class WellRectangle {

    final String label;

    final Rectangle<Double> rectangle;

    @SuppressWarnings("unchecked")
    public WellRectangle(String label, double left, double top, double right,
        double bottom) {
        this.label = label;
        this.rectangle = new Rectangle<Double>(Arrays.asList(
            new Point<Double>(left, top),
            new Point<Double>(right, top),
            new Point<Double>(left, bottom),
            new Point<Double>(right, bottom)
            ));
    }

    public WellRectangle(String label, Rectangle<Double> rectangle) {
        this.label = label;
        this.rectangle = rectangle;
    }

    public WellRectangle(String label, List<Point<Double>> corners) {
        this.label = label;
        this.rectangle = new Rectangle<Double>(corners);
    }

    public String getLabel() {
        return label;
    }

    public double getCornerX(int cornerId) {
        return rectangle.getCorner(cornerId).getX();
    }

    public double getCornerY(int cornerId) {
        return rectangle.getCorner(cornerId).getY();
    }

}

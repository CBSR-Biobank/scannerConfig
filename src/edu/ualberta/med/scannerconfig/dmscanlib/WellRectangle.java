package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.util.SbsLabeling;

public final class WellRectangle {

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

    public double getCornerX(int cornerId) {
        return rectangle.getPoint(cornerId).getX();
    }

    public double getCornerY(int cornerId) {
        return rectangle.getPoint(cornerId).getY();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(label).append(" ").append(rectangle);
        return sb.toString();
    }

    public static Set<WellRectangle> getWellRectanglesForBoundingBox(
        final BoundingBox bbox,
        final int rows, final int cols, final int dpi) {

        // need to make this box slightly smaller so the image dimensions are
        // not exceeded
        double dotWidth = 1 / new Double(dpi).doubleValue();
        final Point whPt = bbox.getWidthAndHeightAsPoint();
        final Point wellWhPt = new Point(
            whPt.getX() / new Double(cols).doubleValue() - dotWidth,
            whPt.getY() / new Double(rows).doubleValue() - dotWidth);

        final BoundingBox startBbox = new BoundingBox(bbox.getCorner(0),
            wellWhPt.translate(bbox.getCorner(0)));

        final Point horTranslation = new Point(wellWhPt.getX(), 0);
        final Point verTranslation = new Point(0, wellWhPt.getY());

        Set<WellRectangle> wells = new HashSet<WellRectangle>();

        int orientation = 0;
        if ((rows == 12) && (cols == 8)) {
            orientation = 1;
        }

        for (int row = 0; row < rows; ++row) {

            BoundingBox wellBbox =
                startBbox.translate(verTranslation.scale(row));

            for (int col = 0; col < cols; ++col) {
                String label;
                if (orientation == 0) {
                    label = SbsLabeling.fromRowCol(row, 11 - col);
                } else {
                    label = SbsLabeling.fromRowCol(col, row);
                }
                WellRectangle well = new WellRectangle(label, wellBbox);
                wells.add(well);
                wellBbox = wellBbox.translate(horTranslation);
            }
        }

        return wells;
    }

}

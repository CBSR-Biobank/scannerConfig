package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.util.SbsLabeling;

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
    
    public static Set<WellRectangle> getWellRectanglesForBoundingBox(final BoundingBox bbox,
        final int rows, final int cols) {
        
        final Point whPt = bbox.getWidthAndHeightAsPoint().scale(
            new Point(1 / new Double(cols).doubleValue(),
                    1 / new Double(rows).doubleValue()));
        
        // need to make slightli smaller so the image dimensions are not exceeded
        final BoundingBox startBbox = new BoundingBox(bbox.getCorner(0), 
            whPt.translate(bbox.getCorner(0)).scale(0.9));
        
        final Point horTranslation = new Point(whPt.getX(), 0);
        final Point verTranslation = new Point(0, whPt.getY());

        Set<WellRectangle> wells = new HashSet<WellRectangle>();

        for (int row = 0; row < rows; ++row) {
            
            BoundingBox wellBbox = startBbox.translate(verTranslation.scale(row));

            for (int col = 0; col < cols; ++col) {
                WellRectangle well = new WellRectangle(
                    SbsLabeling.fromRowCol(row, 11 - col), wellBbox);
                wells.add(well);
                wellBbox = wellBbox.translate(horTranslation);
            }
        }
        
        return wells;        
    }

}

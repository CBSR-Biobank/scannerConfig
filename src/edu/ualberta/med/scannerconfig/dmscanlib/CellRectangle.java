package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.biobank.util.SbsLabeling;
import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.ScannerDpi;

/**
 * Defines rectangular coordinates, in inches, for a region of image that contains a single 2D
 * barcode. The region also contains a label used to refer to it. This region of the image will then
 * be examined and if it contains a valid 2D barcode it will be decoded.
 * 
 * @author Nelson Loyola
 * 
 */
public final class CellRectangle implements Comparable<CellRectangle> {

    // private static Logger log = LoggerFactory.getLogger(WellRectangle.class);

    private final String label;

    private final Path2D.Double polygon;

    private final Map<Integer, Point2D.Double> points;

    public CellRectangle(String label, Rectangle2D.Double rectangle) {
        this.label = label;
        this.polygon = rectToPoly(rectangle);
        this.points = rectToPoints(rectangle);
    }

    public CellRectangle(String label, BoundingBox boundingBox) {
        this(label, boundingBox.getRectangle());
    }

    /*
     * Corner one is where X and Y are minimum then the following corners go in a counter clockwise
     * direction.
     */
    private Path2D.Double rectToPoly(Rectangle2D.Double rectangle) {
        Double maxX = rectangle.x + rectangle.width;
        Double maxY = rectangle.y + rectangle.height;

        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(rectangle.x, rectangle.y);
        polygon.lineTo(rectangle.x, maxY);
        polygon.lineTo(maxX, maxY);
        polygon.lineTo(maxX, rectangle.y);
        return polygon;
    }

    /*
     * Corner one is where X and Y are minimum then the following corners go in a counter clockwise
     * direction.
     */
    private Map<Integer, Point2D.Double> rectToPoints(Rectangle2D.Double rectangle) {
        Map<Integer, Point2D.Double> result = new HashMap<Integer, Point2D.Double>(4);

        Double maxX = rectangle.x + rectangle.width;
        Double maxY = rectangle.y + rectangle.height;

        result.put(0, new Point2D.Double(rectangle.x, rectangle.y));
        result.put(1, new Point2D.Double(rectangle.x, maxY));
        result.put(2, new Point2D.Double(maxX, maxY));
        result.put(3, new Point2D.Double(maxX, rectangle.y));
        return result;
    }

    public String getLabel() {
        return label;
    }

    public Path2D.Double getPolygon() {
        return polygon;
    }

    public boolean containsPoint(double x, double y) {
        return polygon.contains(x, y);
    }

    public Rectangle2D.Double getBoundsRectangle() {
        Rectangle2D r = polygon.getBounds2D();
        return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public org.eclipse.swt.graphics.Rectangle getBoundsRectangleSWT() {
        Rectangle2D r = polygon.getBounds2D();
        return new org.eclipse.swt.graphics.Rectangle(
            (int) r.getX(),
            (int) r.getY(),
            (int) r.getWidth(),
            (int) r.getHeight());
    }

    private Point2D.Double getPoint(int pointId) {
        Point2D.Double point = points.get(pointId);
        if (point == null) {
            throw new IllegalArgumentException("invalid value for corner: " + pointId);
        }
        return point;
    }

    /**
     * 
     * @param cornerId Corner one is where X and Y are minimum then the following corners go in a
     *            counter clockwise direction.
     * @return
     */
    public double getCornerX(int cornerId) {
        return getPoint(cornerId).x;
    }

    /**
     * 
     * @param cornerId Corner one is where X and Y are minimum then the following corners go in a
     *            counter clockwise direction.
     * @return
     */
    public double getCornerY(int cornerId) {
        return getPoint(cornerId).y;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(label).append(" ");
        for (Entry<Integer, Point2D.Double> entry : points.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            sb.append("(").append(entry.getValue().x).append(", ");
            sb.append(entry.getValue().y).append("), ");
        }
        return sb.toString();
    }

    public static Set<CellRectangle> getCellsForBoundingBox(
        final BoundingBox bbox,
        final PlateOrientation orientation,
        final PlateDimensions dimensions,
        final BarcodePosition barcodePosition,
        final ScannerDpi dpi) {

        int rows, cols;

        switch (orientation) {
        case LANDSCAPE:
            rows = dimensions.getRows();
            cols = dimensions.getCols();
            break;
        case PORTRAIT:
            rows = dimensions.getCols();
            cols = dimensions.getRows();
            break;
        default:
            throw new IllegalArgumentException("invalid orientation value: " + orientation);
        }

        double rowsDouble = rows;
        double colsDouble = cols;

        // need to make this box slightly smaller so the image dimensions are
        // not exceeded
        double dotWidth = 1 / new Double(dpi.getValue());
        final Point whPt = bbox.getWidthAndHeightAsPoint();
        final Point wellWhPt = new Point(
            whPt.getX() / colsDouble - dotWidth,
            whPt.getY() / rowsDouble - dotWidth);

        final Point horTranslation = new Point(wellWhPt.x + dotWidth, 0);
        final double verTranslation = wellWhPt.y + dotWidth;

        Set<CellRectangle> wells = new HashSet<CellRectangle>();
        double startX = bbox.getCornerX(0);
        double startY = bbox.getCornerY(0);
        double yOffset = 0;

        for (int row = 0; row < rows; ++row) {
            // this is the first bounding box in the row
            BoundingBox wellBbox = new BoundingBox(startX, startY + yOffset, wellWhPt.x, wellWhPt.y);

            for (int col = 0; col < cols; ++col) {
                String label = getLabelForPosition(
                    row, col, dimensions, orientation, barcodePosition);
                CellRectangle well = new CellRectangle(label, wellBbox);
                wells.add(well);
                wellBbox = wellBbox.translate(horTranslation);

                // log.debug("getWellRectanglesForBoundingBox: well: {}", well);
            }
            yOffset += verTranslation;
        }

        return wells;
    }

    private static String getLabelForPosition(
        int row,
        int col,
        PlateDimensions dimensions,
        PlateOrientation orientation,
        BarcodePosition barcodePosition) {
        int maxCols;

        switch (barcodePosition) {
        case TOP:
            switch (orientation) {
            case LANDSCAPE:
                return SbsLabeling.fromRowCol(row, col);
            case PORTRAIT:
                maxCols = dimensions.getRows();
                return SbsLabeling.fromRowCol(maxCols - 1 - col, row);

            default:
                throw new IllegalStateException("invalid value for orientation: " + orientation);
            }

        case BOTTOM:
            switch (orientation) {
            case LANDSCAPE:
                maxCols = dimensions.getCols();
                return SbsLabeling.fromRowCol(row, maxCols - 1 - col);
            case PORTRAIT:
                return SbsLabeling.fromRowCol(col, row);

            default:
                throw new IllegalStateException("invalid value for orientation: " + orientation);
            }

        default:
            throw new IllegalStateException("invalid value for barcode position: "
                + barcodePosition);
        }
    }

    @Override
    public int compareTo(CellRectangle that) {
        RowColPos thisPos = SbsLabeling.toRowCol(this.label);
        RowColPos thatPos = SbsLabeling.toRowCol(that.label);
        return thisPos.compareTo(thatPos);
    }
}

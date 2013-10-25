package edu.ualberta.med.scannerconfig;

import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateSettings.PlateOrientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.plateposition.PlateSettings.PlateDimensions;

/**
 * Tracks the grid's attributes for the currently scanned plate image.
 * 
 */
public class PlateGrid {

    private static Logger log = LoggerFactory.getLogger(PlateGrid.class.getName());

    private Rectangle2D.Double plate;

    private final Rectangle2D.Double flatbed;

    private PlateOrientation orientation;

    private PlateDimensions dimensions;

    public PlateGrid(
        Rectangle2D.Double flatbed,
        PlateOrientation orientation,
        PlateDimensions dimensions) {
        plate = new Rectangle2D.Double();
        this.flatbed = flatbed;
        this.orientation = orientation;
        this.dimensions = dimensions;
    }

    public Rectangle2D.Double getPlate() {
        Rectangle2D.Double result = new Rectangle2D.Double();
        result.setRect(plate);
        return result;
    }

    /**
     * Only assign the new plate position if it is inside the flatbed rectangle.
     * 
     * @param x the X coordinate of the upper left corner of the new position.
     * @param y the Y coordinate of the upper left corner of the new position.
     * @param w the width of the new plate
     * @param h the hright of the new plate
     */
    public void setPlate(double x, double y, double w, double h) {
        Rectangle2D.Double newPlatePos = new Rectangle2D.Double(x, y, w, h);
        if ((flatbed.outcode(newPlatePos.getX(), newPlatePos.getY()) == 0)
            && (flatbed.outcode(newPlatePos.getMaxX(), newPlatePos.getMaxY()) == 0)) {
            plate = newPlatePos;
            log.debug("setPlate: plate: {}", plate);
        }
    }

    public PlateOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(PlateOrientation orientation) {
        this.orientation = orientation;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "left/" + plate.getX()
            + " top/" + plate.getY()
            + " width/" + plate.getWidth()
            + " height/" + plate.getHeight();
    }

    /**
     * If the orientation is landscape then the the pair returned is (rows, cols). If the
     * orientation is portrait then the the pair returned is (cols, rows)
     * 
     * @return the rows and columns in terms of the current orientation.
     */
    public Pair<Integer, Integer> getDimensions() {
        switch (orientation) {
        case LANDSCAPE:
            return dimensions.getDimensions();
        case PORTRAIT:
            return new ImmutablePair<Integer, Integer>(dimensions.getCols(), dimensions.getRows());
        default:
            throw new IllegalStateException("invalid value for orientation: " + orientation);
        }
    }

    public void setGridDimensions(PlateDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public Rectangle2D.Double getRectangle() {
        return plate;
    }
}

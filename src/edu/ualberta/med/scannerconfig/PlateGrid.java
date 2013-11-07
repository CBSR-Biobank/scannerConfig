package edu.ualberta.med.scannerconfig;

import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Tracks the grid's attributes for the currently scanned plate image.
 * 
 */
public class PlateGrid extends ScanRegion {

    private PlateOrientation orientation;

    private PlateDimensions dimensions;

    /**
     * The flatbed dimensions must be specified.
     * 
     * @param flatbed a rectangle cotanining the dimensions of the flatbed in inches.
     * @param scanRegion a rectangle containing the dimensions of the scanning region that is
     *            contained within the flatbed.
     * @param orientation The orientation of the plate. Either landscape or portrait.
     * @param dimensions The dimensions for the wells contained by the grid. If it it is a NUNC 8x12
     *            plate, then dimensions would be 8 rows and 12 columsn.
     */
    public PlateGrid(
        Rectangle2D.Double flatbed,
        Rectangle2D.Double scanRegion,
        PlateOrientation orientation,
        PlateDimensions dimensions) {
        super(flatbed, scanRegion);
        this.orientation = orientation;
        this.dimensions = dimensions;
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
        StringBuffer b = new StringBuffer();
        b.append(super.toString()).append(" ");
        b.append(orientation).append(" ");
        b.append(dimensions);
        return b.toString();
    }

    /**
     * If the orientation is landscape then the the pair returned is (rows, cols). If the
     * orientation is portrait then the the pair returned is (cols, rows)
     * 
     * @return the rows and columns in terms of the current orientation.
     */
    public Pair<Integer, Integer> getDimensions() {
        return dimensions.getDimensions();
    }

    public void setGridDimensions(PlateDimensions dimensions) {
        this.dimensions = dimensions;
    }
}

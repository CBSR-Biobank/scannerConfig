package edu.ualberta.med.scannerconfig;

import java.awt.geom.Rectangle2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An axis aligned rectangle that defined an area to scan on the flatbed scanner.
 * 
 * @author loyola
 * 
 */
public class ScanRegion {

    private static Logger log = LoggerFactory.getLogger(ScanRegion.class.getName());

    protected Rectangle2D.Double scanRegion;

    /**
     * The flatbed dimensions must be specified.
     * 
     * @param flatbed a rectangle cotanining the dimensions of the flatbed in inches.
     * @param scanRegion a rectangle containing the dimensions of the scanning region that is
     *            contained within the flatbed.
     */
    public ScanRegion(Rectangle2D.Double flatbed, Rectangle2D.Double scanRegion) {
        if ((flatbed.outcode(scanRegion.getX(), scanRegion.getY()) == 0)
            && (flatbed.outcode(scanRegion.getMaxX(), scanRegion.getMaxY()) == 0)) {
            this.scanRegion = scanRegion;
            log.trace("setPlate: plate: {}", scanRegion);
        } else {
            throw new IllegalArgumentException("scanRegion not contained by flatbed dimensions");
        }
    }

    @Override
    public String toString() {
        return scanRegion.toString();
    }

    public Rectangle2D.Double getRectangle() {
        return scanRegion;
    }

}

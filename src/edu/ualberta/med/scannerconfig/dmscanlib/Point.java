package edu.ualberta.med.scannerconfig.dmscanlib;

import java.awt.geom.Point2D;

/**
 * The main reson for this class is to support {@link translate} and {@link scale}.
 * 
 * @author loyola
 * 
 */
public final class Point extends Point2D.Double {
    private static final long serialVersionUID = 1L;

    public Point(double x, double y) {
        super(x, y);
    }

    public Point(Point point) {
        super(point.x, point.y);
    }

    /**
     * Returns a new point that is translated by the dimensions stored in the parameter
     * {@link point}.
     * 
     * @param point The amount to translate this point.
     * @return
     */
    public Point translate(Point point) {
        return new Point(x + point.x, y + point.y);
    }

    /**
     * Returns a new point that is scaled in all directions by {@link factor}.
     * 
     * @param factor The scaling factor for all directions.
     * @return The scaled point.
     */
    public Point scale(double factor) {
        return new Point(x * factor, y * factor);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(x).append(",").append(y).append(")");
        return sb.toString();
    }

}

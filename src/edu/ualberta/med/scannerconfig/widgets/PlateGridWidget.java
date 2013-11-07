package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Rectangle2D;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.ualberta.med.scannerconfig.ImageWithDpi;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;

/**
 * A widget that allows the user to manipulate a grid that is projected on an image of a scanned
 * plate. Each cell in the grid represents an area of the image that will later be examined and, if
 * it contains a 2D DataMatrix barcode, the barcode will be decoded.
 * 
 * @author loyola
 */
public class PlateGridWidget extends ImageWithRegionWidget {

    // private static Logger log = LoggerFactory.getLogger(PlateGridWidget.class.getName());

    PlateDimensions dimensions;

    private PlateOrientation orientation;

    public PlateGridWidget(Composite parent) {
        super(parent);
    }

    /**
     * If the image is available, the image is drawn on the canvas and the grid is projected on top.
     */
    @Override
    protected void paintCanvas(PaintEvent e) {
        if (userRegion == null) return;

        Rectangle imageRect = image.getBounds();
        Image imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(image.getImage(), 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        Display display = canvas.getDisplay();
        Rectangle2D.Double gridRegionForCanvas = getUserRegion();

        int rows = dimensions.getRows();
        int cols = dimensions.getCols();
        double cellWidth = gridRegionForCanvas.width / cols;
        double cellHeight = gridRegionForCanvas.height / rows;
        int a1Row = 0;
        int a1Col = 0;
        if (orientation.equals(PlateOrientation.LANDSCAPE)) {
            a1Col = cols - 1;
        }

        Rectangle cellRect;
        Color foregroundColor = new Color(display, 0, 255, 0);
        Color a1BackgroundColor = new Color(display, 0, 255, 255);
        imageGC.setForeground(foregroundColor);

        double cx, cy = gridRegionForCanvas.y;
        for (int row = 0; row < rows; row++, cy += cellHeight) {
            cx = gridRegionForCanvas.x;

            for (int col = 0; col < cols; col++, cx += cellWidth) {
                cellRect = new Rectangle((int) cx, (int) cy, (int) cellWidth, (int) cellHeight);

                imageGC.drawRectangle(cellRect);

                if ((row == a1Row) && (col == a1Col)) {
                    imageGC.setAlpha(125);
                    imageGC.setBackground(a1BackgroundColor);
                    imageGC.fillRectangle(cellRect);
                    imageGC.setAlpha(255);
                }
            }
        }

        e.gc.drawImage(imageBuffer, 0, 0);

        foregroundColor.dispose();
        a1BackgroundColor.dispose();
    }

    /**
     * Called by parent widget when a new image is available.
     * 
     * @param image the image to display in the widget
     * @param gridRectangle the starting dimensions for the grid to overlay on top of the image
     * @param orientation the orientation of the grid
     * @param dimensions the dimensions of the grid
     */
    public void imageUpdated(
        ImageWithDpi image,
        Rectangle2D.Double gridRectangle,
        PlateOrientation orientation,
        PlateDimensions dimensions) {
        this.orientation = orientation;
        this.dimensions = dimensions;
        imageUpdated(image);
        setUserRegionInInches(gridRectangle);
        refresh();
    }

    public void setPlateOrientation(PlateOrientation orientation) {
        this.orientation = orientation;
        refresh();
    }

    public void setPlateDimensions(PlateDimensions dimensions) {
        this.dimensions = dimensions;
        refresh();
    }
}

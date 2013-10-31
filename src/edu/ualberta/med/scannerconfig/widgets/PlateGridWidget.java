package edu.ualberta.med.scannerconfig.widgets;

import java.awt.geom.Rectangle2D;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.ualberta.med.scannerconfig.PlateOrientation;

/**
 * A widget that allows the user to manipulate a grid that is projected on an image of a scanned
 * plate. Each cell in the grid represents an area of the image that will later be examined and, if
 * it contains a 2D DataMatrix barcode, the barcode will be decoded.
 * 
 * @author loyola
 */
public class PlateGridWidget extends ScanRegionWidget {

    // private static Logger log = LoggerFactory.getLogger(PlateGridWidget.class.getName());

    private final IPlateGridWidget listener;

    public PlateGridWidget(Composite parent, IPlateGridWidget listener) {
        super(parent, listener);
        this.listener = listener;
    }

    /**
     * If the scanned image is available, the image is drawn on the canvas and the grid is projected
     * on top.
     */
    @Override
    protected void paintCanvas(PaintEvent e) {
        Image imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        GC imageGC = new GC(imageBuffer);

        Display display = canvas.getDisplay();
        Rectangle2D.Double scanRegionForCanvas = getScanRegionForCanvas();
        Pair<Integer, Integer> dimensions = listener.getPlateDimensions();

        int rows = dimensions.getLeft();
        int cols = dimensions.getRight();
        double cellWidth = scanRegionForCanvas.width / cols;
        double cellHeight = scanRegionForCanvas.height / rows;
        int a1Row = 0;
        int a1Col = 0;
        if (listener.getOrientation().equals(PlateOrientation.LANDSCAPE)) {
            a1Col = cols - 1;
        }

        Rectangle cellRect;
        Color foregroundColor = new Color(display, 0, 255, 0);
        Color a1BackgroundColor = new Color(display, 0, 255, 255);
        imageGC.setForeground(foregroundColor);

        double cx, cy = scanRegionForCanvas.y;
        for (int row = 0; row < rows; row++, cy += cellHeight) {
            cx = scanRegionForCanvas.x;

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
}

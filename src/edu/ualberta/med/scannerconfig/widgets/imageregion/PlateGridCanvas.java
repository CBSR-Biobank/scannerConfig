package edu.ualberta.med.scannerconfig.widgets.imageregion;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.scannerconfig.BarcodeImage;
import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;
import edu.ualberta.med.scannerconfig.dmscanlib.BoundingBox;
import edu.ualberta.med.scannerconfig.dmscanlib.CellRectangle;
import edu.ualberta.med.scannerconfig.dmscanlib.DecodedWell;

public class PlateGridCanvas extends ImageWithRegionCanvas {

    private PlateDimensions dimensions;

    private PlateOrientation orientation;

    private BarcodePosition barcodePosition;

    // these rectangles have pixels as units
    private final Map<String, CellRectangle> cellRectangles;

    private final Map<String, DecodedWell> decodedWells;

    private final Image decodedIconImage;

    private final Rectangle decodedIconImageBounds;

    public PlateGridCanvas(Composite parent) {
        super(parent);
        cellRectangles = new HashMap<String, CellRectangle>();
        decodedWells = new HashMap<String, DecodedWell>();
        decodedIconImage = BgcPlugin.getDefault().getImage(BgcPlugin.Image.ACCEPT);
        decodedIconImageBounds = decodedIconImage.getBounds();

        addMouseTrackListener(new MouseTrackListener() {

            @Override
            public void mouseHover(MouseEvent e) {
                PlateGridCanvas.this.mouseHover(e);
            }

            @Override
            public void mouseExit(MouseEvent e) {
                // do nothing
            }

            @Override
            public void mouseEnter(MouseEvent e) {
                // do nothing
            }
        });
    }

    protected void updateImage() {
        Display display = getDisplay();
        Color foregroundColor = new Color(display, 0, 255, 0);
        Color a1BackgroundColor = new Color(display, 0, 255, 255);

        Rectangle imageRect = barcodeImage.getBounds();
        Image imageBuffer = new Image(getDisplay(), imageRect.width, imageRect.height);
        GC gc = new GC(imageBuffer);

        gc.drawImage(barcodeImage.getImage(), 0, 0, imageRect.width, imageRect.height,
            0, 0, imageRect.width, imageRect.height);

        gc.setForeground(foregroundColor);

        for (CellRectangle cell : cellRectangles.values()) {
            Rectangle rect = cell.getBoundsRectangleSWT();
            gc.drawRectangle(rect);

            DecodedWell decodedWell = decodedWells.get(cell.getLabel());
            if (decodedWell != null) {
                gc.drawImage(decodedIconImage,
                    0, 0, decodedIconImageBounds.width, decodedIconImageBounds.height,
                    rect.x, rect.y, decodedIconImageBounds.width, decodedIconImageBounds.height);
            }
        }

        Rectangle rect = cellRectangles.get("A1").getBoundsRectangleSWT();
        gc.setAlpha(125);
        gc.setBackground(a1BackgroundColor);
        gc.fillRectangle(rect);
        gc.setAlpha(255);

        gc.drawImage(imageBuffer, 0, 0);
        gc.dispose();

        // FIXME
        // setSourceImage(imageBuffer.getImage());
        imageBuffer.dispose();

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
     * @param barcodePosition the location of the barcode on a tube
     * @param imageSource
     */
    public void updateImage(
        BarcodeImage image,
        Rectangle2D.Double gridRectangle,
        PlateOrientation orientation,
        PlateDimensions dimensions,
        BarcodePosition barcodePosition) {

        setOrientation(orientation);
        setDimensions(dimensions);
        setBarcodePosition(barcodePosition);
        setUserRegionInInches(image, gridRectangle);
        updateImage(image);
        redraw();
    }

    private void udpateCellRectangles() {
        if (getSourceImage() == null) return;

        cellRectangles.clear();

        BoundingBox boundingBoxInPixels = new BoundingBox(getUserRegionInInches());
        Set<CellRectangle> cellsInPixels = CellRectangle.getCellsForBoundingBox(
            boundingBoxInPixels, orientation, dimensions, barcodePosition, barcodeImage.getDpi());

        for (CellRectangle cell : cellsInPixels) {
            String label = cell.getLabel();
            cellRectangles.put(label, cell);
        }
    }

    @Override
    public void controlResized() {
        super.controlResized();
        udpateCellRectangles();
    }

    @Override
    protected void mouseDrag(MouseEvent e) {
        super.mouseDrag(e);
        udpateCellRectangles();
    }

    @Override
    protected void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        udpateCellRectangles();
    }

    private void mouseHover(MouseEvent e) {
        CellRectangle cell = getObjectAtCoordinates(e.x, e.y);
        if (cell != null) {
            StringBuffer buf = new StringBuffer();
            buf.append(cell.getLabel());

            DecodedWell decodedWell = decodedWells.get(cell.getLabel());
            if (decodedWell != null) {
                buf.append(": ").append(decodedWell.getMessage());
            }

            setToolTipText(buf.toString());
        } else {
            setToolTipText(null);
        }
    }

    private CellRectangle getObjectAtCoordinates(int x, int y) {
        for (CellRectangle cell : cellRectangles.values()) {
            if (cell.containsPoint(x, y)) {
                return cell;
            }
        }
        return null;
    }

    public void setDimensions(PlateDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public void setOrientation(PlateOrientation orientation) {
        this.orientation = orientation;
    }

    public void setBarcodePosition(BarcodePosition barcodePosition) {
        this.barcodePosition = barcodePosition;
    }

    public void setDecodeInfo(Map<String, DecodedWell> decodedWells) {
        removeDecodeInfo();
        decodedWells.putAll(decodedWells);
    }

    public void removeDecodeInfo() {
        decodedWells.clear();
    }

    /**
     * Returs the grid region in units of inches.
     * 
     * @return
     */
    public Set<CellRectangle> getCellsInInches() {
        BoundingBox boundingBoxInInches = new BoundingBox(getUserRegionInInches());
        Set<CellRectangle> cellsInInches = CellRectangle.getCellsForBoundingBox(
            boundingBoxInInches, orientation, dimensions, barcodePosition, barcodeImage.getDpi());
        return cellsInInches;
    }
}

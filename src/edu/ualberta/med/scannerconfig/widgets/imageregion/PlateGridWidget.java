package edu.ualberta.med.scannerconfig.widgets.imageregion;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.scannerconfig.BarcodeImage;
import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.ImageSource;
import edu.ualberta.med.scannerconfig.PlateDimensions;
import edu.ualberta.med.scannerconfig.PlateOrientation;
import edu.ualberta.med.scannerconfig.dmscanlib.BoundingBox;
import edu.ualberta.med.scannerconfig.dmscanlib.CellRectangle;
import edu.ualberta.med.scannerconfig.dmscanlib.DecodedWell;

/**
 * A widget that allows the user to manipulate a grid that is projected on an image of a scanned
 * plate. Each cell in the grid represents an area of the image that will later be examined and, if
 * it contains a 2D DataMatrix barcode, the barcode will be decoded.
 * 
 * @author loyola
 */
public class PlateGridWidget extends ImageWithRegionWidget implements MouseTrackListener {

    private static final I18n i18n = I18nFactory.getI18n(PlateGridWidget.class);

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(PlateGridWidget.class.getName());

    private PlateDimensions dimensions;

    private PlateOrientation orientation;

    private BarcodePosition barcodePosition;

    // text to display below the image
    private final Label infoTextLabel;

    // these rectangles have pixels as units
    private final Map<String, CellRectangle> cellRectangles;

    private final Map<String, DecodedWell> decodedWells;

    private final Image decodedImage;

    private final Rectangle decodedImageBounds;

    public PlateGridWidget(Composite parent) {
        super(parent);
        GridLayout layout = (GridLayout) getLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        infoTextLabel = createInfoLabel();

        decodedWells = new HashMap<String, DecodedWell>();
        decodedImage = BgcPlugin.getDefault().getImage(BgcPlugin.Image.ACCEPT);
        decodedImageBounds = decodedImage.getBounds();

        cellRectangles = new HashMap<String, CellRectangle>();
        canvas.addMouseTrackListener(this);
    }

    private Label createInfoLabel() {
        final Composite composite = new Composite(this, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);

        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        composite.setLayoutData(gd);

        Label infoLabel = new Label(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        infoLabel.setLayoutData(gd);
        return infoLabel;
    }

    /**
     * If the image is available, the image is drawn on the canvas and the grid is projected on top.
     */
    @Override
    protected void paintCanvas(PaintEvent e) {
        if (image == null) return;

        // log.debug("paintControl: userRegionInPixels: {}", userRegionInPixels);

        Rectangle imageRect = image.getBounds();
        Image imageBuffer = new Image(canvas.getDisplay(), canvas.getBounds());
        imageGC = new GC(imageBuffer);
        imageGC.drawImage(image.getImage(), 0, 0, imageRect.width, imageRect.height,
            0, 0, canvas.getBounds().width, canvas.getBounds().height);

        Display display = canvas.getDisplay();

        Color foregroundColor = new Color(display, 0, 255, 0);
        Color a1BackgroundColor = new Color(display, 0, 255, 255);
        imageGC.setForeground(foregroundColor);

        for (CellRectangle cell : cellRectangles.values()) {
            Rectangle rect = cell.getBoundsRectangleSWT();
            imageGC.drawRectangle(rect);

            DecodedWell decodedWell = decodedWells.get(cell.getLabel());
            if (decodedWell != null) {
                imageGC.drawImage(decodedImage,
                    0, 0, decodedImageBounds.width, decodedImageBounds.height,
                    rect.x, rect.y, decodedImageBounds.width, decodedImageBounds.height);
            }
        }

        Rectangle rect = cellRectangles.get("A1").getBoundsRectangleSWT();
        imageGC.setAlpha(125);
        imageGC.setBackground(a1BackgroundColor);
        imageGC.fillRectangle(rect);
        imageGC.setAlpha(255);

        e.gc.drawImage(imageBuffer, 0, 0);

        foregroundColor.dispose();
        a1BackgroundColor.dispose();
    }

    @Override
    protected void mouseDrag(MouseEvent e) {
        super.mouseDrag(e);
        udpateCellRectangles();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        udpateCellRectangles();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void controlResized(ControlEvent e) {
        super.controlResized(e);
        udpateCellRectangles();
    }

    public void removeDecodeInfo() {
        decodedWells.clear();
    }

    @Override
    public void mouseEnter(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseExit(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseHover(MouseEvent e) {
        CellRectangle cell = getObjectAtCoordinates(e.x, e.y);
        if (cell != null) {
            StringBuffer buf = new StringBuffer();
            buf.append(cell.getLabel());

            DecodedWell decodedWell = decodedWells.get(cell.getLabel());
            if (decodedWell != null) {
                buf.append(": ").append(decodedWell.getMessage());
            }

            canvas.setToolTipText(buf.toString());
        } else {
            canvas.setToolTipText(null);
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

    private void udpateCellRectangles() {
        if (image == null) return;

        cellRectangles.clear();

        // construct the rectangles in units of pixels (not inches)
        BoundingBox boundingBoxInPixels = new BoundingBox(regionToPixels(getUserRegionInInches()));
        Set<CellRectangle> cellsInPixels = CellRectangle.getCellsForBoundingBox(
            boundingBoxInPixels, orientation, dimensions, barcodePosition, image.getDpi());

        for (CellRectangle cell : cellsInPixels) {
            String label = cell.getLabel();
            cellRectangles.put(label, cell);
        }
    }

    private String getImageInfoText(BarcodeImage image) {
        StringBuffer buf = new StringBuffer();
        String basename = image.getBasename();
        if (image.getImageSource() == ImageSource.FILE) {
            buf.append(i18n.tr("File: "));
            buf.append(basename);
            buf.append(i18n.tr(", created: "));

            infoTextLabel.setToolTipText(image.getFilename());
        } else {
            buf.append(i18n.tr("Image scan date: "));

            infoTextLabel.setToolTipText(null);
        }
        buf.append(DateFormatter.formatAsDateTime(image.getDateLastModified()));
        buf.append(i18n.tr(", DPI: "));
        buf.append(image.getDpi().getDisplayLabel());

        int size = decodedWells.size();
        if (size > 0) {
            buf.append(", tubes decoded: ").append(size);
        }

        return buf.toString();
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
        this.orientation = orientation;
        this.dimensions = dimensions;
        this.barcodePosition = barcodePosition;
        updateImage(image);
        setUserRegionInInches(gridRectangle);
        infoTextLabel.setText(getImageInfoText(image));
        refresh();
    }

    @Override
    public void refresh() {
        udpateCellRectangles();
        super.refresh();
    }

    public void removeImage() {
        updateImage(null);
        removeDecodeInfo();
        refresh();
    }

    /**
     * Called when the user changes the orientation of the grid. The grid is updated to show the new
     * orientation.
     * 
     * @param orientation Either landscape or portrait.
     */
    public void setPlateOrientation(PlateOrientation orientation) {
        this.orientation = orientation;
        removeDecodeInfo();
        refresh();
    }

    /**
     * Called when the user changes the dimensios of the grid. The dimensions state how many tubes
     * are conained in the image. The grid is updated to show the new dimensions.
     * 
     * @param dimensions The number or rows and columns of tubes.
     */
    public void setPlateDimensions(PlateDimensions dimensions) {
        this.dimensions = dimensions;
        removeDecodeInfo();
        refresh();
    }

    /**
     * Called when the user changes the location of where the barcodes are located on a tube. They
     * may be either on the tops or bottoms of the tubes.
     * 
     * @param barcodePosition if the barcodes are on the tops or bottoms of the tubes.
     */
    public void setBarcodePosition(BarcodePosition barcodePosition) {
        this.barcodePosition = barcodePosition;
        removeDecodeInfo();
        refresh();
    }

    /**
     * Used to display decoding information with the image.
     * 
     * @param decodedWells
     */
    public void setDecodedWells(Set<DecodedWell> wells) {
        for (DecodedWell decodedWell : wells) {
            decodedWells.put(decodedWell.getLabel(), decodedWell);
        }
        infoTextLabel.setText(getImageInfoText(image));
        refresh();
    }

    /**
     * Returs the grid region in units of inches.
     * 
     * @return
     */
    public Set<CellRectangle> getCellsInInches() {
        BoundingBox boundingBoxInInches = new BoundingBox(getUserRegionInInches());
        Set<CellRectangle> cellsInInches = CellRectangle.getCellsForBoundingBox(
            boundingBoxInInches, orientation, dimensions, barcodePosition, image.getDpi());
        return cellsInInches;
    }
}

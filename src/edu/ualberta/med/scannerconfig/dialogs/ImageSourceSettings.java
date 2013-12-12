package edu.ualberta.med.scannerconfig.dialogs;

import java.awt.geom.Rectangle2D;

import edu.ualberta.med.scannerconfig.BarcodePosition;
import edu.ualberta.med.scannerconfig.ImageSource;
import edu.ualberta.med.scannerconfig.PalletDimensions;
import edu.ualberta.med.scannerconfig.PalletOrientation;
import edu.ualberta.med.scannerconfig.preferences.scanner.ScannerDpi;

/**
 * Stores the settings associated with an image source.
 * 
 * @author loyola
 * 
 */
public class ImageSourceSettings {

    private static final int STRING_ARRAY_NUM_ITEMS = 8;

    private ImageSource imageSource;
    private PalletOrientation orientation;
    private PalletDimensions dimensions;
    private BarcodePosition barcodePosition;
    private ScannerDpi scannerDpi;
    private Rectangle2D.Double gridRectangle;

    /**
     * The settings associated with an image source.
     * 
     * @param imageSource Can be one of the flatbed scanning regions or a file.
     * @param orientation The orientation of the pallet.
     * @param dimensions The dimensions of the pallet in terms of number of tubes.
     * @param barcodePosition If the tubes have the 2D barcode on the top or bottom of the tube.
     * @param gridRectangle The rectangle that defines the decode region in an image.
     */
    private ImageSourceSettings(
        ImageSource imageSource,
        PalletOrientation orientation,
        PalletDimensions dimensions,
        BarcodePosition barcodePosition,
        ScannerDpi scannerDpi,
        Rectangle2D.Double gridRectangle) {
        this.setImageSource(imageSource);
        this.setOrientation(orientation);
        this.dimensions = dimensions;
        this.barcodePosition = barcodePosition;
        this.scannerDpi = scannerDpi;
        setGridRectangle(gridRectangle);
    }

    public ImageSource getImageSource() {
        return imageSource;
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public PalletOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(PalletOrientation orientation) {
        this.orientation = orientation;
    }

    public PalletDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(PalletDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public BarcodePosition getBarcodePosition() {
        return barcodePosition;
    }

    public void setBarcodePosition(BarcodePosition barcodePosition) {
        this.barcodePosition = barcodePosition;
    }

    public ScannerDpi getScannerDpi() {
        return scannerDpi;
    }

    public void setScannerDpi(ScannerDpi scannerDpi) {
        this.scannerDpi = scannerDpi;
    }

    public Rectangle2D.Double getGridRectangle() {
        return gridRectangle;
    }

    public void setGridRectangle(Rectangle2D.Double gridRectangle) {
        this.gridRectangle = new Rectangle2D.Double();
        this.gridRectangle.x = gridRectangle.x;
        this.gridRectangle.y = gridRectangle.y;
        this.gridRectangle.width = gridRectangle.width;
        this.gridRectangle.height = gridRectangle.height;
    }

    public String[] toSettingsStringArray() {
        String[] result = new String[STRING_ARRAY_NUM_ITEMS];
        result[0] = getOrientation().getId();
        result[1] = dimensions.getId();
        result[2] = barcodePosition.getId();
        result[3] = String.valueOf(scannerDpi.getValue());
        result[4] = String.valueOf(gridRectangle.x);
        result[5] = String.valueOf(gridRectangle.y);
        result[6] = String.valueOf(gridRectangle.width);
        result[7] = String.valueOf(gridRectangle.height);
        return result;
    }

    public static ImageSourceSettings getFromSettingsStringArray(ImageSource source, String[] values) {
        if ((values == null) || (values.length != STRING_ARRAY_NUM_ITEMS)) {
            throw new IllegalStateException("invalid length for grid rectangle settings");
        }
        PalletOrientation orientation = PalletOrientation.getFromIdString(values[0]);
        PalletDimensions dimensions = PalletDimensions.getFromIdString(values[1]);
        BarcodePosition barcodePosition = BarcodePosition.getFromIdString(values[2]);
        ScannerDpi scannerDpi = ScannerDpi.getFromId(Integer.parseInt(values[3]));
        double left = Double.parseDouble(values[4]);
        double top = Double.parseDouble(values[5]);
        double width = Double.parseDouble(values[6]);
        double height = Double.parseDouble(values[7]);
        Rectangle2D.Double rectangle = new Rectangle2D.Double(left, top, width, height);
        return new ImageSourceSettings(
            source, orientation, dimensions, barcodePosition, scannerDpi, rectangle);
    }

    public static ImageSourceSettings defaultSettings(ImageSource source) {
        return new ImageSourceSettings(
            source,
            PalletOrientation.LANDSCAPE,
            PalletDimensions.DIM_ROWS_8_COLS_12,
            BarcodePosition.BOTTOM,
            ScannerDpi.DPI_600,
            new Rectangle2D.Double(-1, -1, -1, -1));
    }
}

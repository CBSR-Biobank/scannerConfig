package edu.ualberta.med.scannerconfig.dialogs;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IDialogSettings;

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

    @SuppressWarnings("nls")
    private static final String PALLET_ORIENTATION_KEY = "palletOrientation";

    @SuppressWarnings("nls")
    private static final String PALLET_DIMENSIONS_KEY = "palletDimensions";

    @SuppressWarnings("nls")
    private static final String BARCODE_POSITION_KEY = "barcodePosition";

    @SuppressWarnings("nls")
    private static final String SCANNER_DPI_KEY = "scannerDpi";

    @SuppressWarnings("nls")
    private static final String REGION_SECTION_KEY_PREFIX = "region";

    @SuppressWarnings("nls")
    private static final String REGION_X_KEY = "regionLeft";

    @SuppressWarnings("nls")
    private static final String REGION_Y_KEY = "regionTop";

    @SuppressWarnings("nls")
    private static final String REGION_WIDTH_KEY = "regionWidth";

    @SuppressWarnings("nls")
    private static final String REGION_HEIGHT_KEY = "regionHeight" +
        "";

    private ImageSource imageSource;
    private PalletOrientation orientation;
    private PalletDimensions dimensions;
    private BarcodePosition barcodePosition;
    private ScannerDpi scannerDpi;
    Map<ScannerDpi, Rectangle2D.Double> gridRectangles;

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
        Map<ScannerDpi, Rectangle2D.Double> gridRectangles) {
        this.setImageSource(imageSource);
        this.setOrientation(orientation);
        this.dimensions = dimensions;
        this.barcodePosition = barcodePosition;
        this.scannerDpi = scannerDpi;
        this.gridRectangles = gridRectangles;
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

    @SuppressWarnings("nls")
    public Rectangle2D.Double getGridRectangle(ScannerDpi dpi) {
        Rectangle2D.Double rect = gridRectangles.get(dpi);
        if (rect == null) {
            throw new IllegalStateException("grid rectangle is null: " + dpi.getDisplayLabel());
        }
        return rect;
    }

    public void setGridRectangle(ScannerDpi dpi, Rectangle2D.Double gridRectangle) {
        gridRectangles.put(dpi, gridRectangle);
    }

    public void putSettingsInSection(IDialogSettings section) {
        section.put(PALLET_ORIENTATION_KEY, orientation.getId());

        section.put(PALLET_DIMENSIONS_KEY, dimensions.getId());
        section.put(BARCODE_POSITION_KEY, barcodePosition.getId());

        if (imageSource != ImageSource.FILE) {
            section.put(SCANNER_DPI_KEY, scannerDpi.getValue());
        }

        putGridRectanglesInSection(section);
    }

    private void putGridRectanglesInSection(IDialogSettings section) {

        for (Entry<ScannerDpi, Rectangle2D.Double> entry : gridRectangles.entrySet()) {
            String sectionName = getGridRectangleSettingsKey(entry.getKey());
            IDialogSettings regionSection = section.getSection(sectionName);
            if (regionSection == null) {
                regionSection = section.addNewSection(sectionName);
            }

            Rectangle2D.Double gridRectangle = entry.getValue();
            regionSection.put(REGION_X_KEY, gridRectangle.x);
            regionSection.put(REGION_Y_KEY, gridRectangle.y);
            regionSection.put(REGION_WIDTH_KEY, gridRectangle.width);
            regionSection.put(REGION_HEIGHT_KEY, gridRectangle.height);
        }

    }

    public static ImageSourceSettings getSettingsFromSection(
        ImageSource source,
        IDialogSettings section) {

        IDialogSettings imageSourceSection = section.getSection(source.getId());
        if (imageSourceSection == null) {
            return defaultSettings(source);
        }

        PalletOrientation orientation;
        PalletDimensions dimensions;
        BarcodePosition barcodePosition;
        ScannerDpi scannerDpi = ScannerDpi.DPI_300;

        String orientationStr = getSetting(
            imageSourceSection,
            PALLET_ORIENTATION_KEY,
            PalletOrientation.LANDSCAPE.getId());
        orientation = PalletOrientation.getFromIdString(orientationStr);

        String dimensionsStr = getSetting(
            imageSourceSection,
            PALLET_DIMENSIONS_KEY,
            PalletDimensions.DIM_ROWS_8_COLS_12.getId());
        dimensions = PalletDimensions.getFromIdString(dimensionsStr);

        String barcodePositionStr = getSetting(
            imageSourceSection,
            BARCODE_POSITION_KEY,
            BarcodePosition.BOTTOM.getId());
        barcodePosition = BarcodePosition.getFromIdString(barcodePositionStr);

        Map<ScannerDpi, Rectangle2D.Double> regions = new HashMap<ScannerDpi, Rectangle2D.Double>();

        if (source != ImageSource.FILE) {
            int dpi = getSetting(imageSourceSection, SCANNER_DPI_KEY, ScannerDpi.DPI_300.getValue());
            scannerDpi = ScannerDpi.getFromId(dpi);

            for (ScannerDpi dpi2 : ScannerDpi.getValidDpis()) {
                regions.put(dpi2, getRegionSettingsFromSection(dpi2, imageSourceSection));
            }
        } else {
            regions.put(ScannerDpi.DPI_UNKNOWN,
                getRegionSettingsFromSection(ScannerDpi.DPI_UNKNOWN, section));
        }

        return new ImageSourceSettings(
            source, orientation, dimensions, barcodePosition, scannerDpi, regions);
    }

    private static Rectangle2D.Double getRegionSettingsFromSection(
        ScannerDpi dpi,
        IDialogSettings section) {
        double x = -1;
        double y = -1;
        double width = -1;
        double height = -1;

        IDialogSettings regionSection = section.getSection(getGridRectangleSettingsKey(dpi));

        if (regionSection != null) {
            x = getSetting(regionSection, REGION_X_KEY, -1.0);
            y = getSetting(regionSection, REGION_Y_KEY, -1.0);
            width = getSetting(regionSection, REGION_WIDTH_KEY, -1.0);
            height = getSetting(regionSection, REGION_HEIGHT_KEY, -1.0);
        }

        return new Rectangle2D.Double(x, y, width, height);
    }

    private static String getSetting(IDialogSettings section, String key, String defaultValue) {
        String value = section.get(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private static int getSetting(IDialogSettings section, String key, int defaultValue) {
        int result = defaultValue;
        try {
            result = section.getInt(key);
        } catch (NumberFormatException e) {
            // do nothing
        }
        return result;
    }

    private static double getSetting(IDialogSettings section, String key, double defaultValue) {
        double result = defaultValue;
        try {
            result = section.getDouble(key);
        } catch (NumberFormatException e) {
            // do nothing
        }
        return result;
    }

    private static String getGridRectangleSettingsKey(ScannerDpi dpi) {
        StringBuffer buf = new StringBuffer();
        buf.append(REGION_SECTION_KEY_PREFIX);
        buf.append(dpi.getValue());
        return buf.toString();
    }

    public static ImageSourceSettings defaultSettings(ImageSource source) {
        Map<ScannerDpi, Rectangle2D.Double> regions =
            new HashMap<ScannerDpi, Rectangle2D.Double>();

        if (source == ImageSource.FILE) {
            regions.put(ScannerDpi.DPI_UNKNOWN, new Rectangle2D.Double(-1, -1, -1, -1));

            return new ImageSourceSettings(
                source,
                PalletOrientation.LANDSCAPE,
                PalletDimensions.DIM_ROWS_8_COLS_12,
                BarcodePosition.BOTTOM,
                ScannerDpi.DPI_UNKNOWN,
                regions);
        }

        for (ScannerDpi dpi : ScannerDpi.getValidDpis()) {
            regions.put(dpi, new Rectangle2D.Double(-1, -1, -1, -1));
        }

        return new ImageSourceSettings(
            source,
            PalletOrientation.LANDSCAPE,
            PalletDimensions.DIM_ROWS_8_COLS_12,
            BarcodePosition.BOTTOM,
            ScannerDpi.DPI_UNKNOWN,
            regions);
    }
}

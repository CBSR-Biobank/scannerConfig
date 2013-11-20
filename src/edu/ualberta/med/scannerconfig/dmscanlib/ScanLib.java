package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanLib {
    /**
     * The first plate number.
     */
    public static final int MIN_PLATE_NUM = 1;

    /**
     * The maximum plate number.
     */
    public static final int MAX_PLATE_NUM = 5;

    /**
     * The call to ScanLib was successful.
     */
    public static final int SC_SUCCESS = 0;

    /**
     * Unable to scan an image.
     */
    public static final int SC_FAIL = -1;

    public static final int SC_TWAIN_UNAVAIL = -2;

    public static final int SC_INVALID_DPI = -3;

    /**
     * no tubes found in wells.
     */
    public static final int SC_INVALID_NOTHING_DECODED = -4;

    public static final int SC_INVALID_IMAGE = -5;

    public static final int SC_INVALID_NOTHING_TO_DECODE = -6;

    public static final int SC_INCORRECT_DPI_SCANNED = -7;

    public static final int CAP_IS_WIA = 0x01;

    public static final int CAP_DPI_300 = 0x02;

    public static final int CAP_DPI_400 = 0x04;

    public static final int CAP_DPI_600 = 0x08;

    public static final int CAP_IS_SCANNER = 0x10;

    private static ScanLib instance = null;

    protected ScanLib() {
    }

    public static ScanLib getInstance() {
        if (instance != null) return instance;

        instance = new ScanLib();

        if (instance == null) {
            throw new NullPointerException("scanlib not supported on your operating system"); //$NON-NLS-1$
        }
        return instance;
    }

    /**
     * Queries the availability of the TWAIN driver.
     * 
     * @return SC_SUCCESS if available, and SC_INVALID_VALUE if unavailable.
     */
    public native ScanLibResult isTwainAvailable();

    /**
     * Creates a dialog box to allow the user to select the scanner to use by
     * default.
     * 
     * @return SC_SUCCESS when selected by the user, and SC_INVALID_VALUE if the
     *         user cancelled the selection dialog.
     */
    public native ScanLibResult selectSourceAsDefault();

    /**
     * Queries the selected scanner for the driver type and supported dpi.
     * 
     * @return Bit 1: Is set if driver type is WIA. Bits 2,3,4 are set if driver
     *         supports 300,400,600 dpi. Bit 5 is set if a proper scanner source
     *         is selected.
     */
    public native ScanLibResult getScannerCapability();

    /**
     * Scans an image for the specified dimensions. The image is in Windows BMP
     * format.
     * 
     * @param verbose The amount of logging information to generate. 1 is
     *            minimal and 9 is very detailed. Logging information is
     *            appended to file scanlib.log.
     * @param dpi The dots per inch for the image. Function
     *            slGetScannerCapability() returns the valid values.
     * @param brightness a value between -1000 and 1000. Only used when using
     *            the TWAIN driver.
     * @param contrast a value between -1000 and 1000. Only used when using the
     *            TWAIN driver.
     * @param left The left margin in inches.
     * @param top The top margin in inches.
     * @param right The width in inches.
     * @param bottom The height in inches.
     * @param filename The file name to save the bitmap to.
     * 
     * @return SC_SUCCESS if valid. SC_FAIL unable to scan an image.
     */
    public native ScanLibResult scanImage(long verbose, long dpi, int brightness, int contrast,
        BoundingBox region, String filename);

    /**
     * Scans the whole flatbed region. The image is in Windows BMP format.
     * 
     * @param verbose The amount of logging information to generate. 1 is
     *            minimal and 9 is very detailed. Logging information is
     *            appended to file dmscanlib.log.
     * @param dpi The dots per inch for the image. Function
     *            slGetScannerCapability() returns the valid values.
     * @param brightness a value between -1000 and 1000. Only used when using
     *            the TWAIN driver.
     * @param contrast a value between -1000 and 1000. Only used when using the
     *            TWAIN driver.
     * @param filename The file name to save the bitmap to.
     * 
     * @return SC_SUCCESS if valid. SC_FAIL unable to scan an image.
     */
    public native ScanLibResult scanFlatbed(long verbose, long dpi, int brightness, int contrast,
        String filename);

    /**
     * Used to scan a region of the flatbed containing an 2d barcodes and decode
     * individual rectangles within the image.
     * 
     * @param verbose set this to non-zero to see debugging output.
     * @param dpi the dots per inch setting to scan the image. Valid values are
     *            300, 400, 600.
     * @param brightness a value between -1000 and 1000. Only used when using
     *            the TWAIN driver.
     * @param contrast a value between -1000 and 1000. Only used when using the
     *            TWAIN driver.
     * @param region the bounding box for the region to be scanned. The top-left
     *            and bottom-right points in units of inches.
     * @param decodeOptions See the constructor for {@link DecodeOptions} for a
     *            description of these settings.
     * @param wells An array of {@link CellRectangle} objects defining the well
     *            regions containing 2D barcode tubes.
     */
    public native DecodeResult scanAndDecode(long verbose, long dpi, int brightness, int contrast,
        BoundingBox region, DecodeOptions decodeOptions, CellRectangle[] wells);

    /**
     * Used to decode individual rectangles within the image containing 2D
     * barcodes.
     * 
     * @param verbose set this to non-zero to see debugging output.
     * @param filename the filename containing an image with 2D barcodes.
     * @param decodeOptions See the constructor for {@link DecodeOptions} for a
     *            description of these settings.
     * @param wells An array of {@link CellRectangle} objects defining the well
     *            regions containing 2D barcode tubes.
     * 
     */
    public native DecodeResult decodeImage(long verbose, String filename,
        DecodeOptions decodeOptions, CellRectangle[] wells);

}

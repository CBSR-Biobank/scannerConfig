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

    public static final int CAP_IS_WIA = 0x01;

    public static final int CAP_DPI_300 = 0x02;

    public static final int CAP_DPI_400 = 0x04;

    public static final int CAP_DPI_600 = 0x08;

    public static final int CAP_IS_SCANNER = 0x10;

    private static ScanLib instance = null;

    protected ScanLib() {
    }

    public static ScanLib getInstance() {
        if (instance != null)
            return instance;

        instance = new ScanLib();

        if (instance == null) {
            throw new NullPointerException(
                "scanlib not supported on your operating system"); //$NON-NLS-1$
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
     * @param brightness a value between -1000 and 1000.
     * @param contrast a value between -1000 and 1000.
     * @param left The left margin in inches.
     * @param top The top margin in inches.
     * @param right The width in inches.
     * @param bottom The height in inches.
     * @param filename The file name to save the bitmap to.
     * 
     * @return SC_SUCCESS if valid. SC_FAIL unable to scan an image.
     */
    public native ScanLibResult scanImage(long verbose, long dpi,
        int brightness, int contrast, ScanRegion region, String filename);

    /**
     * Scans the whole flatbed region. The image is in Windows BMP format.
     * 
     * @param verbose The amount of logging information to generate. 1 is
     *            minimal and 9 is very detailed. Logging information is
     *            appended to file dmscanlib.log.
     * @param dpi The dots per inch for the image. Function
     *            slGetScannerCapability() returns the valid values.
     * @param brightness a value between -1000 and 1000.
     * @param contrast a value between -1000 and 1000.
     * @param filename The file name to save the bitmap to.
     * 
     * @return SC_SUCCESS if valid. SC_FAIL unable to scan an image.
     */
    public native ScanLibResult scanFlatbed(long verbose, long dpi,
        int brightness, int contrast, String filename);

    /**
     * Scans an image specified by region and then decodes sub regions. The
     * decoded barcodes are returned by the method.
     * 
     * Calling this function also creates the "decoded.bmp" windows bitmap file.
     * This file shows a green square around the barcodes that were successfully
     * decoded. If the regions failed to decode then a red square is drawn
     * around it.
     * 
     * @param verbose The amount of logging information to generate. 1 is
     *            minimal and 9 is very detailed. Logging information is
     *            appended to file scanlib.log.
     * @param
     * @param plateNum The plate number. Must be a number between 1 and 5.
     * @param left The left margin in inches.
     * @param top The top margin in inches.
     * @param right The width in inches.
     * @param bottom The height in inches.
     * @param scanGap The number of pixels to use for scan grid gap. This is a
     *            libdmtx parameter.
     * @param squareDev Maximum deviation (degrees) from squareness between
     *            adjacent barcode sides. Default value is N=40, but N=10 is
     *            recommended for flat applications like faxes and other scanned
     *            documents. Barcode regions found with corners <(90-N) or
     *            >(90+N) will be ignored by the decoder.
     * @param edgeThresh Set the minimum edge threshold as a percentage of
     *            maximum. For example, an edge between a pure white and pure
     *            black pixel would have an intensity of 100. Edges with
     *            intensities below the indicated threshold will be ignored by
     *            the decoding process. Lowering the threshold will increase the
     *            amount of work to be done, but may be necessary for low
     *            contrast or blurry images.
     * @param corrections The number of corrections to make while decoding.
     * @param cellDistance The distance in inches to use between cells.
     * @param orientation 0 for landscape, 1 for portrait.
     * 
     * @return SC_SUCCESS if the decoding process was successful.
     *         SC_INVALID_IMAGE if the scanned image is invalid.he INI file.
     *         SC_INVALID_POSITION if no sample found on row A or column 1 of
     *         the pallet. SC_POS_CALC_ERROR if sample positions could not be
     *         determined.
     */
    public native DecodeResult decodePlate(long verbose, long dpi,
        int brightness, int contrast, long plateNum, ScanRegion region,
        double scanGap, long squareDev, long edgeThresh, long corrections,
        double cellDistance, double gapX, double gapY, long profileA,
        long profileB, long profileC, long orientation);

    /**
     * From the regions specified in the INI file for the corresponding plate,
     * decodes all the regions. The decoded barcodes are written to the file
     * "dmscanlib.txt". The scanlib.txt file is a comma separated value file
     * with the following columns: Plate, Row, Column, Barcode.
     * 
     * Calling this function also creates the "decoded.bmp" windows bitmap file.
     * This file shows a green square around the barcodes that were successfully
     * decoded. If the regions failed to decode then a red square is drawn
     * around it.
     * 
     * @param verbose The amount of logging information to generate. 1 is
     *            minimal and 9 is very detailed. Logging information is
     *            appended to file scanlib.log.
     * @param plateNum The plate number. Must be a number beteen 1 and 4.
     * @param filename The windows bitmap file to decode.
     * @param scanGap The number of pixels to use for scan grid gap. This is a
     *            libdmtx parameter.
     * @param squareDev Maximum deviation (degrees) from squareness between
     *            adjacent barcode sides. Default value is N=40, but N=10 is
     *            recommended for flat applications like faxes and other scanned
     *            documents. Barcode regions found with corners <(90-N) or
     *            >(90+N) will be ignored by the decoder.
     * @param edgeThresh Set the minimum edge threshold as a percentage of
     *            maximum. For example, an edge between a pure white and pure
     *            black pixel would have an intensity of 100. Edges with
     *            intensities below the indicated threshold will be ignored by
     *            the decoding process. Lowering the threshold will increase the
     *            amount of work to be done, but may be necessary for low
     *            contrast or blurry images.
     * @param corrections The number of corrections to make while decoding.
     * @param cellDistance The distance in inches to use between cells.
     * @param orientation 0 for landscape, 1 for portrait.
     * 
     * @return SC_SUCCESS if the decoding process was successful.
     *         SC_INVALID_IMAGE if the scanned image is invalid.
     *         SC_INVALID_POSITION if no sample found on row A or column 1 of
     *         the pallet. SC_POS_CALC_ERROR if sample positions could not be
     *         determined.
     */
    public native DecodeResult decodeImage(long verbose, long plateNum,
        String filename, double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY,
        long profileA, long profileB, long profileC, long orientation);

}

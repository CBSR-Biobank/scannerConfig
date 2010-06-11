package edu.ualberta.med.scannerconfig.scanlib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ScanLib {
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

	/**
	 * The TWAIN driver was not found.
	 */
	public static final int SC_TWAIN_UAVAIL = -2;

	/**
	 * An invalid DPI value was specified.
	 */
	public static final int SC_INVALID_DPI = -3;

	/**
	 * The plate number used is invalid. Must be 1 to 5.
	 */
	public static final int SC_INVALID_PLATE_NUM = -4;

	/**
	 * The user did not select a valid scanning source.
	 */
	public static final int SC_INVALID_VALUE = -5;

	/**
	 * The scanned image is invalid.
	 */
	public static final int SC_INVALID_IMAGE = -6;

	/**
	 * Sample not found on row A or column 1
	 */
	public static final int SC_INVALID_POSITION = -7;

	/**
	 * Could not determine positions.
	 */
	public static final int SC_POS_CALC_ERROR = -8;

	private static final Map<Integer, String> ERROR_MSG;
	static {
		Map<Integer, String> aMap = new HashMap<Integer, String>();
		aMap.put(SC_SUCCESS, "The call to ScanLib was successful");
		aMap.put(SC_FAIL, "Unable to scan an image");
		aMap.put(SC_TWAIN_UAVAIL, "The TWAIN driver was not found");
		aMap.put(SC_INVALID_DPI, "An invalid DPI value was specified");
		aMap.put(SC_INVALID_PLATE_NUM, "The plate number used is invalid");
		aMap.put(SC_INVALID_VALUE,
				"The user did not select a valid scanning source");
		aMap.put(SC_INVALID_IMAGE, "The scanned image is invalid");
		aMap.put(SC_INVALID_POSITION, "Sample not found on row A or column 1.");
		aMap.put(SC_POS_CALC_ERROR, "Could not determine aliquot positions.");
		ERROR_MSG = Collections.unmodifiableMap(aMap);
	};

	public static final int CAP_IS_WIA = 0x01;

	public static final int CAP_DPI_300 = 0x02;

	public static final int CAP_DPI_400 = 0x04;

	public static final int CAP_DPI_600 = 0x08;

	public static final int CAP_IS_SCANNER = 0x10;

	private static ScanLib instance = null;

	protected ScanLib() {

	}

	public static String getErrMsg(int err) throws IllegalArgumentException {
		if ((err < SC_POS_CALC_ERROR) || (err > SC_SUCCESS)) {
			throw new IllegalArgumentException("value " + err
					+ " is not a valid error code");
		}
		return ERROR_MSG.get(err);
	}

	public static ScanLib getInstance() {
		if (instance != null)
			return instance;

		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows")) {
			instance = new ScanLibWin32();
		} else if (osname.startsWith("Linux")) {
			instance = new ScanLibSimulate();
		}

		if (instance == null) {
			throw new RuntimeException("scanlib not supported on your os");
		}
		return instance;
	}

	/**
	 * Queries the availability of the TWAIN driver.
	 * 
	 * @return SC_SUCCESS if available, and SC_INVALID_VALUE if unavailable.
	 */
	public abstract int slIsTwainAvailable();

	/**
	 * Creates a dialog box to allow the user to select the scanner to use by
	 * default.
	 * 
	 * @return SC_SUCCESS when selected by the user, and SC_INVALID_VALUE if the
	 *         user cancelled the selection dialog.
	 */
	public abstract int slSelectSourceAsDefault();

	/**
	 * Queries the selected scanner for the driver type and supported dpi.
	 * 
	 * @return Bit 1: Is set if driver type is WIA. Bits 2,3,4 are set if driver
	 *         supports 300,400,600 dpi. Bit 5 is set if a proper scanner source
	 *         is selected.
	 */
	public abstract int slGetScannerCapability();

	/**
	 * Scans an image for the specified dimensions. The image is in Windows BMP
	 * format.
	 * 
	 * @param verbose
	 *            The amount of logging information to generate. 1 is minimal
	 *            and 9 is very detailed. Logging information is appended to
	 *            file scanlib.log.
	 * @param dpi
	 *            The dots per inch for the image. Possible values are 200, 300,
	 *            400, 600, 720, 800.
	 * @param brightness
	 *            a value between -1000 and 1000.
	 * @param contrast
	 *            a value between -1000 and 1000.
	 * @param left
	 *            The left margin in inches.
	 * @param top
	 *            The top margin in inches.
	 * @param right
	 *            The width in inches.
	 * @param bottom
	 *            The height in inches.
	 * @param filename
	 *            The file name to save the bitmap to.
	 * 
	 * @return SC_SUCCESS if valid. SC_FAIL unable to scan an image.
	 */
	public abstract int slScanImage(long verbose, long dpi, int brightness,
			int contrast, double left, double top, double right, double bottom,
			String filename);

	/**
	 * From the regions specified in the INI file for the corresponding plate,
	 * scans an image and then decodes all the regions. The decoded barcodes are
	 * written to the file "scanlib.txt". The scanlib.txt file is a comma
	 * separated value file with the following columns: Plate, Row, Column,
	 * Barcode.
	 * 
	 * Calling this function also creates the "decoded.bmp" windows bitmap file.
	 * This file shows a green square around the barcodes that were successfully
	 * decoded. If the regions failed to decode then a red square is drawn
	 * around it.
	 * 
	 * @param verbose
	 *            The amount of logging information to generate. 1 is minimal
	 *            and 9 is very detailed. Logging information is appended to
	 *            file scanlib.log.
	 * @param plateNum
	 *            The plate number. Must be a number between 1 and 5.
	 * @param left
	 *            The left margin in inches.
	 * @param top
	 *            The top margin in inches.
	 * @param right
	 *            The width in inches.
	 * @param bottom
	 *            The height in inches.
	 * @param scanGap
	 *            The number of pixels to use for scan grid gap. This is a
	 *            libdmtx parameter.
	 * @param squareDev
	 *            Maximum deviation (degrees) from squareness between adjacent
	 *            barcode sides. Default value is N=40, but N=10 is recommended
	 *            for flat applications like faxes and other scanned documents.
	 *            Barcode regions found with corners <(90-N) or >(90+N) will be
	 *            ignored by the decoder.
	 * @param edgeThresh
	 *            Set the minimum edge threshold as a percentage of maximum. For
	 *            example, an edge between a pure white and pure black pixel
	 *            would have an intensity of 100. Edges with intensities below
	 *            the indicated threshold will be ignored by the decoding
	 *            process. Lowering the threshold will increase the amount of
	 *            work to be done, but may be necessary for low contrast or
	 *            blurry images.
	 * @param corrections
	 *            The number of corrections to make while decoding.
	 * @param cellDistance
	 *            The distance in inches to use between cells.
	 * 
	 * @return SC_SUCCESS if the decoding process was successful.
	 *         SC_INVALID_IMAGE if the scanned image is invalid.he INI file.
	 *         SC_INVALID_POSITION if no sample found on row A or column 1 of
	 *         the pallet. SC_POS_CALC_ERROR if sample positions could not be
	 *         determined.
	 */
	public abstract int slDecodePlate(long verbose, long dpi, int brightness,
			int contrast, long plateNum, double left, double top, double right,
			double bottom, double scanGap, long squareDev, long edgeThresh,
			long corrections, double cellDistance);

	public abstract int slDecodePlateMultipleDpi(long verbose, long dpi1,
			long dpi2, long dpi3, int brightness, int contrast, long plateNum,
			double left, double top, double right, double bottom,
			double scanGap, long squareDev, long edgeThresh, long corrections,
			double cellDistance);

	/**
	 * From the regions specified in the INI file for the corresponding plate,
	 * decodes all the regions. The decoded barcodes are written to the file
	 * "scanlib.txt". The scanlib.txt file is a comma separated value file with
	 * the following columns: Plate, Row, Column, Barcode.
	 * 
	 * Calling this function also creates the "decoded.bmp" windows bitmap file.
	 * This file shows a green square around the barcodes that were successfully
	 * decoded. If the regions failed to decode then a red square is drawn
	 * around it.
	 * 
	 * @param verbose
	 *            The amount of logging information to generate. 1 is minimal
	 *            and 9 is very detailed. Logging information is appended to
	 *            file scanlib.log.
	 * @param plateNum
	 *            The plate number. Must be a number beteen 1 and 4.
	 * @param filename
	 *            The windows bitmap file to decode.
	 * @param scanGap
	 *            The number of pixels to use for scan grid gap. This is a
	 *            libdmtx parameter.
	 * @param squareDev
	 *            Maximum deviation (degrees) from squareness between adjacent
	 *            barcode sides. Default value is N=40, but N=10 is recommended
	 *            for flat applications like faxes and other scanned documents.
	 *            Barcode regions found with corners <(90-N) or >(90+N) will be
	 *            ignored by the decoder.
	 * @param edgeThresh
	 *            Set the minimum edge threshold as a percentage of maximum. For
	 *            example, an edge between a pure white and pure black pixel
	 *            would have an intensity of 100. Edges with intensities below
	 *            the indicated threshold will be ignored by the decoding
	 *            process. Lowering the threshold will increase the amount of
	 *            work to be done, but may be necessary for low contrast or
	 *            blurry images.
	 * @param corrections
	 *            The number of corrections to make while decoding.
	 * @param cellDistance
	 *            The distance in inches to use between cells.
	 * 
	 * @return SC_SUCCESS if the decoding process was successful.
	 *         SC_INVALID_IMAGE if the scanned image is invalid.
	 *         SC_INVALID_POSITION if no sample found on row A or column 1 of
	 *         the pallet. SC_POS_CALC_ERROR if sample positions could not be
	 *         determined.
	 */
	public abstract int slDecodeImage(long verbose, long plateNum,
			String filename, double scanGap, long squareDev, long edgeThresh,
			long corrections, double cellDistance);

}

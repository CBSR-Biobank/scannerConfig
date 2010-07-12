package edu.ualberta.med.scannerconfig.scanlib;

public class ScanLibWin32 extends ScanLib {

	@Override
	public int slIsTwainAvailable() {
		return ScanLibWin32Wrapper.slIsTwainAvailable();
	}

	@Override
	public int slSelectSourceAsDefault() {
		return ScanLibWin32Wrapper.slSelectSourceAsDefault();
	}

	@Override
	public int slScanImage(long verbose, long dpi, int brightness,
			int contrast, double left, double top, double right, double bottom,
			String filename) {
		return ScanLibWin32Wrapper.slScanImage(verbose, dpi, brightness,
				contrast, left, top, right, bottom, filename);
	}

	@Override
	public int slDecodePlate(long verbose, long dpi, int brightness,
			int contrast, long plateNum, double left, double top, double right,
			double bottom, double scanGap, long squareDev, long edgeThresh,
			long corrections, double cellDistance) {
		return ScanLibWin32Wrapper.slDecodePlate(verbose, dpi, brightness,
				contrast, plateNum, left, top, right, bottom, scanGap,
				squareDev, edgeThresh, corrections, cellDistance);
	}

	@Override
	public int slDecodeImage(long verbose, long plateNum, String filename,
			double scanGap, long squareDev, long edgeThresh, long corrections,
			double cellDistance) {
		return ScanLibWin32Wrapper.slDecodeImage(verbose, plateNum, filename,
				scanGap, squareDev, edgeThresh, corrections, cellDistance);
	}

	@Override
	public int slGetScannerCapability() {

		int cap = ScanLibWin32Wrapper.slGetScannerCapability();

		/* XXX getcap Debug Code */
		// cap = cap & (~ScanLib.CAP_DPI_600);

		return cap;
	}
}

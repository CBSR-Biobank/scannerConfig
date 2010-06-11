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
	public int slDecodePlateMultipleDpi(long verbose, long dpi1, long dpi2,
			long dpi3, int brightness, int contrast, long plateNum,
			double left, double top, double right, double bottom,
			double scanGap, long squareDev, long edgeThresh, long corrections,
			double cellDistance) {
		return ScanLibWin32Wrapper.slDecodePlateMultipleDpi(verbose, dpi1,
				dpi2, dpi3, brightness, contrast, plateNum, left, top, right,
				bottom, scanGap, squareDev, edgeThresh, corrections,
				cellDistance);
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
		return ScanLibWin32Wrapper.slGetScannerCapability();
	}


}

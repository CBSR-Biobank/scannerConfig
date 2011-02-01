package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanLibWin32 extends ScanLib {

    @Override
    public int slIsTwainAvailable() {
        return DmScanLibWin32Wrapper.slIsTwainAvailable();
    }

    @Override
    public int slSelectSourceAsDefault() {
        return DmScanLibWin32Wrapper.slSelectSourceAsDefault();
    }

    @Override
    public int slScanImage(long verbose, long dpi, int brightness,
        int contrast, double left, double top, double right, double bottom,
        String filename) {
        return DmScanLibWin32Wrapper.slScanImage(verbose, dpi, brightness,
            contrast, left, top, right, bottom, filename);
    }

    @Override
    public int slScanFlatbed(long verbose, long dpi, int brightness,
        int contrast, String filename) {
        return DmScanLibWin32Wrapper.slScanFlatbed(verbose, dpi, brightness,
            contrast, filename);
    }

    @Override
    public int slDecodePlate(long verbose, long dpi, int brightness,
        int contrast, long plateNum, double left, double top, double right,
        double bottom, double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY,
        long profileA, long profileB, long profileC, long isVertical) {
        return DmScanLibWin32Wrapper.slDecodePlate(verbose, dpi, brightness,
            contrast, plateNum, left, top, right, bottom, scanGap, squareDev,
            edgeThresh, corrections, cellDistance, gapX, gapY, profileA,
            profileB, profileC, isVertical);
    }

    @Override
    public int slDecodeImage(long verbose, long plateNum, String filename,
        double scanGap, long squareDev, long edgeThresh, long corrections,
        double cellDistance, double gapX, double gapY, long profileA,
        long profileB, long profileC, long isVertical) {
        return DmScanLibWin32Wrapper.slDecodeImage(verbose, plateNum, filename,
            scanGap, squareDev, edgeThresh, corrections, cellDistance, gapX,
            gapY, profileA, profileB, profileC, isVertical);
    }

    @Override
    public int slGetScannerCapability() {

        int cap = DmScanLibWin32Wrapper.slGetScannerCapability();

        return cap;
    }
}

package edu.ualberta.med.scannerconfig.dmscanlib;

public class ScanLibWin32 extends ScanLib {

    @Override
    public native ScanLibResult slIsTwainAvailable();

    @Override
    public native ScanLibResult slSelectSourceAsDefault();

    @Override
    public native ScanLibResult slGetScannerCapability();

    @Override
    public native ScanLibResult slScanImage(long verbose, long dpi,
        int brightness, int contrast, double left, double top, double right,
        double bottom, String filename);

    @Override
    public native ScanLibResult slScanFlatbed(long verbose, long dpi,
        int brightness, int contrast, String filename);

    @Override
    public native DecodeResult slDecodePlate(long verbose, long dpi,
        int brightness, int contrast, long plateNum, double left, double top,
        double right, double bottom, double scanGap, long squareDev,
        long edgeThresh, long corrections, double cellDistance, double gapX,
        double gapY, long profileA, long profileB, long profileC,
        long isVertical);

    @Override
    public native DecodeResult slDecodeImage(long verbose, long plateNum,
        String filename, double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY,
        long profileA, long profileB, long profileC, long isVertical);
}

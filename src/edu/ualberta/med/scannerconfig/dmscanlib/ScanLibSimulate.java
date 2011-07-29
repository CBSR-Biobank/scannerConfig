package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ScanLibSimulate extends ScanLib {

    private static Random r = new Random();

    @Override
    public ScanLibResult slIsTwainAvailable() {
        return new ScanLibResult(SC_FAIL,
            "function not supported in simulation environment");
    }

    @Override
    public ScanLibResult slSelectSourceAsDefault() {
        return new ScanLibResult(SC_FAIL,
            "function not supported in simulation environment");
    }

    @Override
    public ScanLibResult slGetScannerCapability() {
        return new ScanLibResult(SC_FAIL,
            "function not supported in simulation environment");
    }

    @Override
    public ScanLibResult slScanImage(long verbose, long dpi, int brightness,
        int contrast, double left, double top, double right, double bottom,
        String filename) {
        return new ScanLibResult(SC_FAIL,
            "function not supported in simulation environment");
    }

    @Override
    public ScanLibResult slScanFlatbed(long verbose, long dpi, int brightness,
        int contrast, String filename) {
        return new ScanLibResult(SC_FAIL,
            "function not supported in simulation environment");
    }

    @Override
    public DecodeResult slDecodePlate(long verbose, long dpi, int brightness,
        int contrast, long plateNum, double left, double top, double right,
        double bottom, double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY,
        long profileA, long profileB, long profileC, long isVertical) {
        DecodeResult result = new DecodeResult(SC_SUCCESS, null);

        List<Integer> sampleIds = new ArrayList<Integer>();
        int samples = r.nextInt(96) + 1;
        for (int i = 0; i < samples; ++i) {
            sampleIds.add(r.nextInt(samples));
        }
        Collections.sort(sampleIds);

        for (Integer id : sampleIds) {
            result.setCell(id / 12, id % 12, getRandomString(10));
        }

        return result;
    }

    private static String getRandomString(int maxlen) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < maxlen; ++j) {
            sb.append((char) ('A' + r.nextInt(26)));
        }
        return sb.toString();
    }

    @Override
    public DecodeResult slDecodeImage(long verbose, long plateNum,
        String filename, double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY,
        long profileA, long profileB, long profileC, long isVertical) {
        return new DecodeResult(SC_FAIL,
            "function not supported in simulation environment");
    }

}

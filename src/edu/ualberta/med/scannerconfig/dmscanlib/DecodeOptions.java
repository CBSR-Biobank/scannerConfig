package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeOptions {  
    public static final double DEFAULT_SCAN_GAP = 0.085;
    public static final long   DEFAULT_SQUARE_DEV = 10;
    public static final long   DEFAULT_EDGE_THRESH = 5;
    public static final long   DEFAULT_CORRECTIONS = 10;
    public static final long   DEFAULT_SHRINK = 1;  
    
    private final double scanGap;
    private final long squareDev;
    private final long edgeThresh;
    private final long corrections;
    private final long shrink;

    public DecodeOptions(double scanGap, long squareDev, long edgeThresh,
        long corrections, long shrink) {
        this.scanGap = scanGap;
        this.squareDev = squareDev;
        this.edgeThresh = edgeThresh;
        this.corrections = corrections;
        this.shrink = shrink;

    }

    public double getScanGap() {
        return scanGap;
    }

    public long getSquareDev() {
        return squareDev;
    }

    public long getEdgeThresh() {
        return edgeThresh;
    }

    public long getCorrections() {
        return corrections;
    }

    public long getShrink() {
        return shrink;
    }
    
    public static DecodeOptions getDefaultDecodeOptions() {
        return new DecodeOptions(DecodeOptions.DEFAULT_SCAN_GAP, 
            DecodeOptions.DEFAULT_SQUARE_DEV, DecodeOptions.DEFAULT_EDGE_THRESH,
            DecodeOptions.DEFAULT_CORRECTIONS, DecodeOptions.DEFAULT_SHRINK);   
    }
}

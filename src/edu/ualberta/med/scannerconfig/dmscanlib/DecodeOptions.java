package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeOptions {
    private final double scanGap;
    private final long squareDev;
    private final long edgeThresh;
    private final long corrections;
    private final long shrink;

    // DecodeOptions(0.085, 15, 5, 10, 0.345);

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
}

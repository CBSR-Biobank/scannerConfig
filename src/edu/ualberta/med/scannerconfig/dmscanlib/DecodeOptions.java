package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeOptions {
    final double scanGap;
    final long squareDev;
    final long edgeThresh;
    final long corrections;
    private final long shrink;
    final double cellDistance;

    // DecodeOptions(0.085, 15, 5, 10, 0.345);

    public DecodeOptions(double scanGap, long squareDev, long edgeThresh,
        long corrections, long shrink, double cellDistance) {
        this.scanGap = scanGap;
        this.squareDev = squareDev;
        this.edgeThresh = edgeThresh;
        this.corrections = corrections;
        this.shrink = shrink;
        this.cellDistance = cellDistance;

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

    public double getCellDistance() {
        return cellDistance;
    }
}

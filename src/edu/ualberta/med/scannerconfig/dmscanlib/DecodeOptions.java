package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeOptions {
    final double scanGap;
    final long squareDev;
    final long edgeThresh;
    final long corrections;
    final double cellDistance;

    public DecodeOptions(double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance) {
        this.scanGap = scanGap;
        this.squareDev = squareDev;
        this.edgeThresh = edgeThresh;
        this.corrections = corrections;
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

    public double getCellDistance() {
        return cellDistance;
    }
}

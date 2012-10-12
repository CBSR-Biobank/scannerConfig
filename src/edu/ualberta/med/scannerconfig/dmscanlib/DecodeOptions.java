package edu.ualberta.med.scannerconfig.dmscanlib;

public class DecodeOptions {
    final double scanGap;
    final long squareDev;
    final long edgeThresh;
    final long corrections;
    final double cellDistance;
    final double gapX;
    final double gapY;

    public DecodeOptions(double scanGap, long squareDev, long edgeThresh,
        long corrections, double cellDistance, double gapX, double gapY) {
        this.scanGap = scanGap;
        this.squareDev = squareDev;
        this.edgeThresh = edgeThresh;
        this.corrections = corrections;
        this.cellDistance = cellDistance;
        this.gapX = gapX;
        this.gapY = gapY;

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

    public double getGapX() {
        return gapX;
    }

    public double getGapY() {
        return gapY;
    }
}

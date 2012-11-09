package edu.ualberta.med.scannerconfig.dmscanlib;

import edu.ualberta.med.biobank.util.SbsLabeling;

public final class DecodedWell implements Comparable<DecodedWell> {

    final String label;
    final String message;

    public DecodedWell(String label, String message) {
        this.label = label;
        this.message = message;
    }

    public DecodedWell(int row, int col, String message) {
        this.label = SbsLabeling.fromRowCol(row, col);
        this.message = message;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int compareTo(DecodedWell o) {
        return label.compareTo(o.label);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(label).append(": ").append(message);
        return sb.toString();
    }
}

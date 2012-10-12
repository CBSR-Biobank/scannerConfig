package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.Set;
import java.util.TreeSet;

public class DecodeResult extends ScanLibResult {

    private final Set<DecodedWell> wells = new TreeSet<DecodedWell>();

    public DecodeResult(int resultCode, int value, String message) {
        super(resultCode, value, message);
    }

    public void addWell(String label, String message) {
        wells.add(new DecodedWell(label, message));
    }

    public Set<DecodedWell> getDecodedWells() {
        return wells;
    }
}

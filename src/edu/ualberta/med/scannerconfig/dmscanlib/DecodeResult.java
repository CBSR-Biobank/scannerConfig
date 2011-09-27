package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.ArrayList;
import java.util.List;

public class DecodeResult extends ScanLibResult {

    private List<ScanCell> cells = null;

    public DecodeResult(int resultCode, int value, String message) {
        super(resultCode, value, message);
    }

    /**
     * Called by JNI interface to dmscanlib.
     * 
     * @param row
     * @param col
     * @param message
     */
    public void setCell(int row, int col, String message) {
        if ((row < 0) || (row >= ScanCellPos.ROW_MAX)) {
            throw new IndexOutOfBoundsException("invalid row: " + row); //$NON-NLS-1$
        }

        if ((col < 0) || (col >= ScanCellPos.COL_MAX)) {
            throw new IndexOutOfBoundsException("invalid column: " + col); //$NON-NLS-1$
        }

        if (cells == null) {
            cells = new ArrayList<ScanCell>();
        }

        cells.add(new ScanCell(row, col, message));
    }

    public List<ScanCell> getCells() {
        if (cells == null) {
            throw new NullPointerException("cells not initialized"); //$NON-NLS-1$
        }
        return cells;
    }
}

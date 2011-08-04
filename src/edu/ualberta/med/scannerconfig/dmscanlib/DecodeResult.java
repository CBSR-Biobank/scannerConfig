package edu.ualberta.med.scannerconfig.dmscanlib;

import java.util.HashMap;
import java.util.Map;

public class DecodeResult extends ScanLibResult {

    private Map<ScanCellPos, ScanCell> cells = null;

    public DecodeResult(int resultCode, int value, String message) {
        super(resultCode, value, message);
    }

    public ScanCell getCell(int row, int col) {
        if ((row < 0) || (row >= ScanCellPos.ROW_MAX)) {
            throw new IndexOutOfBoundsException("invalid row: " + row);
        }

        if ((col < 0) || (col >= ScanCellPos.COL_MAX)) {
            throw new IndexOutOfBoundsException("invalid column: " + col);
        }

        if (cells == null) {
            throw new NullPointerException("cells were not initialized");
        }

        return cells.get(new ScanCellPos(row, col));
    }

    public void setCell(int row, int col, String message) {
        if ((row < 0) || (row >= ScanCellPos.ROW_MAX)) {
            throw new IndexOutOfBoundsException("invalid row: " + row);
        }

        if ((col < 0) || (col >= ScanCellPos.COL_MAX)) {
            throw new IndexOutOfBoundsException("invalid column: " + col);
        }

        if (cells == null) {
            cells = new HashMap<ScanCellPos, ScanCell>();
        }

        cells.put(new ScanCellPos(row, col), new ScanCell(row, col, message));
    }

    public Map<ScanCellPos, ScanCell> getCells() {
        if (cells == null) {
            cells = new HashMap<ScanCellPos, ScanCell>();
        }
        return cells;
    }
}
